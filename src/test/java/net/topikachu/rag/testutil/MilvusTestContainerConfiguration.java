package net.topikachu.rag.testutil;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.milvus.MilvusContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class MilvusTestContainerConfiguration {
	@Bean
	@ServiceConnection
	MilvusContainer milvusContainer() {

		return new MilvusContainer(DockerImageName.parse("milvusdb/milvus:latest"));
	}
}
