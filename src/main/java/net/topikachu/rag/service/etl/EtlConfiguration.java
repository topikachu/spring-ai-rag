package net.topikachu.rag.service.etl;

import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)

public class EtlConfiguration {
	@Bean
	TextSplitter textSplitter() {
		return new TokenTextSplitter();
	}

	@Bean
	BatchingStrategy singleDocumentBatchingStrategy() {
		return documents -> documents.stream().map(List::of).toList();
	}


}
