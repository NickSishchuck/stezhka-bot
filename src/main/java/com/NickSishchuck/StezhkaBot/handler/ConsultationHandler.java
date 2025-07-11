package com.NickSishchuck.StezhkaBot.handler;

import com.NickSishchuck.StezhkaBot.entity.ConsultationRequest;
import com.NickSishchuck.StezhkaBot.service.ConsultationService;
import com.NickSishchuck.StezhkaBot.utils.MenuBuilder;
import com.NickSishchuck.StezhkaBot.utils.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
public class ConsultationHandler implements MenuHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConsultationHandler.class);

    private final ConsultationService consultationService;
    private TelegramClient telegramClient;
    private MessageSender messageSender;

    @Value("${bot.admin.user.ids:}")
    private String adminUserIds;

    @Autowired
    public ConsultationHandler(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @Override
    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.messageSender = new MessageSender(telegramClient);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.equals("consultations_main") ||
                callbackData.equals("consultation_confirm") ||
                callbackData.equals("consultation_cancel") ||
                callbackData.equals("/consultations") ||
                callbackData.startsWith("process_consultation_") ||
                callbackData.startsWith("view_consultation_");
    }

    @Override
    public void handle(long chatId, String callbackData) {
        if (callbackData.equals("/consultations") && isAdmin(chatId)) {
            showUnprocessedConsultations(chatId);
        }
    }

    @Override
    public void handle(long chatId, int messageId, String callbackData) {
        switch (callbackData) {
            case "consultations_main" -> startConsultation(chatId, messageId);
            case "consultation_confirm" -> confirmConsultation(chatId, messageId);
            case "consultation_cancel" -> cancelConsultation(chatId, messageId);
            default -> {
                if (callbackData.startsWith("process_consultation_") && isAdmin(chatId)) {
                    processConsultation(chatId, messageId, callbackData);
                } else if (callbackData.startsWith("view_consultation_") && isAdmin(chatId)) {
                    String requestId = callbackData.substring("view_consultation_".length());
                    showConsultationDetails(chatId, Long.parseLong(requestId));
                }
            }
        }
    }

    /**
     * Start consultation request
     */
    private void startConsultation(long chatId, int messageId) {
        // Check for recent consultations with new limit system
        if (consultationService.hasRecentConsultation(chatId, 0)) {
            int currentCount = consultationService.getCurrentRequestCount(chatId);
            messageSender.editMessage(chatId, messageId,
                    String.format("⚠️ Ви вже подали максимальну кількість заявок (%d/5). Спробуйте пізніше або зачекайте 30 хвилин.", currentCount),
                    new MenuBuilder().addButton("⬅️ Назад", "main").build());
            return;
        }

        consultationService.startConsultation(chatId);
        showConsultationStep(chatId, messageId, ConsultationService.ConsultationStep.NAME);
    }

    /**
     * Show consultation step
     */
    private void showConsultationStep(long chatId, int messageId, ConsultationService.ConsultationStep step) {
        String message = switch (step) {
            case NAME -> "👤 *Введіть ваше ім'я та прізвище:*";
            case PHONE -> "📞 *Введіть ваш номер телефону:*";
            case CONFIRM -> formatConfirmationMessage(chatId);
            default -> "Помилка";
        };

        var keyboard = new MenuBuilder();

        if (step == ConsultationService.ConsultationStep.CONFIRM) {
            keyboard.addButton("✅ Підтвердити", "consultation_confirm")
                    .addButton("❌ Скасувати", "consultation_cancel");
        } else {
            keyboard.addButton("❌ Скасувати", "consultation_cancel");
        }

        messageSender.editMessage(chatId, messageId, message, keyboard.build());
    }

    /**
     * Process text input for consultation
     */
    public boolean processTextInput(long chatId, String text) {
        ConsultationService.ConsultationFormState state = consultationService.getConsultationState(chatId);
        if (state == null || state.getCurrentStep() == ConsultationService.ConsultationStep.CONFIRM) {
            return false;
        }

        // Validate input
        if (!validateInput(state.getCurrentStep(), text)) {
            showValidationError(chatId, state.getCurrentStep());
            return true;
        }

        ConsultationService.ConsultationStep nextStep = consultationService.processConsultationInput(chatId, text);

        if (nextStep != null) {
            showConsultationStep(chatId, nextStep);
        }

        return true;
    }

    /**
     * Show consultation step in new message
     */
    private void showConsultationStep(long chatId, ConsultationService.ConsultationStep step) {
        String message = switch (step) {
            case NAME -> "👤 *Введіть ваше ім'я та прізвище:*";
            case PHONE -> "📞 *Введіть ваш номер телефону:*";
            case CONFIRM -> formatConfirmationMessage(chatId);
            default -> "Помилка";
        };

        var keyboard = new MenuBuilder();

        if (step == ConsultationService.ConsultationStep.CONFIRM) {
            keyboard.addButton("✅ Підтвердити", "consultation_confirm")
                    .addButton("❌ Скасувати", "consultation_cancel");
        } else {
            keyboard.addButton("❌ Скасувати", "consultation_cancel");
        }

        messageSender.sendMessage(chatId, message, keyboard.build());
    }

    /**
     * Validate user input
     */
    private boolean validateInput(ConsultationService.ConsultationStep step, String input) {
        return switch (step) {
            case NAME -> input.length() >= 2 && input.length() <= 100;
            case PHONE -> input.matches("^\\+?[0-9\\s\\-()]+$") && input.length() >= 9;
            default -> true;
        };
    }

    /**
     * Show validation error
     */
    private void showValidationError(long chatId, ConsultationService.ConsultationStep step) {
        String error = switch (step) {
            case NAME -> "❌ Будь ласка, введіть коректне ім'я та прізвище";
            case PHONE -> "❌ Будь ласка, введіть коректний номер телефону";
            default -> "❌ Некоректні дані";
        };

        var keyboard = new MenuBuilder()
                .addButton("❌ Скасувати", "consultation_cancel")
                .build();

        messageSender.sendMessage(chatId, error + "\n\nСпробуйте ще раз:", keyboard);
    }

    /**
     * Format confirmation message
     */
    private String formatConfirmationMessage(long chatId) {
        ConsultationService.ConsultationFormState state = consultationService.getConsultationState(chatId);
        if (state == null) {
            return "Помилка";
        }

        return String.format(
                "📋 *Перевірте дані:*\n\n" +
                        "👤 *Ім'я:* %s\n" +
                        "📞 *Телефон:* %s\n\n" +
                        "Все вірно?",
                state.getName(),
                state.getPhone()
        );
    }

    /**
     * Confirm consultation
     */
    private void confirmConsultation(long chatId, int messageId) {
        ConsultationRequest request = consultationService.completeConsultation(chatId);

        if (request != null) {
            messageSender.editMessage(chatId, messageId,
                    "✅ *Дякуємо!*\n\nВаша заявка на консультацію прийнята. Ми зв'яжемося з вами найближчим часом.\n\n" +
                            "Номер заявки: #" + request.getId(),
                    new MenuBuilder().addButton("⬅️ На головну", "main").build());

            // Notify admin
            notifyAdminAboutNewConsultation(request);
        } else {
            messageSender.editMessage(chatId, messageId,
                    "❌ Помилка при збереженні заявки. Спробуйте пізніше.",
                    new MenuBuilder().addButton("⬅️ На головну", "main").build());
        }
    }

    /**
     * Cancel consultation
     */
    private void cancelConsultation(long chatId, int messageId) {
        consultationService.cancelConsultation(chatId);
        messageSender.editMessage(chatId, messageId,
                "❌ Заявку на консультацію скасовано",
                new MenuBuilder().addButton("⬅️ На головну", "main").build());
    }

    /**
     * Notify admin about new consultation request
     */
    private void notifyAdminAboutNewConsultation(ConsultationRequest request) {
        String message = consultationService.formatRequestForAdmin(request);

        var keyboardMarkup = new MenuBuilder()
                .addButton("✅ Опрацьовано", "process_consultation_" + request.getId())
                .build();

        // Send to all admins
        for (Long adminId : getAdminIds()) {
            try {
                SendMessage sendMessage = SendMessage.builder()
                        .chatId(adminId)
                        .text(message)
                        .parseMode("Markdown")
                        .replyMarkup(keyboardMarkup)
                        .build();

                Message sentMessage = telegramClient.execute(sendMessage);

                // Save message ID for future reference
                consultationService.updateAdminMessageId(request.getId(), sentMessage.getMessageId());

            } catch (TelegramApiException e) {
                logger.error("Failed to notify admin {} about consultation {}", adminId, request.getId(), e);
            }
        }
    }

    /**
     * Show unprocessed consultations for admin
     */
    private void showUnprocessedConsultations(long chatId) {
        List<ConsultationRequest> requests = consultationService.getUnprocessedRequests();

        if (requests.isEmpty()) {
            messageSender.sendMessage(chatId,
                    "✅ Немає необроблених заявок на консультацію",
                    new MenuBuilder().addButton("⬅️ Назад", "admin_main").build());
            return;
        }

        StringBuilder message = new StringBuilder("📋 *Необроблені консультації (" + requests.size() + "):*\n\n");
        var keyboardBuilder = new MenuBuilder();

        int count = 1;
        for (ConsultationRequest request : requests) {
            if (count <= 10) { // Show max 10 requests
                message.append(String.format("%d️⃣ %s - %s - %s\n",
                        count,
                        request.getName(),
                        request.getPhone(),
                        request.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM"))
                ));

                if (count <= 5) { // Add buttons for first 5
                    keyboardBuilder.addButton(count + "", "view_consultation_" + request.getId());
                }
            }
            count++;
        }

        if (requests.size() > 10) {
            message.append("\n... та ще ").append(requests.size() - 10).append(" заявок");
        }

        keyboardBuilder.addRow().addButton("⬅️ Назад", "admin_main");

        messageSender.sendMessage(chatId, message.toString(), keyboardBuilder.build());
    }

    /**
     * Show consultation details
     */
    private void showConsultationDetails(long chatId, Long requestId) {
        consultationService.getUnprocessedRequests().stream()
                .filter(r -> r.getId().equals(requestId))
                .findFirst()
                .ifPresent(request -> {
                    String message = consultationService.formatRequestForAdmin(request);
                    var keyboard = new MenuBuilder()
                            .addButton("✅ Опрацьовано", "process_consultation_" + request.getId())
                            .addButton("⬅️ Назад", "/consultations")
                            .build();

                    messageSender.sendMessage(chatId, message, keyboard);
                });
    }

    /**
     * Process consultation request
     */
    private void processConsultation(long chatId, int messageId, String callbackData) {
        String requestIdStr = callbackData.substring("process_consultation_".length());
        Long requestId = Long.parseLong(requestIdStr);

        boolean success = consultationService.markAsProcessed(requestId, chatId);

        if (success) {
            // Try to delete the message
            try {
                telegramClient.execute(org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .build());
            } catch (TelegramApiException e) {
                // If delete fails, just edit the message
                messageSender.editMessage(chatId, messageId,
                        "✅ Консультація #" + requestId + " опрацьована",
                        new MenuBuilder().build());
            }
        }
    }

    /**
     * Check if user is admin
     */
    private boolean isAdmin(long userId) {
        return getAdminIds().contains(userId);
    }

    /**
     * Get admin IDs
     */
    private List<Long> getAdminIds() {
        if (adminUserIds == null || adminUserIds.isEmpty()) {
            return List.of();
        }

        return java.util.Arrays.stream(adminUserIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .map(Long::parseLong)
                .toList();
    }
}