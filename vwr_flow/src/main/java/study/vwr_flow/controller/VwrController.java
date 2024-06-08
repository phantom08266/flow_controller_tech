package study.vwr_flow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import study.vwr_flow.controller.dto.CreateVwrResponse;
import study.vwr_flow.service.VwrService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vwr")
public class VwrController {
    private final VwrService vwrService;

    @PostMapping("/test")
    public Mono<?> redisTest(@RequestParam final Long userId,
                             @RequestParam(required = false) final String queueName) {
        return vwrService.redisTest(queueName, userId)
                .map(CreateVwrResponse::new);
    }
}
