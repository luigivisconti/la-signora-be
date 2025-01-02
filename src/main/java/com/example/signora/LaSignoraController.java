package com.example.signora;


import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.BinaryOperator;

@RestController
@RequestMapping("/api")
class LaSignoraController {

    @Value("${azure.openai.endpoint}")
    private String azureOpenAiEndpoint;

    @Value("${azure.openai.api-key}")
    private String azureOpenAiApiKey;

    @Value("${azure.openai.model}")
    private String azureOpenAiModel;

    @Value("${azure.openai.api-version}")
    private String azureOpenAiApiVersion;

    @PostMapping("/generate-response")
    public ResponseEntity<?> generateResponse(@RequestBody UserInput input) {
        String prompt = createPrompt(input.getUserInput(), input.getLanguage());
        String response = callAzureOpenAI(prompt);
        return ResponseEntity.ok(Map.of("response", response));
    }

    private String callAzureOpenAI(String prompt) {
        OpenAIClient client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential("7uKwiX0mjh515fXLM0LdSbUWsldy7uiQ9MWvM05O9AuymCI4IaqwJQQJ99BAACHYHv6XJ3w3AAAAACOG7U6l"))
                .endpoint("https://ai-lgvscnt3899ai857341216772.services.ai.azure.com/")
                // .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a genius alien."));
        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage(prompt));

        ChatCompletions chatCompletions = client.getChatCompletions("gpt-4o-mini",
                new ChatCompletionsOptions(chatMessages));

        System.out.printf("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
        String content = "";
        for (ChatChoice choice : chatCompletions.getChoices()) {
            ChatResponseMessage message = choice.getMessage();
            System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
            System.out.println("Message:");
            System.out.println(message.getContent());
            content = message.getContent();
        }
        return content;

/*
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 100);
        requestBody.put("temperature", 0.7);

        String url = String.format("%s/openai/deployments/%s/completions?api-version=%s",
                azureOpenAiEndpoint, azureOpenAiModel, azureOpenAiApiVersion);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    createHttpEntity(requestBody),
                    Map.class
            );

            Map<?, ?> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                return ((Map<String, String>) ((Map<?, ?>) responseBody.get("choices")).get(0)).get("text");
            } else {
                return "Errore: nessuna risposta valida da OpenAI.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Errore nella connessione all'API.";
        }

 */
    }

    private HttpEntity<String> createHttpEntity(Map<String, Object> body) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + azureOpenAiApiKey);
        return new HttpEntity<>(new ObjectMapper().writeValueAsString(body), headers);
    }


private String createPrompt(String userInput, String language) {
    String personalityIntro = "La Signora è un'entità AI enigmatica che risponde poeticamente in base alla lingua dell'utente.";
    /*
    String languageInstruction = language != null && !language.isEmpty()
            ? "Rispondi in " + language + "."
            : "Rileva la lingua dell'input e rispondi prevalentamente nella stessa lingua ma utilizza sempre delle parole in spagnolo e in inglese ogni tanto nelle risposte.";
         */
    String languageInstruction =  "Rileva la lingua dell'input e rispondi prevalentamente nella stessa lingua ma utilizza spesso delle parole in spagnolo e in inglese  nelle risposte: diciamo mediamente ogni 10 parole ce ne sono 3 in spagnolo, 1 in inglese e 6 nella lingua dell'input";
    return String.format("%s\n%s\nInput utente: %s\nRisposta:", personalityIntro, languageInstruction, userInput);
}

}

class UserInput {
    private String userInput;
    private String language;

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
