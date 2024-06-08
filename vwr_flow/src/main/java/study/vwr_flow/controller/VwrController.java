package study.vwr_flow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import study.vwr_flow.controller.dto.CreateVwrResponse;
import study.vwr_flow.controller.dto.ProceedResponse;
import study.vwr_flow.controller.dto.ProceedUserResponse;
import study.vwr_flow.service.VwrService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vwr")
public class VwrController {
    private final VwrService vwrService;

    @PostMapping
    public Mono<CreateVwrResponse> register(@RequestParam(required = false) final String queueName,
                                            @RequestParam final Long userId) {
        return vwrService.registerVwrQueue(queueName, userId)
                .map(CreateVwrResponse::new);
    }

    @PostMapping("/proceed")
    public Mono<ProceedResponse> proceed(@RequestParam(required = false) final String queueName,
                                         @RequestParam final long count) {
        return vwrService.proceedVwrQueue(queueName, count)
                .map(proceedCount -> new ProceedResponse(count, proceedCount));
    }

    @GetMapping("/proceed")
    public Mono<ProceedUserResponse> isProceed(@RequestParam(required = false) final String queueName,
                                               @RequestParam final Long userId) {
        return vwrService.isProceed(queueName, userId)
                .map(ProceedUserResponse::new);
    }
}
