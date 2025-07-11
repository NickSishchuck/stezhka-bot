package com.NickSishchuck.StezhkaBot.service;

import com.NickSishchuck.StezhkaBot.entity.TextContent;
import com.NickSishchuck.StezhkaBot.repository.TextContentRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

@Service
public class TextContentService {

    private static final Logger logger = LoggerFactory.getLogger(TextContentService.class);

    private final TextContentRepository textContentRepository;
    private final Map<String, String> textCache = new ConcurrentHashMap<>();

    @Autowired
    private AdminNotificationService notificationService;

    @Autowired
    public TextContentService(TextContentRepository textContentRepository) {
        this.textContentRepository = textContentRepository;
    }

    /**
     * Load all text content from database on startup
     */
    @PostConstruct
    public void loadAllTexts() {
        try {
            logger.info("Loading text content from database...");

            List<TextContent> allTexts = textContentRepository.findAll();

            if (allTexts.isEmpty()) {
                logger.warn("No text content found in database. Initializing with default values...");
                return;
            }

            // Load into cache
            for (TextContent content : allTexts) {
                textCache.put(content.getTextKey(), content.getTextValue());
            }

            logger.info("Successfully loaded {} text entries from database", textCache.size());

        } catch (Exception e) {
            logger.error("Failed to load text content from database. Using fallback values.", e);
        }
    }

    /**
     * Get text by key name
     */
    public String getText(String key) {
        String text = textCache.get(key);
        if (text == null) {
            logger.warn("Text key '{}' not found in cache. Returning key as fallback.", key);
            return key; // Return key as fallback
        }
        return text;
    }

    /**
     * Update text content and save to database
     */
    @Transactional
    public boolean updateText(String key, String newValue) {
        try {
            TextContent content = textContentRepository.findByTextKey(key)
                    .orElse(new TextContent(key, newValue, "Updated via bot"));

            content.setTextValue(newValue);
            textContentRepository.save(content);

            // Update cache
            textCache.put(key, newValue);

            logger.info("Successfully updated text key '{}' with new value", key);
            return true;

        } catch (Exception e) {
            logger.error("Failed to update text key '{}'", key, e);
            return false;
        }
    }

    /**
     * Create new text entry
     */
    @Transactional
    public boolean createText(String key, String value, String description) {
        try {
            if (textContentRepository.existsByTextKey(key)) {
                logger.warn("Text key '{}' already exists", key);
                return false;
            }

            TextContent content = new TextContent(key, value, description);
            textContentRepository.save(content);

            // Update cache
            textCache.put(key, value);

            logger.info("Successfully created new text key '{}'", key);
            return true;

        } catch (Exception e) {
            logger.error("Failed to create text key '{}'", key, e);
            return false;
        }
    }

    /**
     * Refresh cache from database
     */
    public void refreshCache() {
        logger.info("Refreshing text content cache...");
        textCache.clear();
        loadAllTexts();
    }

    /**
     * Get all cached texts (for debugging/admin purposes)
     */
    public Map<String, String> getAllTexts() {
        return Map.copyOf(textCache);
    }
}