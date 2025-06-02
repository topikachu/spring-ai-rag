package net.topikachu.rag.ai.model;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.context.annotation.FilterType.REGEX;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration
@ContextConfiguration(classes = {ModelTest.TestConfig.class})
@EnabledIfEnvironmentVariable(named =  "OPENAI_API_KEY", matches = ".+", disabledReason="No OpenAI API key found")
public class ModelTest {
	@Autowired
	private ChatModel chatModel;
	@Autowired
	private EmbeddingModel embeddingModel;

	@Test
	public void testChatComplete() {
		var prompt = Prompt.builder().content("hello, how are you today?").build();
		var chatResponse = chatModel.call(prompt);
		assertThat(chatResponse.getResult().getOutput().getText()).isNotEmpty();
	}

	@Test
	public void testEmbedding() {
		var embedding = embeddingModel.embed("Hello, World!");
		assertThat(embedding).hasSize(1024);

	}

	@ComponentScan(excludeFilters = {
			@ComponentScan.Filter(type = REGEX, pattern = "net\\.topikachu\\.rag\\..*")
	})
	static class TestConfig {

	}


}
