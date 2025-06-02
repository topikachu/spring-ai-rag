package net.topikachu.rag.api;

import lombok.extern.slf4j.Slf4j;
import net.topikachu.rag.service.chat.ChatService;
import net.topikachu.rag.service.etl.EtlPipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.security.Principal;

import static org.springframework.ai.reader.tika.TikaDocumentReader.METADATA_SOURCE;

@RestController()
@RequestMapping("/api/v1")
@Slf4j
public class RestApi {

	@Autowired
	private ChatService chatService;

	@Autowired
	private EtlPipeline etlPipeline;

	@PostMapping(path = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> chat(@RequestBody ChatRequest chatRequest, @RequestParam() String conversationId, Principal principal) {
		var conversationKey = String.format("%s:%s", principal.getName(), conversationId);

		return chatService.stream(chatRequest.userInput, conversationKey)
				.doOnError(exp -> log.error("Error in chat", exp));
	}

	@PostMapping(path = "/index", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> index() {
		return etlPipeline.ingestionFlux()
				.map(document -> (String) document.getMetadata().get(METADATA_SOURCE));
	}

	record ChatRequest(String userInput) implements Serializable {
	}
}
