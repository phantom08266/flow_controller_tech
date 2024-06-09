package study.vwr_flow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class VwrFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(VwrFlowApplication.class, args);
    }

}
