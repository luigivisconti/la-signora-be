package com.example.signora;


import com.azure.ai.openai.models.*;
import org.springframework.beans.factory.annotation.Autowired;
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
       co.setStop(Collections.singletonList("###"));

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

        return processResponse(content);

    }

    public static String processResponse(String content) {
        // Controlla se la risposta termina con '***'
        if (content.trim().endsWith("***")) {
            return content.substring(0, content.length() - 3).trim();
        }

        // Regex per trovare l'ultimo punto, punto esclamativo o interrogativo
        String regex = "(?s)(.*?[.!?])[^.!?]*$";

        // Usa il pattern per troncare il contenuto
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(content);

        if (matcher.matches()) {
            // Restituisci tutto il contenuto fino all'ultimo punto, punto esclamativo o interrogativo
            return matcher.group(1).trim();
        }

        // Se non ci sono segni di punteggiatura, restituisci una stringa vuota
        return content;
    }

private String createPrompt(String userInput, String language) {
    String personalityIntro = "La Signora è un'entità AI enigmatica che risponde poeticamente in base alla lingua dell'utente.";
    /*
    String languageInstruction = language != null && !language.isEmpty()
            ? "Rispondi in " + language + "."
            : "Rileva la lingua dell'input e rispondi prevalentamente nella stessa lingua ma utilizza sempre delle parole in spagnolo e in inglese ogni tanto nelle risposte.";
         */
    String languageInstruction =  "Rileva la lingua dell'input e rispondi prevalentamente nella stessa lingua ma utilizza spesso delle parole in spagnolo e in inglese  nelle risposte: diciamo mediamente ogni 10 parole ce ne sono 3 in spagnolo, 1 in inglese e 6 nella lingua dell'input. " +
            "Non superare una lunghezza che si interrompa nel mezzo di una frase. Concludi sempre la risposta con tre asterischi così che sappia quando è finita";
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
