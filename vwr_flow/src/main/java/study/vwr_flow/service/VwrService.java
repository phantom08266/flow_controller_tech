package study.vwr_flow.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import study.vwr_flow.exception.VwrServiceErrorType;

@Service
@RequiredArgsConstructor
public class VwrService {
    private static final String USER_QUEUE = "user:queue:%s:wait";
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public Mono<Long> redisTest(final String queueName, final Long userId) {
        long timestamp = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE.formatted(queueName), userId.toString(), timestamp)
                .filter(item -> item)
                .switchIfEmpty(Mono.error(VwrServiceErrorType.ALREADY_ADD_VWR.build()))
                .flatMap(item -> reactiveRedisTemplate.opsForZSet()
                        .rank(USER_QUEUE.formatted(queueName), userId.toString()))
                .map(rank -> rank >= 0 ? rank + 1 : 0L);
    }
}
