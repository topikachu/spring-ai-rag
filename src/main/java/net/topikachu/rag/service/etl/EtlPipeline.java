package net.topikachu.rag.service.etl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Component
@Slf4j
public class EtlPipeline {

	private final DocReader documentReader;
	private final VectorStore vectorStore;
	private final TextSplitter textSplitter;

	public EtlPipeline(VectorStore vectorStore,
	                   TextSplitter textSplitter,
	                   DocReader documentReader) {
		this.vectorStore = vectorStore;
		this.textSplitter = textSplitter;
		this.documentReader = documentReader;
	}

	public Flux<Document> ingestionFlux() {
		return documentReader.getDocuments()
				.flatMap(document -> {
					var processChunks = Mono.fromRunnable(() -> {
						var chunks = textSplitter.apply(List.of(document));
						vectorStore.write(chunks); // expensive operation
					}).subscribeOn(Schedulers.boundedElastic());

					return Flux.concat(
							Flux.just(document),
							processChunks.then(Mono.empty()) // suppress downstream emission
					);


				})

				.doOnComplete(() -> log.info("RunIngestion() finished"))
				.doOnError(e -> log.error("Error during ingestion", e));

	}

	// Keep the original ingestion() method for backward compatibility
	public List<Document> ingestion() {
		return ingestionFlux().collectList().block();
	}


}
