package com.NickSishchuck.StezhkaBot.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    private final Dotenv dotenv;

    public BotConfig() {
        this.dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();
    }

    @Bean
    public String botToken() {
        String token = dotenv.get("BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("BOT_TOKEN is not set in .env file");
        }
        return token;
    }

    @Bean
    public String botUsername() {
        String username = dotenv.get("BOT_USERNAME");
        if (username == null || username.isEmpty()) {
            throw new IllegalStateException("BOT_USERNAME is not set in .env file");
        }
        return username;
    }
}