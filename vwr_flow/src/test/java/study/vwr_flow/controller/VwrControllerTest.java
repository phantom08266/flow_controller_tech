package study.vwr_flow.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import study.vwr_flow.config.EmbeddedRedisConfig;
import study.vwr_flow.controller.dto.CreateVwrResponse;
import study.vwr_flow.controller.dto.ProceedResponse;
import study.vwr_flow.controller.dto.ProceedUserResponse;
import study.vwr_flow.exception.ServiceException;
import study.vwr_flow.service.VwrService;

@SpringBootTest
@Import(EmbeddedRedisConfig.class)
@ActiveProfiles("test")
class VwrControllerTest {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Autowired
    private VwrController vwrController;

    @BeforeEach
    void setUp() {
        // 모든 키 삭제
        ReactiveRedisConnection reactiveConnection = redisTemplate.getConnectionFactory().getReactiveConnection();
        reactiveConnection.serverCommands().flushAll().subscribe();

        // 특정 키 삭제
        // redisTemplate.opsForZSet().delete("user:queue:test:wait").subscribe();
    }

    @Test
    void register() {
        StepVerifier.create(vwrController.register("test", 1L))
                .expectNext(new CreateVwrResponse(1L))
                .verifyComplete();

        StepVerifier.create(vwrController.register("test", 2L))
                .expectNext(new CreateVwrResponse(2L))
                .verifyComplete();

        StepVerifier.create(vwrController.register("test", 3L))
                .expectNext(new CreateVwrResponse(3L))
                .verifyComplete();
    }

    @Test
    void registerExceptionTest() {
        StepVerifier.create(vwrController.register("test", 1L))
                .expectNext(new CreateVwrResponse(1L))
                .verifyComplete();

        StepVerifier.create(vwrController.register("test", 1L))
                .expectError(ServiceException.class)
                .verify();
    }

    @Test
    void proceed() {
        StepVerifier.create(vwrController.register("test", 1L)
                        .then(vwrController.register("test", 2L))
                        .then(vwrController.register("test", 3L))
                        .then(vwrController.proceed("test", 4)))
                .expectNext(new ProceedResponse(4L, 3L))
                .verifyComplete();
    }

    @Test
    void isProceed() {
        StepVerifier.create(
                        vwrController.register("test", 1L)
                                .then(vwrController.proceed("test", 1L))
                                .then(vwrController.isProceed("test", 1L)))
                .expectNext(new ProceedUserResponse(true))
                .verifyComplete();
    }
}