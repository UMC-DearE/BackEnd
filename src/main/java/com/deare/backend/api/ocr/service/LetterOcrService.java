package com.deare.backend.api.ocr.service;

import com.deare.backend.api.ocr.dto.OcrLettersRequestDTO;
import com.deare.backend.api.ocr.dto.OcrLettersResponseDTO;
import com.deare.backend.api.ocr.dto.OcrLettersResponseDTO.OcrResultDTO;
import com.deare.backend.domain.letter.exception.OcrErrorCode;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.letter.repository.LetterImageRepository;
import com.deare.backend.global.common.exception.GeneralException;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
import com.deare.backend.global.external.gemini.adapter.ocr.OcrAdapter;
import com.deare.backend.global.external.gemini.dto.request.ocr.OcrPromptFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class LetterOcrService {

    private static final Long TEST_USER_ID = 1L; // TODO: 인증 연동

    private final OcrAdapter ocrAdapter;
    private final ImageRepository imageRepository;
    private final LetterImageRepository letterImageRepository;
    private final ImageContentLoader imageContentLoader;

    public OcrLettersResponseDTO ocrLetters(OcrLettersRequestDTO request) {
        List<Long> imageIds = request.getImageIds();
        validateImageIds(imageIds);

        // 1) DB에서 이미지들 조회
        List<Image> images = imageRepository.findAllById(imageIds);
        Map<Long, Image> imageMap = images.stream()
                .collect(Collectors.toMap(Image::getId, i -> i));

        // 2) 소유권은 letter_image -> letter(user_id)로 판별
        Set<Long> owned = new HashSet<>(letterImageRepository.findOwnedImageIds(TEST_USER_ID, imageIds));

        List<OcrResultDTO> results = new ArrayList<>();

        for (Long imageId : imageIds) {
            Image image = imageMap.get(imageId);

            if (image == null) {
                results.add(fail(imageId, OcrErrorCode.OCR_NOT_FOUND));
                continue;
            }

            // letter에 연결된 이미지가 아니면 소유권 판별 불가 -> 403 처리
            if (!owned.contains(imageId)) {
                results.add(fail(imageId, OcrErrorCode.OCR_FORBIDDEN));
                continue;
            }

            try {
                byte[] bytes = imageContentLoader.load(image.getImageKey());
                String base64 = Base64.getEncoder().encodeToString(bytes);

                String text = ocrAdapter.ocr(OcrPromptFactory.instruction(), base64);
                results.add(OcrResultDTO.ok(imageId, text == null ? "" : text.trim()));

            } catch (ExternalApiException e) {
                // Gemini/Feign 연동 오류는 AI_**** 그대로 내려주기
                results.add(OcrResultDTO.fail(
                        imageId,
                        e.getErrorCode().getCode(),
                        e.getErrorCode().getMessage()
                ));
            } catch (Exception e) {
                results.add(fail(imageId, OcrErrorCode.OCR_INTERNAL_ERROR));
            }
        }

        String combinedText = results.stream()
                .filter(OcrResultDTO::isSuccess)
                .map(OcrResultDTO::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n\n"));

        return OcrLettersResponseDTO.builder()
                .combinedText(combinedText)
                .results(results)
                .build();
    }

    private void validateImageIds(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            throw new GeneralException(OcrErrorCode.OCR_BAD_REQUEST);
        }
        if (imageIds.size() > 10) {
            throw new GeneralException(OcrErrorCode.OCR_TOO_MANY_IMAGES);
        }

        Set<Long> set = new HashSet<>();
        for (Long id : imageIds) {
            if (id == null || id <= 0 || !set.add(id)) {
                throw new GeneralException(OcrErrorCode.OCR_INVALID_IMAGE_IDS);
            }
        }
    }

    private OcrResultDTO fail(Long imageId, OcrErrorCode ec) {
        return OcrResultDTO.fail(imageId, ec.getCode(), ec.getMessage());
    }
}
