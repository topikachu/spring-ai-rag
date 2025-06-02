package net.topikachu.rag.ai.memory;

import net.topikachu.rag.testutil.MilvusTestContainerConfiguration;
import net.topikachu.rag.testutil.RedisTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.context.annotation.FilterType.REGEX;

@EnableAutoConfiguration
@SpringBootTest(classes = {RedisTestContainerConfiguration.class, MilvusTestContainerConfiguration.class},
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {RedisChatMemoryRepositoryTest.TestConfig.class})
class RedisChatMemoryRepositoryTest {
	@Autowired
	RedisChatMemoryRepository redisChatMemoryRepository;

	@Test
	void testRedisChatMemoryRepository() {
		redisChatMemoryRepository.saveAll("test", List.of(new UserMessage("userMessage"), new AssistantMessage("assistantMessage"), new SystemMessage("systemMessage"), new ToolResponseMessage(List.of())));
		var messages = redisChatMemoryRepository.findByConversationId("test");
		assertThat(messages).hasSize(4);
	}

	@ComponentScan(excludeFilters = {
			@ComponentScan.Filter(type = REGEX, pattern = {
					"net\\.topikachu\\.rag\\.api\\..*",
					"net\\.topikachu\\.rag\\.service\\..*"})
	})
	@EnableAutoConfiguration
	static class TestConfig {

	}
}
