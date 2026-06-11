package com.example.springai.controller;

import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;
import com.linecorp.bot.webhook.model.CallbackRequest;
import com.linecorp.bot.messaging.model.URIImagemapAction.*;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

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

                if (text.contains("關於糯米橋")) {
                    sendImagemap(replyToken);
                } else if (text.startsWith("開發:")) {
                    String response = codeChatClient.prompt().user(text.replace("開發:", "")).call().content();
                    replyText(replyToken, response);
                } else {
                    String response = coffeeChatClient.prompt().user(text).call().content();
                    replyText(replyToken, response);
                }
            }
        });
    }

    private void sendImagemap(String replyToken) {
        String baseUrl = "https://line-bot-to-render.onrender.com";

        ImagemapMessage imagemapMessage = new ImagemapMessage(
                null,
                null,
                URI.create(baseUrl + "/image_v2"), // 確保加上了正確的副檔名與斜線
                "推薦影片",
                new ImagemapBaseSize(1040, 1040),
                // 將所有 Action 放在同一個 List 裡面
                List.of(
                         new URIImagemapAction(
                                new ImagemapArea(0, 520, 520, 520),
                                "https://www.xn--2dw500bvka.tw/index.asp",
                                 "url1"

                        ),
                        new URIImagemapAction(
                                new ImagemapArea(520, 520, 520, 520),
                                "https://m.facebook.com/NuomiBridge/",
                                "url2"
                        )
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
