package com.deare.backend.api.setting;

import com.deare.backend.api.setting.service.SettingWriteService;
import com.deare.backend.api.setting.service.SettingWriteServiceImpl;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.entity.enums.Provider;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, SettingWriteServiceImpl.class})
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SettingWriteServiceImplTest {

    @Autowired private SettingWriteService settingWriteService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserSettingRepository userSettingRepository;

    /**
     * 동일 사용자 설정의 동시 생성 방지 검증
     * (1) 두 트랜잭션이 동시에 생성을 요청해도 모두 정상 종료되는가?
     * (2) 사용자 설정이 한 건만 생성되는가?
     * (3) 새로 생성된 설정은 해금 안내 미대상 상태인가?
     */
    @Test
    void concurrentEnsureCreatesSingleSetting() throws Exception {
        User user = userRepository.saveAndFlush(User.signUpUser(
                Provider.GOOGLE,
                "concurrent-setting",
                "concurrent-setting@example.com",
                "concurrent-setting"
        ));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<Void>> results = List.of(
                    executor.submit(() -> ensureAfterSignal(user.getId(), ready, start)),
                    executor.submit(() -> ensureAfterSignal(user.getId(), ready, start))
            );

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            for (Future<Void> result : results) {
                result.get(5, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(userSettingRepository.findByUser_Id(user.getId()))
                .get()
                .matches(setting -> !setting.shouldShowDecorationUnlockGuide());
        assertThat(userSettingRepository.count()).isEqualTo(1);
    }

    private Void ensureAfterSignal(
            Long userId,
            CountDownLatch ready,
            CountDownLatch start
    ) throws InterruptedException {
        ready.countDown();
        start.await();
        settingWriteService.ensureSettingExists(userId);
        return null;
    }
}
