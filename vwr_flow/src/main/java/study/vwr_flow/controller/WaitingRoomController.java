package study.vwr_flow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import study.vwr_flow.service.VwrService;

@Controller
@RequiredArgsConstructor
public class WaitingRoomController {
    private final VwrService vwrService;


    @GetMapping("waiting-room")
    public Mono<?> waitingRoom(@RequestParam(required = false, defaultValue = "default") String queueName,
                               @RequestParam Long userId,
                               @RequestParam String redirectUrl) {

        return vwrService.isProceed(queueName, userId)
                .filter(isProceed -> isProceed)
                .flatMap(result -> Mono.just(Rendering.redirectTo(redirectUrl).build()))
                .switchIfEmpty(
                        vwrService.registerVwrQueue(queueName, userId)
                                .onErrorResume(ex -> vwrService.getRank(queueName, userId))
                                .map(rank ->
                                        Rendering.view("waiting-room")
                                                .modelAttribute("number", rank)
                                                .modelAttribute("queueName", queueName)
                                                .modelAttribute("userId", userId)
                                                .build()
                                ));
    }
}