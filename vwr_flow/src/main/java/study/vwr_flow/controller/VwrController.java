package study.vwr_flow.controller;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import study.vwr_flow.controller.dto.CreateVwrResponse;
import study.vwr_flow.controller.dto.ProceedResponse;
import study.vwr_flow.controller.dto.ProceedUserResponse;
import study.vwr_flow.exception.RankResponse;
import study.vwr_flow.service.VwrService;

@Slf4j
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

    @GetMapping("/proceed-token")
    public Mono<ProceedUserResponse> isProceedByToken(@RequestParam(required = false) final String queueName,
                                                      @RequestParam final Long userId,
                                                      @RequestParam final String token) {
        return vwrService.isProceedByToken(queueName, userId, token)
                .map(ProceedUserResponse::new);
    }

    @GetMapping("/rank")
    public Mono<RankResponse> getRank(@RequestParam(required = false) final String queueName,
                                      @RequestParam final Long userId) {
        return vwrService.getRank(queueName, userId)
                .map(RankResponse::new);
    }

    @GetMapping("/token")
    public Mono<String> generateToken(@RequestParam(required = false) final String queueName,
                                      @RequestParam final Long userId,
                                      ServerWebExchange exchange) {
        return Mono.defer(() -> vwrService.generateToken(queueName, userId))
                .map(token -> {
                    exchange.getResponse().addCookie(
                            ResponseCookie.from("user-queue-%s-%d".formatted(queueName, userId), token)
                                    .maxAge(Duration.ofSeconds(300))
                                    .path("/")
                                    .build()
                    );
                    return token;
                });
    }
}
