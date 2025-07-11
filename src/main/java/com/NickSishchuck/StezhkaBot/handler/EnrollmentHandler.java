package com.NickSishchuck.StezhkaBot.handler;

import com.NickSishchuck.StezhkaBot.entity.EnrollmentRequest;
import com.NickSishchuck.StezhkaBot.service.EnrollmentService;
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
public class EnrollmentHandler implements MenuHandler {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentHandler.class);

    private final EnrollmentService enrollmentService;
    private TelegramClient telegramClient;
    private MessageSender messageSender;

    @Value("${bot.admin.user.ids:}")
    private String adminUserIds;

    @Autowired
    public EnrollmentHandler(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @Override
    public void setTelegramClient(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.messageSender = new MessageSender(telegramClient);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("enroll_") ||
                callbackData.equals("enrollment_general") ||
                callbackData.equals("enrollment_confirm") ||
                callbackData.equals("enrollment_cancel") ||
                callbackData.equals("/requests") ||
                callbackData.startsWith("process_request_") ||
                callbackData.startsWith("view_request_");
    }

    @Override
    public void handle(long chatId, String callbackData) {
        if (callbackData.equals("/requests") && isAdmin(chatId)) {
            showUnprocessedRequests(chatId);
        }
    }

    @Override
    public void handle(long chatId, int messageId, String callbackData) {
        switch (callbackData) {
            case "enrollment_general" -> startGeneralEnrollment(chatId, messageId);
            case "enrollment_confirm" -> confirmEnrollment(chatId, messageId);
            case "enrollment_cancel" -> cancelEnrollment(chatId, messageId);
            default -> {
                if (callbackData.startsWith("enroll_")) {
                    String programType = callbackData.substring("enroll_".length());
                    startEnrollment(chatId, messageId, programType);
                } else if (callbackData.startsWith("enrollment_course_")) {
                    String course = callbackData.substring("enrollment_course_".length());
                    selectCourse(chatId, messageId, course);
                } else if (callbackData.startsWith("process_request_") && isAdmin(chatId)) {
                    processRequest(chatId, messageId, callbackData);
                } else if (callbackData.startsWith("view_request_") && isAdmin(chatId)) {
                    String requestId = callbackData.substring("view_request_".length());
                    showRequestDetails(chatId, Long.parseLong(requestId));
                }
            }
        }
    }

    /**
     * Start enrollment with pre-selected course
     */
    private void startEnrollment(long chatId, int messageId, String programType) {
        // Check for recent enrollments
        if (enrollmentService.hasRecentEnrollment(chatId, 2)) {
            messageSender.editMessage(chatId, messageId,
                    "⚠️ Ви вже подавали заявку нещодавно. Ми зв'яжемося з вами найближчим часом!",
                    new MenuBuilder().addButton("⬅️ Назад", "programs_main").build());
            return;
        }

        String courseDisplayName = getCourseDisplayName(programType);
        enrollmentService.startEnrollment(chatId, programType, courseDisplayName);

        showEnrollmentStep(chatId, messageId, EnrollmentService.EnrollmentStep.CHILD_NAME);
    }

    /**
     * Start general enrollment from main menu
     */
    private void startGeneralEnrollment(long chatId, int messageId) {
        // Check for recent enrollments
        if (enrollmentService.hasRecentEnrollment(chatId, 2)) {
            messageSender.editMessage(chatId, messageId,
                    "⚠️ Ви вже подавали заявку нещодавно. Ми зв'яжемося з вами найближчим часом!",
                    new MenuBuilder().addButton("⬅️ Назад", "main").build());
            return;
        }

        enrollmentService.startEnrollmentWithoutCourse(chatId);
        showCourseSelection(chatId, messageId);
    }

    /**
     * Show course selection menu
     */
    private void showCourseSelection(long chatId, int messageId) {
        var keyboard = new MenuBuilder()
                // Age 4-6
                .addButton("📚 Підготовка до школи", "enrollment_course_preschool")
                .addButton("🧠 Нейропсихолог (дошкільнята)", "enrollment_course_neuropsychologist_preschool")  // NEW
                .addRow()
                // Age 6-10
                .addButton("🏫 Початкова школа", "enrollment_course_primary")
                .addButton("🇬🇧 Англійська мова (6-10)", "enrollment_course_english")
                .addRow()
                .addButton("💰 Фінансова грамотність", "enrollment_course_financial")
                .addButton("🎨 Творчі гуртки", "enrollment_course_creative")
                .addRow()
                // Age 11-15
                .addButton("🧠 Психолог (підлітки)", "enrollment_course_teen_psychologist")
                .addButton("🇬🇧 Англійська (11-15)", "enrollment_course_english_middle")  // NEW
                .addRow()
                // Age 15-18
                .addButton("🎯 Підготовка до НМТ", "enrollment_course_nmt")
                .addRow()
                // Specialists
                .addButton("👩‍⚕️ Психолог", "enrollment_course_psychologist")
                .addButton("🗣️ Логопед", "enrollment_course_speech_therapist")
                .addRow()
                .addButton("🧠 Нейропедагог", "enrollment_course_neuropedagog")
                .addRow()
                // Vacation programs
                .addButton("🍂 Осінні канікули", "enrollment_course_autumn_vacation")
                .addButton("❄️ Зимові канікули", "enrollment_course_winter_vacation")
                .addRow()
                .addButton("🌸 Весняні канікули", "enrollment_course_spring_vacation")
                .addButton("☀️ Літні канікули", "enrollment_course_summer_vacation")
                .addRow()
                .addButton("❌ Скасувати", "enrollment_cancel")
                .build();

        messageSender.editMessage(chatId, messageId,
                "🎓 *Оберіть програму для запису:*", keyboard);
    }

    /**
     * Handle course selection
     */
    private void selectCourse(long chatId, int messageId, String course) {
        String courseDisplayName = getCourseDisplayName(course);
        enrollmentService.setCourse(chatId, course, courseDisplayName);
        showEnrollmentStep(chatId, messageId, EnrollmentService.EnrollmentStep.CHILD_NAME);
    }

    /**
     * Show enrollment step
     */
    private void showEnrollmentStep(long chatId, int messageId, EnrollmentService.EnrollmentStep step) {
        String message = switch (step) {
            case CHILD_NAME -> "👦 *Введіть ім'я та прізвище дитини:*";
            case CHILD_AGE -> "🎂 *Введіть вік дитини:*";
            case PARENT_NAME -> "👨‍👩‍👧 *Введіть ваше ім'я та прізвище:*";
            case PARENT_PHONE -> "📞 *Введіть ваш номер телефону:*";
            case CONFIRM -> formatConfirmationMessage(chatId);
            default -> "Помилка";
        };

        var keyboard = new MenuBuilder();

        if (step == EnrollmentService.EnrollmentStep.CONFIRM) {
            keyboard.addButton("✅ Підтвердити", "enrollment_confirm")
                    .addButton("❌ Скасувати", "enrollment_cancel");
        } else {
            keyboard.addButton("❌ Скасувати", "enrollment_cancel");
        }

        messageSender.editMessage(chatId, messageId, message, keyboard.build());
    }

    /**
     * Process text input for enrollment
     */
    public boolean processTextInput(long chatId, String text) {
        EnrollmentService.EnrollmentFormState state = enrollmentService.getEnrollmentState(chatId);
        if (state == null || state.getCurrentStep() == EnrollmentService.EnrollmentStep.CONFIRM) {
            return false;
        }

        // Validate input
        if (!validateInput(state.getCurrentStep(), text)) {
            showValidationError(chatId, state.getCurrentStep());
            return true;
        }

        EnrollmentService.EnrollmentStep nextStep = enrollmentService.processEnrollmentInput(chatId, text);

        if (nextStep != null) {
            showEnrollmentStep(chatId, nextStep);
        }

        return true;
    }

    /**
     * Show enrollment step in new message
     */
    private void showEnrollmentStep(long chatId, EnrollmentService.EnrollmentStep step) {
        String message = switch (step) {
            case CHILD_NAME -> "👦 *Введіть ім'я та прізвище дитини:*";
            case CHILD_AGE -> "🎂 *Введіть вік дитини:*";
            case PARENT_NAME -> "👨‍👩‍👧 *Введіть ваше ім'я та прізвище:*";
            case PARENT_PHONE -> "📞 *Введіть ваш номер телефону:*";
            case CONFIRM -> formatConfirmationMessage(chatId);
            default -> "Помилка";
        };

        var keyboard = new MenuBuilder();

        if (step == EnrollmentService.EnrollmentStep.CONFIRM) {
            keyboard.addButton("✅ Підтвердити", "enrollment_confirm")
                    .addButton("❌ Скасувати", "enrollment_cancel");
        } else {
            keyboard.addButton("❌ Скасувати", "enrollment_cancel");
        }

        messageSender.sendMessage(chatId, message, keyboard.build());
    }

    /**
     * Validate user input
     */
    private boolean validateInput(EnrollmentService.EnrollmentStep step, String input) {
        return switch (step) {
            case CHILD_NAME, PARENT_NAME -> input.length() >= 2 && input.length() <= 100;
            case CHILD_AGE -> {
                try {
                    int age = Integer.parseInt(input);
                    yield age >= 3 && age <= 18;
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
            case PARENT_PHONE -> input.matches("^\\+?[0-9\\s\\-()]+$") && input.length() >= 9;
            default -> true;
        };
    }

    /**
     * Show validation error
     */
    private void showValidationError(long chatId, EnrollmentService.EnrollmentStep step) {
        String error = switch (step) {
            case CHILD_NAME -> "❌ Будь ласка, введіть коректне ім'я та прізвище";
            case CHILD_AGE -> "❌ Будь ласка, введіть вік від 3 до 18 років";
            case PARENT_NAME -> "❌ Будь ласка, введіть коректне ім'я та прізвище";
            case PARENT_PHONE -> "❌ Будь ласка, введіть коректний номер телефону";
            default -> "❌ Некоректні дані";
        };

        var keyboard = new MenuBuilder()
                .addButton("❌ Скасувати", "enrollment_cancel")
                .build();

        messageSender.sendMessage(chatId, error + "\n\nСпробуйте ще раз:", keyboard);
    }

    /**
     * Format confirmation message
     */
    private String formatConfirmationMessage(long chatId) {
        EnrollmentService.EnrollmentFormState state = enrollmentService.getEnrollmentState(chatId);
        if (state == null) {
            return "Помилка";
        }

        return String.format(
                "📋 *Перевірте дані:*\n\n" +
                        "👦 *Дитина:* %s (%s років)\n" +
                        "👨‍👩‍👧 *Батьки:* %s\n" +
                        "📞 *Телефон:* %s\n" +
                        "🎓 *Програма:* %s\n\n" +
                        "Все вірно?",
                state.getChildName(),
                state.getChildAge(),
                state.getParentName(),
                state.getParentPhone(),
                state.getCourseDisplayName()
        );
    }

    /**
     * Confirm enrollment
     */
    private void confirmEnrollment(long chatId, int messageId) {
        EnrollmentRequest request = enrollmentService.completeEnrollment(chatId);

        if (request != null) {
            messageSender.editMessage(chatId, messageId,
                    "✅ *Дякуємо!*\n\nВаша заявка прийнята. Ми зв'яжемося з вами найближчим часом.\n\n" +
                            "Номер заявки: #" + request.getId(),
                    new MenuBuilder().addButton("⬅️ На головну", "main").build());

            // Notify admin
            notifyAdminAboutNewRequest(request);
        } else {
            messageSender.editMessage(chatId, messageId,
                    "❌ Помилка при збереженні заявки. Спробуйте пізніше.",
                    new MenuBuilder().addButton("⬅️ На головну", "main").build());
        }
    }

    /**
     * Cancel enrollment
     */
    private void cancelEnrollment(long chatId, int messageId) {
        enrollmentService.cancelEnrollment(chatId);
        messageSender.editMessage(chatId, messageId,
                "❌ Заявку скасовано",
                new MenuBuilder().addButton("⬅️ На головну", "main").build());
    }

    /**
     * Notify admin about new enrollment request
     */
    private void notifyAdminAboutNewRequest(EnrollmentRequest request) {
        String message = enrollmentService.formatRequestForAdmin(request);

        var keyboardMarkup = new MenuBuilder()
                .addButton("✅ Опрацьовано", "process_request_" + request.getId())
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
                enrollmentService.updateAdminMessageId(request.getId(), sentMessage.getMessageId());

            } catch (TelegramApiException e) {
                logger.error("Failed to notify admin {} about enrollment {}", adminId, request.getId(), e);
            }
        }
    }

    /**
     * Show unprocessed requests for admin
     */
    private void showUnprocessedRequests(long chatId) {
        List<EnrollmentRequest> requests = enrollmentService.getUnprocessedRequests();

        if (requests.isEmpty()) {
            messageSender.sendMessage(chatId,
                    "✅ Немає необроблених заявок",
                    new MenuBuilder().addButton("⬅️ Назад", "admin_main").build());
            return;
        }

        StringBuilder message = new StringBuilder("📋 *Необроблені заявки (" + requests.size() + "):*\n\n");
        var keyboardBuilder = new MenuBuilder();

        int count = 1;
        for (EnrollmentRequest request : requests) {
            if (count <= 10) { // Show max 10 requests
                message.append(String.format("%d️⃣ %s - %s - %s\n",
                        count,
                        request.getChildName(),
                        request.getCourseDisplayName(),
                        request.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM"))
                ));

                if (count <= 5) { // Add buttons for first 5
                    keyboardBuilder.addButton(count + "", "view_request_" + request.getId());
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
     * Show request details
     */
    private void showRequestDetails(long chatId, Long requestId) {
        enrollmentService.getUnprocessedRequests().stream()
                .filter(r -> r.getId().equals(requestId))
                .findFirst()
                .ifPresent(request -> {
                    String message = enrollmentService.formatRequestForAdmin(request);
                    var keyboardMarkup = new MenuBuilder()
                            .addButton("✅ Опрацьовано", "process_request_" + request.getId())
                            .addButton("⬅️ Назад", "/requests")
                            .build();

                    messageSender.sendMessage(chatId, message, keyboardMarkup);
                });
    }

    /**
     * Process enrollment request
     */
    private void processRequest(long chatId, int messageId, String callbackData) {
        String requestIdStr = callbackData.substring("process_request_".length());
        Long requestId = Long.parseLong(requestIdStr);

        boolean success = enrollmentService.markAsProcessed(requestId, chatId);

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
                        "✅ Заявка #" + requestId + " опрацьована",
                        new MenuBuilder().build());
            }
        }
    }

    /**
     * Get course display name
     */
    private String getCourseDisplayName(String courseKey) {
        return switch (courseKey) {
            case "preschool" -> "Підготовка до школи";
            case "neuropsychologist_preschool" -> "Нейропсихолог (дошкільнята)";
            case "primary" -> "Початкова школа";
            case "english" -> "Англійська мова (6-10 років)";
            case "english_middle" -> "Англійська мова (11-15 років)";
            case "financial" -> "Фінансова грамотність";
            case "creative" -> "Творчі гуртки";
            case "teen_psychologist", "teen_psychology" -> "Психолог (підлітки)";
            case "nmt" -> "Підготовка до НМТ";
            case "psychologist" -> "Психолог";
            case "speech_therapist" -> "Логопед";
            case "neuropedagog" -> "Нейропедагог";
            case "autumn_vacation" -> "Осінні канікули";
            case "winter_vacation" -> "Зимові канікули";
            case "spring_vacation" -> "Весняні канікули";
            case "summer_vacation" -> "Літні канікули";
            default -> courseKey;
        };
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