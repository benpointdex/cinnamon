package com.henry.cinnamon.config;

import com.henry.cinnamon.services.DejaCodeMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider dejaCodeTools(DejaCodeMcpTools dejaCodeMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(dejaCodeMcpTools)
                .build();
    }
}
