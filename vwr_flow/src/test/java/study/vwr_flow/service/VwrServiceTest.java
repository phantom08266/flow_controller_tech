package study.vwr_flow.service;

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
import reactor.test.StepVerifier;
import study.vwr_flow.config.EmbeddedRedisConfig;
import study.vwr_flow.controller.VwrController;

@SpringBootTest
@Import(EmbeddedRedisConfig.class)
@ActiveProfiles("test")
class VwrServiceTest {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Autowired
    private VwrService vwrService;

    @BeforeEach
    void setUp() {
        // 모든 키 삭제
        ReactiveRedisConnection reactiveConnection = redisTemplate.getConnectionFactory().getReactiveConnection();
        reactiveConnection.serverCommands().flushAll().subscribe();

        // 특정 키 삭제
        // redisTemplate.opsForZSet().delete("user:queue:test:wait").subscribe();
    }

    @Test
    void generateTokenTest() {
        StepVerifier.create(vwrService.generateToken("test", 1L))
                .expectNext("fdf2efb0a1590a27641401016db64afa70a886686a6d6e96e4644eea3e65c3d1")
                .verifyComplete();
    }

    @Test
    void test() {
        StepVerifier.create(vwrService.isProceedByToken("test", 1L, "fdf2efb0a1590a27641401016db64afa70a886686a6d6e96e4644eea3e65c3d1"))
                .expectNext(true)
                .verifyComplete();
    }
}