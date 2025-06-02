package net.topikachu.rag.service.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
@Slf4j
public class ChatService {
	@Autowired
	private ChatModel chatModel;
	@Autowired
	private VectorStore vectorStore;

	@Autowired
	private ChatMemory chatMemory;

	public ChatClient.ChatClientRequestSpec input(String userInput, String conversationId) {
		return ChatClient.builder(chatModel)
				.build().prompt()
				.advisors(
						new QuestionAnswerAdvisor(vectorStore),
						MessageChatMemoryAdvisor.builder(chatMemory).build()
				)
				.advisors(spec -> spec.param(CONVERSATION_ID, conversationId))
				.user(userInput);

	}

	public Flux<String> stream(String userInput, String conversationId) {
		return input(userInput, conversationId)
				.stream().content();
	}

	public ChatResponse call(String userInput, String conversationId) {
		return input(userInput, conversationId)
				.call().chatResponse();
	}


}
