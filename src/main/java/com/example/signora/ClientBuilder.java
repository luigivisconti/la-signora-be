package com.example.signora;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class ClientBuilder {

    @Value("${azure.openai.endpoint}")
    private String azureOpenAiEndpoint;
    @Value("${azure.openai.api-key}")
    private String azureOpenAiApiKey;
    @Value("${azure.openai.model}")
    private String azureOpenAiModel;
    @Value("${azure.openai.api-version}")
    private String azureOpenAiApiVersion;
    private final OpenAIClient client;

    public ClientBuilder(){
        OkHttpAsyncHttpClientBuilder httpClientBuilder = new OkHttpAsyncHttpClientBuilder()
                .connectionPool(new okhttp3.ConnectionPool(10, 5, TimeUnit.MINUTES)) // Max 10 connessioni, timeout di 5 minuti
                .readTimeout(Duration.ofSeconds(30)) // Timeout di lettura di 30 secondi
                .writeTimeout(Duration.ofSeconds(30)); // Timeout di scrittura di 30 secondi

        this.client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential("7uKwiX0mjh515fXLM0LdSbUWsldy7uiQ9MWvM05O9AuymCI4IaqwJQQJ99BAACHYHv6XJ3w3AAAAACOG7U6l"))
                .endpoint("https://ai-lgvscnt3899ai857341216772.services.ai.azure.com/")
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .httpClient(httpClientBuilder.build())
                .buildClient();

    }

    public OpenAIClient getClient(){
        return client;
    }

}
