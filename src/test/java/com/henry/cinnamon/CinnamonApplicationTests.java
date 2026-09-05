package com.henry.cinnamon;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CinnamonApplicationTests {

	@MockitoBean
	private EmbeddingModel embeddingModel;

	@Test
	void contextLoads() {
	}
}
