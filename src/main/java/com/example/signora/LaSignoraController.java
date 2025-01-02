package com.example.signora;


import com.azure.ai.openai.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
class LaSignoraController {

    @Autowired
    ClientBuilder clientBuilder;

    @PostMapping("/generate-response")
    public ResponseEntity<?> generateResponse(@RequestBody UserInput input) {
        String prompt = createPrompt(input.getUserInput(), input.getLanguage());
        String response = callAzureOpenAI(prompt);
        return ResponseEntity.ok(Map.of("response", response));
    }

    private String callAzureOpenAI(String prompt) {

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a genius alien."));
        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage(prompt));
       ChatCompletionsOptions co = new ChatCompletionsOptions(chatMessages);
       co.setMaxTokens(100);
       ChatCompletions chatCompletions = clientBuilder.getClient().getChatCompletions("gpt-4o-mini",
                co);

        System.out.printf("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
        String content = "";
        ChatChoice choice = chatCompletions.getChoices().get(0);
            ChatResponseMessage message = choice.getMessage();
            System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
            System.out.println("Message:");
            System.out.println(message.getContent());
            content = message.getContent();

        return content;

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
