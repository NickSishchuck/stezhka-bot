package com.NickSishchuck.StezhkaBot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Value("${BOT_TOKEN}")
    private String botToken;

    @Value("${BOT_USERNAME}")
    private String botUsername;

    @Bean
    public String botToken() {
        if (botToken == null || botToken.isEmpty()) {
            throw new IllegalStateException("BOT_TOKEN is not set in .env file");
        }
        return botToken;
    }

    @Bean
    public String botUsername() {
        if (botUsername == null || botUsername.isEmpty()) {
            throw new IllegalStateException("BOT_USERNAME is not set in .env file");
        }
        return botUsername;
    }
}