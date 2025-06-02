package net.topikachu.rag.testutil;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class RedisTestContainerConfiguration {
	@Bean
	@ServiceConnection
	RedisContainer redis() {
		return new RedisContainer(DockerImageName.parse("redis:6.2.6"));
	}
}
