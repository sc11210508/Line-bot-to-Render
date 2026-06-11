package com.example.springai.controller;

import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;
import com.linecorp.bot.webhook.model.CallbackRequest;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class AiController {

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
    public void handleCallback(@RequestBody CallbackRequest request) {
        request.events().forEach(event -> {
            if (event instanceof MessageEvent messageEvent && messageEvent.message() instanceof TextMessageContent textMessage) {
                String replyToken = messageEvent.replyToken();
                String text = textMessage.text().trim();

                // 使用非同步處理，避免阻塞 LINE Webhook 連線
                CompletableFuture.runAsync(() -> {
                    try {
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
                        e.printStackTrace(); // 確保錯誤有記錄
                    }
                });
            }
        });
    }

    private void sendImagemap(String replyToken) {
        // 重要：在 Render 部署時，建議將此 URL 改為環境變數
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
