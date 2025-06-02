package net.topikachu.rag.service.chat;

import net.topikachu.rag.testutil.MilvusTestContainerConfiguration;
import net.topikachu.rag.testutil.RedisTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.REGEX;

@EnableAutoConfiguration
@Import({MilvusTestContainerConfiguration.class, RedisTestContainerConfiguration.class})

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = {"spring.ai.vectorstore.milvus.initialize-schema=true"})
@ContextConfiguration(classes = {ChatServiceTest.TestConfig.class})
class ChatServiceTest {
	@Autowired
	private ChatService chatService;
	@Autowired
	private VectorStore vectorStore;
	@MockitoBean
	private ChatModel chatModel;
	@MockitoBean
	private EmbeddingModel embeddingModel;

	@Test
	void chat() {

		var promptArgumentCaptor = ArgumentCaptor.forClass(Prompt.class);
		var chatRepose = ChatResponse.builder()
				.generations(List.of(new Generation(new AssistantMessage("Bamboo is a common food for pandas."))))
				.build();

		when(chatModel.call(promptArgumentCaptor.capture())).thenReturn(chatRepose);
		Random random = new Random();
		float[] randomEmbedding = new float[1024];
		for (int i = 0; i < randomEmbedding.length; i++) {
			randomEmbedding[i] = random.nextFloat(); // Generates random float between 0.0 and 1.0
		}

		when(embeddingModel.dimensions()).thenReturn(1024);
		when(embeddingModel.embed(anyString())).thenReturn(randomEmbedding);

		when(embeddingModel.embed(anyList(), any(EmbeddingOptions.class), any(BatchingStrategy.class))).thenAnswer(invocation ->
		{
			var documents = (List) invocation.getArgument(0);
			return documents.stream()
					.map(notused -> randomEmbedding)
					.toList();
		});
		Document document = Document.builder()
				.text("Lili is a panda")
				.build();

		vectorStore.add(List.of(document));
		var ragResponse = chatService.call("What does Lili usually eat?", "test-conversation-id");
		assertThat(promptArgumentCaptor.getValue().getContents())
				.contains("Lili is a panda");
		assertThat(ragResponse.getResults()).first().isInstanceOfSatisfying(Generation.class, generation -> {
			assertThat(generation.getOutput().getText()).isEqualTo("Bamboo is a common food for pandas.");
		});


	}

	@ComponentScan(excludeFilters = {
			@ComponentScan.Filter(type = REGEX, pattern = {
					"net\\.topikachu\\.rag\\.api\\..*",
					"net\\.topikachu\\.rag\\.service\\.elt\\..*"})
	})
	@EnableAutoConfiguration
	static class TestConfig {

	}
}
