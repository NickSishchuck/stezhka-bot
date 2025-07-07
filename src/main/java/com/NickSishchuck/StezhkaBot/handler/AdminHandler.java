package com.NickSishchuck.StezhkaBot.handler;

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

    private final TextContentService textContentService;
    private TelegramClient telegramClient;
    private MessageSender messageSender;

    // List of admin user IDs (can be moved to database later)
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

        String message = "🔧 *Admin Panel*\n\nSelect an option:";
        messageSender.sendMessage(chatId, message, keyboard);
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
                "📝 *Editing: %s*\n\n" +
                        "*Current text:*\n" +
                        "```\n%s\n```\n\n" +
                        "💡 To update this text, send a message in the format:\n" +
                        "`/update_text %s YOUR_NEW_TEXT_HERE`",
                textKey, currentText, textKey
        );

        messageSender.sendMessage(chatId, message, keyboard);
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

        StringBuilder message = new StringBuilder("📋 *All Text Entries:*\n\n");

        allTexts.entrySet().stream()
                .limit(10) // Limit to prevent message being too long
                .forEach(entry -> {
                    String preview = entry.getValue().length() > 50
                            ? entry.getValue().substring(0, 50) + "..."
                            : entry.getValue();
                    message.append(String.format("• *%s*: %s\n", entry.getKey(), preview));
                });

        if (allTexts.size() > 10) {
            message.append(String.format("\n... and %d more entries", allTexts.size() - 10));
        }

        var keyboard = new MenuBuilder()
                .addButton("⬅️ Back", "admin_main")
                .build();

        messageSender.sendMessage(chatId, message.toString(), keyboard);
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

        String content = messageText.substring("/update_text ".length());
        String[] parts = content.split(" ", 2);

        if (parts.length < 2) {
            messageSender.sendMessage(chatId,
                    "❌ Invalid format. Use: `/update_text TEXT_KEY new content here`",
                    new MenuBuilder().addButton("⬅️ Back", "admin_content").build());
            return true;
        }

        String textKey = parts[0];
        String newValue = parts[1];

        boolean success = textContentService.updateText(textKey, newValue);

        String responseMessage = success
                ? "✅ Text updated successfully!"
                : "❌ Failed to update text. Please try again.";

        var keyboard = new MenuBuilder()
                .addButton("📝 Edit Another", "admin_content")
                .addButton("⬅️ Back to Admin", "admin_main")
                .build();

        messageSender.sendMessage(chatId, responseMessage, keyboard);
        return true;
    }
}