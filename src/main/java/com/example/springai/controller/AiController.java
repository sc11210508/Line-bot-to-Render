package com.example.springai.controller;

import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;
import org.springframework.http.ResponseEntity;
import com.linecorp.bot.webhook.model.CallbackRequest;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);
    // 用於過濾重複的 Token，避免因 LINE 重試機制導致的 400 錯誤
    private final Set<String> processedTokens = ConcurrentHashMap.newKeySet();

    private final ChatClient coffeeChatClient;
    private final ChatClient codeChatClient;
    private final MessagingApiClient messagingApiClient;

    public AiController(
            ChatClient coffeeChatClient,
            @Qualifier("codeChatClient") ChatClient codeChatClient,
            MessagingApiClient messagingApiClient) {
        this.coffeeChatClient = coffeeChatClient;
        this.codeChatClient = codeChatClient;
        this.messagingApiClient = messagingApiClient;
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody CallbackRequest request) {
        // 1. 立即返回 200 OK，這是最重要的！
        // 這樣 LINE 就不會認為您的服務掛了，也不會觸發重試。
        CompletableFuture.runAsync(() -> {
            request.events().forEach(event -> {
                if (event instanceof MessageEvent messageEvent && messageEvent.message() instanceof TextMessageContent textMessage) {
                    String replyToken = messageEvent.replyToken();
                    
                    // 防重入
                    if (!processedTokens.add(replyToken)) return;

                    try {
                        String text = textMessage.text().trim();
                        if (text.contains("關於糯米橋")) {
                            sendImagemap(replyToken);
                        } else if (text.startsWith("開發:")) {
                            String response = codeChatClient.prompt().user(text.replace("開發:", "")).call().content();
                            replyText(replyToken, response);
                        } else {
                            String response = coffeeChatClient.prompt().user(text).call().content();
                            replyText(replyToken, response);
                        }
                    } catch (Exception e) {
                        log.error("非同步任務執行失敗", e);
                    }
                }
            });
        });
        
        return ResponseEntity.ok("OK");
    }

    private void sendImagemap(String replyToken) {
        String baseUrl = System.getenv().getOrDefault("BASE_URL", "https://line-bot-to-render.onrender.com");

        ImagemapMessage imagemapMessage = new ImagemapMessage(
                null, null,
                URI.create(baseUrl + "/image_v2"),
                "推薦影片",
                new ImagemapBaseSize(1040, 1040),
                List.of(
                        new URIImagemapAction(new ImagemapArea(0, 520, 520, 520), "https://www.xn--2dw500bvka.tw/index.asp", "url1"),
                        new URIImagemapAction(new ImagemapArea(520, 520, 520, 520), "https://m.facebook.com/NuomiBridge/", "url2")
                ),
                new ImagemapVideo(
                        URI.create(baseUrl + "/videopr.mp4"),
                        URI.create(baseUrl + "/background.jpg"),
                        new ImagemapArea(0, 0, 1040, 520),
                        new ImagemapExternalLink(URI.create("https://www.youtube.com/@yannilife8"), "點我看更多")
                )
        );

        messagingApiClient.replyMessage(new ReplyMessageRequest(replyToken, List.of(imagemapMessage), false));
    }

    private void replyText(String replyToken, String text) {
        messagingApiClient.replyMessage(new ReplyMessageRequest(replyToken, List.of(new TextMessage(text)), false));
    }
}
