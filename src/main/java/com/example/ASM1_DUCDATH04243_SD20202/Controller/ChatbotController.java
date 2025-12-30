package com.example.ASM1_DUCDATH04243_SD20202.Controller;

import com.example.ASM1_DUCDATH04243_SD20202.Service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public class ChatbotController {

    @Autowired
    private AIService aiService;

    // Sửa record để nhận thêm studentId (kiểu Integer khớp với model)
    public record ChatRequest(String message, Integer studentId) {}

    @PostMapping("/chat")
    @ResponseBody
    public CompletableFuture<ResponseEntity<Map<String, String>>> handleChatMessage(@RequestBody ChatRequest chatRequest) {
        String userMessage = chatRequest.message();
        Integer studentId = chatRequest.studentId(); // Lấy studentId từ request

        // Truyền cả 2 tham số vào service
        return aiService.getChatResponse(userMessage, studentId).thenApply(aiResponse -> {
            Map<String, String> response = Map.of("reply", aiResponse);
            return ResponseEntity.ok(response);
        });
    }
    public record SystemChatRequest(String message, String context) {}

    @PostMapping("/chat-system")
    @ResponseBody
    public CompletableFuture<ResponseEntity<Map<String, String>>> handleSystemChatMessage(@RequestBody SystemChatRequest chatRequest) {
        String userMessage = chatRequest.message();
        String context = chatRequest.context();

        return aiService.getSystemChatResponse(userMessage, context).thenApply(aiResponse -> {
            Map<String, String> response = Map.of("reply", aiResponse);
            return ResponseEntity.ok(response);
        });
    }
}