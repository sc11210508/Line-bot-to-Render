package com.example.springai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@Slf4j
public class StreamingAiController {

    private final ChatClient chatClient;

    // 顯式指定注入 coffeeChatClient
    public StreamingAiController(@Qualifier("coffeeChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String prompt) {
        return chatClient.prompt().user(prompt).stream().content();
    }

    @GetMapping(value = "/chat/stream/system", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreamWithSystem(
            @RequestParam String prompt,
            @RequestParam(defaultValue = "你是一個友善且專業的 AI 助手") String system) {
        return chatClient.prompt().system(system).user(prompt).stream().content();
    }

    @GetMapping(value = "/chat/stream/custom", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreamCustom(@RequestParam String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content()
                .map(content -> "🤖 AI: " + content)
                .filter(content -> content != null && !content.trim().isEmpty());
    }
}