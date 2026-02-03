package com.deare.backend.api.ocr.service;

import com.deare.backend.api.ocr.dto.request.OcrLettersRequestDTO;
import com.deare.backend.api.ocr.dto.response.OcrLettersResponseDTO;
import com.deare.backend.api.ocr.dto.response.OcrLettersResponseDTO.OcrResultDTO;
import com.deare.backend.domain.letter.exception.OcrErrorCode;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.letter.repository.LetterImageRepository;
import com.deare.backend.global.auth.util.SecurityUtil;
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

    private final OcrAdapter ocrAdapter;
    private final ImageRepository imageRepository;
    private final LetterImageRepository letterImageRepository;
    private final ImageContentLoader imageContentLoader;

    public OcrLettersResponseDTO ocrLetters(OcrLettersRequestDTO request) {
        List<Long> imageIds = request.getImageIds();
        validateImageIds(imageIds);

        Long userId = SecurityUtil.getCurrentUserId();

        // DB에서 이미지들 조회
        List<Image> images = imageRepository.findAllById(imageIds);
        Map<Long, Image> imageMap = images.stream()
                .collect(Collectors.toMap(Image::getId, i -> i));

        Set<Long> linked = new HashSet<>(letterImageRepository.findLinkedImageIds(imageIds));
        Set<Long> owned  = new HashSet<>(letterImageRepository.findOwnedImageIds(userId, imageIds));

        List<OcrResultDTO> results = new ArrayList<>();

        for (Long imageId : imageIds) {
            Image image = imageMap.get(imageId);

            if (image == null) {
                results.add(fail(imageId, OcrErrorCode.OCR_NOT_FOUND));
                continue;
            }

            // 1) 400: letter_image에 아예 연결 안 된 이미지
            if (!linked.contains(imageId)) {
                results.add(fail(imageId, OcrErrorCode.OCR_IMAGE_NOT_LINKED));
                continue;
            }

            // 2) 403: 연결은 되어 있는데 내 편지(내 userId)가 아닐시
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
