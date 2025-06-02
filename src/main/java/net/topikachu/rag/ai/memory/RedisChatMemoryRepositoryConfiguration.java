package net.topikachu.rag.ai.memory;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.io.IOException;
import java.util.List;

import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

@Configuration(proxyBeanMethods = false)
public class RedisChatMemoryRepositoryConfiguration {

	@Bean
	public RedisChatMemoryRepository redisChatMemoryRepository(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Message> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		var serializer = new GenericJackson2JsonRedisSerializer(createObjectMapper());
		template.setValueSerializer(serializer);
		template.afterPropertiesSet();
		return new RedisChatMemoryRepository(template);
	}



	ObjectMapper createObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(Message.class, new MessageJsonSerializer());
		simpleModule.addDeserializer(UserMessage.class, new JsonDeserializer<>() {
			@Override
			public UserMessage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
				p.nextToken();
				var content = p.getValueAsString();
				p.nextToken();

				return UserMessage.builder()
						.text(content)
						.build();
			}
		});
		simpleModule.addDeserializer(AssistantMessage.class, new JsonDeserializer<>() {
			@Override
			public AssistantMessage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
				p.nextToken();
				var content = p.getValueAsString();
				p.nextToken();

				return new AssistantMessage(content);

			}
		});
		simpleModule.addDeserializer(SystemMessage.class, new JsonDeserializer<>() {
			@Override
			public SystemMessage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
				p.nextToken();
				var content = p.getValueAsString();
				p.nextToken();

				return SystemMessage.builder()
						.text(content)
						.build();
			}
		});
		simpleModule.addDeserializer(ToolResponseMessage.class, new JsonDeserializer<>() {
			@Override
			public ToolResponseMessage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
				p.nextToken();
				p.nextToken();
				return new ToolResponseMessage(List.of());
			}
		});
		objectMapper.registerModule(simpleModule);
		objectMapper.setDefaultTyping(
				new ObjectMapper.DefaultTypeResolverBuilder(DefaultTyping.EVERYTHING, objectMapper.getPolymorphicTypeValidator())
						.init(JsonTypeInfo.Id.CLASS, null)
						.inclusion(JsonTypeInfo.As.PROPERTY));
		return objectMapper;
	}


	static class MessageJsonSerializer extends JsonSerializer<Message> {
		@Override
		public void serialize(Message value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			var content = value.getText();
			gen.writeStringField("content", content);
		}

		@Override
		public void serializeWithType(Message value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
			WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, JsonToken.START_OBJECT));
			this.serialize(value, g, provider);
			typeSer.writeTypeSuffix(g, typeIdDef);
		}
	}
}
