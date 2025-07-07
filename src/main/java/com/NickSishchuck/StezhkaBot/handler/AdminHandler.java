package com.NickSishchuck.StezhkaBot.handler;

import com.NickSishchuck.StezhkaBot.service.StezhkaBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.NickSishchuck.StezhkaBot.service.TextContentService;
import com.NickSishchuck.StezhkaBot.utils.MenuBuilder;
import com.NickSishchuck.StezhkaBot.utils.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;

@Component
public class AdminHandler implements MenuHandler {

    private static final Logger logger = LoggerFactory.getLogger(StezhkaBotService.class);
    private final TextContentService textContentService;
    private TelegramClient telegramClient;
    private MessageSender messageSender;

    // List of admin user IDs TODO move to db layer
    @Value("${bot.admin.user.ids:}")
    private String adminUserIds;

    @Autowired
    public AdminHandler(TextContentService textContentService) {
        this.textContentService = textContentService;
    }

    @Override
    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.messageSender = new MessageSender(telegramClient);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("admin_") ||
                callbackData.equals("/admin") ||
                callbackData.startsWith("text_edit_") ||
                callbackData.startsWith("text_update_");
    }

    @Override
    public void handle(long chatId, String callbackData) {
        // Check if user is admin
        if (!isAdmin(chatId)) {
            messageSender.sendMessage(chatId, "❌ Access denied. Admin privileges required.",
                    new MenuBuilder().addButton("⬅️ Back", "main").build());
            return;
        }

        switch (callbackData) {
            case "/admin", "admin_main" -> showAdminMenu(chatId);
            case "admin_content" -> showContentManagement(chatId);
            case "admin_refresh" -> refreshContent(chatId);
            case "admin_list_all" -> listAllTexts(chatId);
            default -> {
                if (callbackData.startsWith("text_edit_")) {
                    String key = callbackData.substring("text_edit_".length());
                    showTextEditor(chatId, key);
                }
            }
        }
    }

    private void showAdminMenu(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("📝 Content Management", "admin_content")
                .addButton("🔄 Refresh Cache", "admin_refresh")
                .addRow()
                .addButton("📋 List All Texts", "admin_list_all")
                .addButton("⬅️ Back to Main", "main")
                .build();

        String message = "🔧 Admin Panel\n\nSelect an option:";
        messageSender.sendPlainMessage(chatId, message, keyboard);
    }


    private void showContentManagement(long chatId) {
        var keyboard = new MenuBuilder()
                .addButton("📝 Edit Welcome Message", "text_edit_WELCOME_MESSAGE")
                .addButton("📝 Edit Main Menu", "text_edit_MAIN_MENU_MESSAGE")
                .addRow()
                .addButton("📝 Edit Programs Menu", "text_edit_PROGRAMS_MENU_MESSAGE")
                .addButton("📝 Edit FAQ", "text_edit_FAQ_TEXT")
                .addRow()
                .addButton("📝 Edit Contacts", "text_edit_CONTACTS_TEXT")
                .addButton("📝 Edit News", "text_edit_NEWS_TEXT")
                .addRow()
                .addButton("⬅️ Back", "admin_main")
                .build();

        String message = "📝 *Content Management*\n\nSelect text to edit:";
        messageSender.sendMessage(chatId, message, keyboard);
    }

    private void showTextEditor(long chatId, String textKey) {
        String currentText = textContentService.getText(textKey);

        var keyboard = new MenuBuilder()
                .addButton("⬅️ Back", "admin_content")
                .build();

        String message = String.format(
                "📝 Editing: %s\n\n" +
                        "Current text:\n" +
                        "═══════════════════\n" +
                        "%s\n" +
                        "═══════════════════\n\n" +
                        "💡 To update, send a message like this:\n\n" +
                        "/update_text %s\n" +
                        "Your new text content here\n" +
                        "(can be multiple lines)\n\n" +
                        "Or on one line:\n" +
                        "/update_text %s Your new text here",
                textKey, currentText, textKey, textKey
        );

        messageSender.sendPlainMessage(chatId, message, keyboard);
    }

    /**
     * Escape MarkdownV2 special characters for display in admin messages
     */
    private String escapeMarkdownV2(String text) {
        if (text == null) return "";

        // Escape special MarkdownV2 characters
        return text.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    private void refreshContent(long chatId) {
        try {
            textContentService.refreshCache();

            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Back", "admin_main")
                    .build();

            messageSender.sendMessage(chatId, "✅ Content cache refreshed successfully!", keyboard);
        } catch (Exception e) {
            var keyboard = new MenuBuilder()
                    .addButton("⬅️ Back", "admin_main")
                    .build();

            messageSender.sendMessage(chatId, "❌ Failed to refresh cache: " + e.getMessage(), keyboard);
        }
    }

    private void listAllTexts(long chatId) {
        Map<String, String> allTexts = textContentService.getAllTexts();

        StringBuilder message = new StringBuilder("📋 All Text Entries:\n\n");

        allTexts.entrySet().stream()
                .limit(10)
                .forEach(entry -> {
                    String preview = entry.getValue().length() > 50
                            ? entry.getValue().substring(0, 50) + "..."
                            : entry.getValue();
                    message.append(String.format("• %s: %s\n", entry.getKey(), preview));
                });

        if (allTexts.size() > 10) {
            message.append(String.format("\n... and %d more entries", allTexts.size() - 10));
        }

        var keyboard = new MenuBuilder()
                .addButton("⬅️ Back", "admin_main")
                .build();

        messageSender.sendPlainMessage(chatId, message.toString(), keyboard);
    }

    // Helper method to check if user is admin
    private boolean isAdmin(long userId) {
        if (adminUserIds == null || adminUserIds.isEmpty()) {
            return false; // No admins configured
        }

        String[] adminIds = adminUserIds.split(",");
        String userIdStr = String.valueOf(userId);

        for (String adminId : adminIds) {
            if (adminId.trim().equals(userIdStr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle text update commands
     */
    public boolean handleTextUpdate(long chatId, String messageText) {
        if (!isAdmin(chatId)) {
            return false;
        }

        if (!messageText.startsWith("/update_text ")) {
            return false;
        }

        String content = messageText.substring("/update_text ".length()).trim();

        // Split on first whitespace (space, newline, tab, etc.)
        String[] parts = content.split("\\s+", 2);

        if (parts.length < 2) {
            messageSender.sendPlainMessage(chatId,
                    "❌ Invalid format. Use:\n/update_text TEXT_KEY new content here\n\nOr:\n/update_text TEXT_KEY\nnew content here",
                    new MenuBuilder().addButton("⬅️ Back", "admin_content").build());
            return true;
        }

        String textKey = parts[0].trim();
        String newValue = parts[1].trim();

        if (textKey.isEmpty() || newValue.isEmpty()) {
            messageSender.sendPlainMessage(chatId,
                    "❌ Both key and value are required.\n\nFormat: /update_text TEXT_KEY new content here",
                    new MenuBuilder().addButton("⬅️ Back", "admin_content").build());
            return true;
        }

        // Log what we're actually updating
        logger.info("Admin {} updating text key '{}' with value: '{}'", chatId, textKey, newValue);

        boolean success = textContentService.updateText(textKey, newValue);

        String responseMessage = success
                ? String.format("✅ Text updated successfully!\n\nKey: %s\nNew content length: %d characters", textKey, newValue.length())
                : "❌ Failed to update text. Please try again.";

        var keyboard = new MenuBuilder()
                .addButton("📝 Edit Another", "admin_content")
                .addButton("⬅️ Back to Admin", "admin_main")
                .build();

        messageSender.sendPlainMessage(chatId, responseMessage, keyboard);
        return true;
    }
}