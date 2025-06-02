package net.topikachu.rag.ai.memory;

import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SessionCallback;

import java.util.List;

public class RedisChatMemoryRepository implements ChatMemoryRepository {
	private final RedisTemplate<String, Message> chatMemoryRedisTemplate;

	public RedisChatMemoryRepository(RedisTemplate<String, Message> chatMemoryRedisTemplate) {
		this.chatMemoryRedisTemplate = chatMemoryRedisTemplate;
	}

	@Override
	public @NotNull List<String> findConversationIds() {
		try (var cursor = chatMemoryRedisTemplate.scan(ScanOptions.scanOptions()
				.match(generateRedisKey("*"))
				.build())) {
			return cursor.stream()
					.map(key -> key.substring(generateRedisKey("").length()))
					.toList();
		}

	}

	@Override
	public List<Message> findByConversationId(@NotNull String conversationId) {
		return chatMemoryRedisTemplate.opsForList().range(generateRedisKey(conversationId), 0, -1);
//
	}

	@Override
	public void saveAll(@NotNull String conversationId, List<Message> messages) {
//
		var key = generateRedisKey(conversationId);

		chatMemoryRedisTemplate.executePipelined(
				new SessionCallback() {
					@Override
					public Object execute(@NotNull RedisOperations operations) throws DataAccessException {
						operations.delete(key);
						operations.opsForList().rightPushAll(key, messages);
						return null;
					}
				}
		);
	}

	@Override
	public void deleteByConversationId(@NotNull String conversationId) {
		chatMemoryRedisTemplate.delete(generateRedisKey(conversationId));
	}

	private String generateRedisKey(String conversationId) {
		return "chat-memory:" + conversationId;
	}
}
