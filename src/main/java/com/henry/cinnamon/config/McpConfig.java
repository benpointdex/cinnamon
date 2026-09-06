package com.henry.cinnamon.config;

import com.henry.cinnamon.services.CinnamonMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider cinnamonTools(CinnamonMcpTools cinnamonMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(cinnamonMcpTools)
                .build();
    }
}
