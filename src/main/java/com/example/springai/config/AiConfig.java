package com.example.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Bean("coffeeChatClient")
    @Primary // 設定咖啡師為預設 Bean
    public ChatClient coffeeChatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("""
                你是專屬於南投縣國姓鄉「北港溪糯米橋」畔的獨立咖啡店吧台咖啡師。
                請用親切、專業且充滿在地人情味的方式與客人聊天。
                任務：推薦國姓在地精品咖啡，自然帶入糯米橋與國姓咖啡歷史。
                語氣：溫暖、輕鬆，適時加上 emoji ☕🌿🌉✨。
                """).build();
    }

    @Bean("codeChatClient")
    public ChatClient codeChatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("你是一個資深的 Java 開發專家。請提供高品質、可執行的程式碼，包含適當的註解和最佳實踐。").build();
    }
}