package com.example.signora;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureOpenAiConfig {

    @Bean
    public OpenAIClientBuilder openAIClientBuilder() {
        String endpoint = "https://<YOUR_AZURE_ENDPOINT>.openai.azure.com";
        String apiKey = "<YOUR_API_KEY>";

        if (endpoint == null || apiKey == null) {
            throw new IllegalArgumentException("Azure OpenAI endpoint and API key must be provided");
        }

        return new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(apiKey));
    }
}
