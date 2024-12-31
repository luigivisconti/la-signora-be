package com.example.signora;


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

import java.util.HashMap;
import java.util.Map;

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
    }

    private HttpEntity<Map<String, Object>> createHttpEntity(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + azureOpenAiApiKey);
        return new HttpEntity<>(body, headers);
    }


private String createPrompt(String userInput, String language) {
    String personalityIntro = "La Signora è un'entità AI enigmatica che risponde poeticamente in base alla lingua dell'utente.";
    String languageInstruction = language != null && !language.isEmpty()
            ? "Rispondi in " + language + "."
            : "Rileva la lingua dell'input e rispondi nella stessa lingua.";

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
