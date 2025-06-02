package net.topikachu.rag;

import net.topikachu.rag.testutil.MilvusTestContainerConfiguration;
import net.topikachu.rag.testutil.RedisTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import({
		MilvusTestContainerConfiguration.class,
		RedisTestContainerConfiguration.class
}
)
@SpringBootTest(properties = {"input.directory=test"})
class RagApplicationTests {

	@Test
	void contextLoads() {
	}

}
