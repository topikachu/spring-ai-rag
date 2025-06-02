package net.topikachu.rag.service.etl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class DocReader implements DocumentReader {

	@Value("${input.directory}")
	private String inputDir;

	@Value("${input.filename.glob:*.txt}")
	private String pattern;

	public Flux<Path> scanDirectory() {
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

		return Flux.using(
				() -> Files.walk(Paths.get(inputDir)),
				stream ->
						Flux.fromStream(
								stream
										.filter(Files::isRegularFile)
										.filter(path -> matcher.matches(path.getFileName()))
						),
				Stream::close
		).subscribeOn(Schedulers.boundedElastic());
	}

	public Flux<Document> getDocuments() {
		return scanDirectory()
				.flatMap(path -> Flux.fromIterable(new TikaDocumentReader(path.toUri().toString()).get()));
	}

	// Keep the original get() method for backward compatibility
	@Override
	public List<Document> get() {
		return getDocuments().collectList().block();
	}
}
