package study.vwr_flow.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import study.vwr_flow.exception.VwrServiceErrorType;

@Service
@RequiredArgsConstructor
public class VwrService {
    private static final String USER_QUEUE = "user:queue:%s:wait";
    private static final String USER_PROCEED_QUEUE = "user:queue:%s:proceed";

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public Mono<Long> registerVwrQueue(final String queueName, final Long userId) {
        long timestamp = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE.formatted(queueName), userId.toString(), timestamp)
                .filter(item -> item)
                .switchIfEmpty(Mono.error(VwrServiceErrorType.ALREADY_ADD_VWR.build()))
                .flatMap(item -> reactiveRedisTemplate.opsForZSet()
                        .rank(USER_QUEUE.formatted(queueName), userId.toString()))
                .map(rank -> rank >= 0 ? rank + 1 : 0L);
    }

    // 5개의 요청이 들어오면 뽑아낼수 있는 갯수만큼 뽑아내서 proceed 키에 저장한다.
    public Mono<Long> proceedVwrQueue(final String queueName, final long count) {
        return reactiveRedisTemplate.opsForZSet().popMin(USER_QUEUE.formatted(queueName), count)
                .flatMap(user -> reactiveRedisTemplate.opsForZSet().add(USER_PROCEED_QUEUE.formatted(queueName),
                        Objects.requireNonNull(user.getValue()), Instant.now().getEpochSecond()))
                .count();
    }

    // proceed한 키인지 확인한다.
    public Mono<Boolean> isProceed(final String queueName, final Long userId) {

        return reactiveRedisTemplate.opsForZSet().rank(USER_PROCEED_QUEUE.formatted(queueName), userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0);
    }

    public Mono<Boolean> isProceedByToken(final String queueName, final Long userId, final String token) {
        isEnable(queueName, userId, token);
        return generateToken(queueName, userId)
                .filter(expectToken -> expectToken.equalsIgnoreCase(token))
                .map(expectToken -> true)
                .defaultIfEmpty(false);
    }

    public Mono<Long> getRank(String queueName, Long userId) {
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE.formatted(queueName), userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0 ? rank + 1 : rank);
    }

    public boolean isEnable(String queueName, Long userId, String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = "user-queue-%s-%d".formatted(queueName, userId);
            byte[] byteData = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : byteData) {
                sb.append(String.format("%02x", b));
            }
            boolean result = sb.toString().equals(token);
            System.out.println(result);
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    public Mono<String> generateToken(String queueName, Long userId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = "user-queue-%s-%d".formatted(queueName, userId);
            byte[] byteData = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : byteData) {
                sb.append(String.format("%02x", b));
            }
            return Mono.just(sb.toString());
        } catch (Exception e) {
            return Mono.error(VwrServiceErrorType.ALREADY_ADD_VWR.build());
        }
    }
}
