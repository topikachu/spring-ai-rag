package net.topikachu.rag.ai.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MessageJacksonTest {

	@Test
	public void testSerialize() throws JsonProcessingException {
		var objectMapper = new RedisChatMemoryRepositoryConfiguration().createObjectMapper();
		var json = objectMapper.writeValueAsString(
				List.of(
						new UserMessage("This is a user message"),
						new AssistantMessage("This is a assistant message"),
						new SystemMessage("This is a system message"),
						new ToolResponseMessage(List.of())));
		var list = objectMapper.readValue(json, List.class);
		assertThat(list).hasSize(4);
		assertThat(list.get(0)).isInstanceOfSatisfying(UserMessage.class, userMessage -> {
			assertThat(userMessage.getText()).isEqualTo("This is a user message");
		});
		assertThat(list.get(1)).isInstanceOfSatisfying(AssistantMessage.class, assistantMessage -> {
			assertThat(assistantMessage.getText()).isEqualTo("This is a assistant message");
		});
		assertThat(list.get(2)).isInstanceOfSatisfying(SystemMessage.class, systemMessage -> {
			assertThat(systemMessage.getText()).isEqualTo("This is a system message");
		});
		assertThat(list.get(3)).isInstanceOf(ToolResponseMessage.class);
	}

}
