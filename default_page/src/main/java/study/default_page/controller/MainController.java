package study.default_page.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Arrays;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class MainController {

    private RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/")
    public String main(@RequestParam(required = false, defaultValue = "default") String queueName,
                       @RequestParam Long userId,
                       HttpServletRequest request
    ) {

        String tokenKey = "user-queue-%s-%d".formatted(queueName, userId);
        Cookie cookie = Arrays.stream(request.getCookies()).filter(item -> item.getName().equals(tokenKey)).findFirst()
                .orElse(null);

        String token = "";
        if (cookie != null) {
            token = cookie.getValue();
        }
        // String queue = "user-queue-%s-%d".formatted(queueName, userId);
        URI requestUri = UriComponentsBuilder
                .fromUriString("http://localhost:9010")
                .path("/api/v1/vwr/proceed-token")
                .queryParam("userId", userId)
                .queryParam("queueName", queueName)
                .queryParam("token", token)
                .encode()
                .build()
                .toUri();

        ResponseEntity<UserWaitResponse> forEntity = restTemplate.getForEntity(requestUri, UserWaitResponse.class);
        if (forEntity.getBody() == null || !forEntity.getBody().isProceed) {
            return "redirect:http://localhost:9010/waiting-room?queueName=%s&userId=%d&redirectUrl=%s"
                    .formatted(queueName, userId, "http://localhost:9000?userId=%d".formatted(userId));
        }

        return "index";
    }

    public record UserWaitResponse(
            boolean isProceed
    ) {
    }
}
