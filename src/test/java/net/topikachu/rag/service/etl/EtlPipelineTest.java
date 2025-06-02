package net.topikachu.rag.service.etl;

import net.topikachu.rag.testutil.MilvusTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.REGEX;

@EnableAutoConfiguration
@Import(MilvusTestContainerConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {"input.directory=src/test/resources/corpus", "spring.ai.vectorstore.milvus.initialize-schema=true"})
@ContextConfiguration(classes = {EtlPipelineTest.TestConfig.class})
class EtlPipelineTest {
	@Autowired
	private EtlPipeline etlPipeline;
	@MockitoBean
	private EmbeddingModel embeddingModel;
	@Autowired
	private VectorStore vectorStore;

	@Test
	void testIngestion() {

		Random random = new Random();
		float[] randomEmbedding = new float[1024];
		for (int i = 0; i < randomEmbedding.length; i++) {
			randomEmbedding[i] = random.nextFloat(); // Generates random float between 0.0 and 1.0
		}
		when(embeddingModel.embed(anyList(), any(EmbeddingOptions.class), any(BatchingStrategy.class))).thenAnswer(invocation ->
		{
			var documents = (List) invocation.getArgument(0);
			return documents.stream()
					.map(notused -> randomEmbedding)
					.toList();
		});
		when(embeddingModel.embed(matches("any search"))).thenReturn(randomEmbedding);

		var results = etlPipeline.ingestion();
		assertThat(results).hasSize(2);

		var documents = vectorStore.similaritySearch("any search");
		assertThat(documents).isNotEmpty();
	}

	@ComponentScan(excludeFilters = {
			@ComponentScan.Filter(type = REGEX, pattern = {
					"net\\.topikachu\\.rag\\.api\\..*"
			}
			)
	})
	@EnableAutoConfiguration
	static class TestConfig {

	}
}
