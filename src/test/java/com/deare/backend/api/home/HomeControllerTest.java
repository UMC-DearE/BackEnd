package com.deare.backend.api.home;

import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.entity.enums.ContentType;
import com.deare.backend.domain.image.entity.enums.FileType;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.sticker.entity.UserSticker;
import com.deare.backend.domain.sticker.repository.UserStickerRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.entity.enums.Provider;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.auth.jwt.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private UserSettingRepository userSettingRepository;
    @Autowired private UserStickerRepository userStickerRepository;
    @Autowired private ImageRepository imageRepository;
    @Autowired private JwtProvider jwtProvider;

    private User user;
    private String accessToken;
    private Image image1;
    private Image image2;

    @BeforeEach
    void setUp() {
        userStickerRepository.deleteAll();
        userSettingRepository.deleteAll();
        imageRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.signUpUser(Provider.KAKAO, "home-user-1", "home@test.com", "홈유저"));
        accessToken = jwtProvider.generateAccessToken(user);

        image1 = imageRepository.save(Image.create("key1", "https://cdn.test/1.png", "1.png", FileType.PNG, 1024L, ContentType.STICKER));
        image2 = imageRepository.save(Image.create("key2", "https://cdn.test/2.png", "2.png", FileType.PNG, 2048L, ContentType.STICKER));
    }

    private String editHomeRequest(String homeColor, List<Map<String, Object>> stickers) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "homeColor", homeColor,
                "stickers", stickers
        ));
    }

    private Map<String, Object> stickerPayload(Long imageId, double posX, double posY, int posZ, double rotation, double scale) {
        return Map.of(
                "imageId", imageId,
                "posX", posX,
                "posY", posY,
                "posZ", posZ,
                "rotation", rotation,
                "scale", scale
        );
    }

    @Test
    @DisplayName("홈 편집 - 배경색과 스티커가 한 번에 저장된다")
    void editHome_success() throws Exception {
        String body = editHomeRequest("#FFAA00", List.of(
                stickerPayload(image1.getId(), 10.5, 20.25, 1, 45.0, 1.5)
        ));

        mockMvc.perform(put("/api/v1/home/edit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        UserSetting setting = userSettingRepository.findByUser_Id(user.getId()).orElseThrow();
        assertThat(setting.getHomeColor()).isEqualTo("#FFAA00");

        List<UserSticker> stickers = userStickerRepository.findAllByUser_IdOrderByPosZAsc(user.getId());
        assertThat(stickers).hasSize(1);
        assertThat(stickers.get(0).getImage().getId()).isEqualTo(image1.getId());
        assertThat(stickers.get(0).getPosZ()).isEqualTo(1);

        mockMvc.perform(get("/api/v1/home").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.setting.homeColor").value("#FFAA00"))
                .andExpect(jsonPath("$.data.stickers", hasSize(1)))
                .andExpect(jsonPath("$.data.stickers[0].imageId").value(image1.getId()));
    }

    @Test
    @DisplayName("홈 편집 - UserSetting이 없는 유저도 새로 생성되며 저장된다")
    void editHome_createsSettingWhenMissing() throws Exception {
        assertThat(userSettingRepository.findByUser_Id(user.getId())).isEmpty();

        String body = editHomeRequest("#123456", List.of());

        mockMvc.perform(put("/api/v1/home/edit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        UserSetting setting = userSettingRepository.findByUser_Id(user.getId()).orElseThrow();
        assertThat(setting.getHomeColor()).isEqualTo("#123456");
    }

    @Test
    @DisplayName("홈 편집 - 재저장 시 기존 스티커는 새 스티커로 완전히 교체된다")
    void editHome_replacesExistingStickers() throws Exception {
        String firstBody = editHomeRequest("#FFFFFF", List.of(
                stickerPayload(image1.getId(), 1.0, 1.0, 1, 0.0, 1.0)
        ));
        mockMvc.perform(put("/api/v1/home/edit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstBody))
                .andExpect(status().isOk());
        assertThat(userStickerRepository.findAllByUser_IdOrderByPosZAsc(user.getId())).hasSize(1);

        String secondBody = editHomeRequest("#000000", List.of(
                stickerPayload(image2.getId(), 5.0, 5.0, 2, 90.0, 2.0)
        ));
        mockMvc.perform(put("/api/v1/home/edit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondBody))
                .andExpect(status().isOk());

        List<UserSticker> stickers = userStickerRepository.findAllByUser_IdOrderByPosZAsc(user.getId());
        assertThat(stickers).hasSize(1);
        assertThat(stickers.get(0).getImage().getId()).isEqualTo(image2.getId());

        UserSetting setting = userSettingRepository.findByUser_Id(user.getId()).orElseThrow();
        assertThat(setting.getHomeColor()).isEqualTo("#000000");
    }

    @Test
    @DisplayName("홈 편집 - 잘못된 HEX 색상 형식이면 400 에러")
    void editHome_invalidHomeColor() throws Exception {
        String body = editHomeRequest("blue", List.of());

        mockMvc.perform(put("/api/v1/home/edit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400_VALIDATION"));

        assertThat(userSettingRepository.findByUser_Id(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("홈 편집 - 존재하지 않는 이미지를 스티커로 참조하면 에러")
    void editHome_imageNotFound() throws Exception {
        String body = editHomeRequest("#ABCDEF", List.of(
                stickerPayload(999999L, 0.0, 0.0, 0, 0.0, 1.0)
        ));

        mockMvc.perform(put("/api/v1/home/edit")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IMAGE_40001"));
    }

    @Test
    @DisplayName("홈 편집 - 인증 토큰 없으면 401")
    void editHome_unauthorized() throws Exception {
        String body = editHomeRequest("#ABCDEF", List.of());

        mockMvc.perform(put("/api/v1/home/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
