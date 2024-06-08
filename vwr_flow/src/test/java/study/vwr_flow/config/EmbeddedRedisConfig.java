package study.vwr_flow.config;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

@TestConfiguration
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    public EmbeddedRedisConfig() throws IOException {
        this.redisServer = new RedisServer(63790);
        this.redisServer.start();
    }


    @PreDestroy
    public void stopRedis() throws IOException {
        if (this.redisServer != null) {
            this.redisServer.stop();
        }
    }
}
