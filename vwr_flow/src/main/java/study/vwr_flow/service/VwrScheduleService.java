package study.vwr_flow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuples;

@Slf4j
@Service
@RequiredArgsConstructor
public class VwrScheduleService {

    @Value("${scheduler.enable}")
    private boolean enable;

    private static final String USER_QUEUE_KEY_SCAN = "user:queue:*:wait";
    private static final int SCAN_MAX_COUNT = 100;
    private static final int PROCEED_COUNT = 5;

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final VwrService vwrService;

    @Scheduled(initialDelay = 5000, fixedDelay = 3000)
    public void vwrSchedule() {
        if (!enable) return;
        log.info("schedule start ...");

        reactiveRedisTemplate.scan(
                        ScanOptions.scanOptions()
                                .match(USER_QUEUE_KEY_SCAN)
                                .count(SCAN_MAX_COUNT)
                                .build()
                ).map(key -> key.split(":")[2])
                .flatMap(queueName -> vwrService.proceedVwrQueue(queueName, PROCEED_COUNT)
                        .map(proceedCount -> Tuples.of(queueName, proceedCount)))
                .doOnNext(tuple -> log.info("queueName: %s, max_request_count: %d,proceedCount: %d".formatted(tuple.getT1(),
                                PROCEED_COUNT,
                                tuple.getT2())))
                .subscribe();
    }
}
