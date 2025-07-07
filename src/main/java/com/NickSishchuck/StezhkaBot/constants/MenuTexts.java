package com.NickSishchuck.StezhkaBot.constants;

import com.NickSishchuck.StezhkaBot.service.TextContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MenuTexts {

    private final TextContentService textContentService;

    @Autowired
    public MenuTexts(TextContentService textContentService) {
        this.textContentService = textContentService;
    }

    // Methods instead of constants
    public String getWelcomeMessage() {
        return textContentService.getText("WELCOME_MESSAGE");
    }

    public String getMainMenuMessage() {
        return textContentService.getText("MAIN_MENU_MESSAGE");
    }

    public String getProgramsMenuMessage() {
        return textContentService.getText("PROGRAMS_MENU_MESSAGE");
    }

    public String getAge4to6Message() {
        return textContentService.getText("AGE_4_6_MESSAGE");
    }

    public String getAge6to10Message() {
        return textContentService.getText("AGE_6_10_MESSAGE");
    }

    public String getAge11to15Message() {
        return textContentService.getText("AGE_11_15_MESSAGE");
    }

    public String getAge15to18Message() {
        return textContentService.getText("AGE_15_18_MESSAGE");
    }

    public String getSpecialistsMessage() {
        return textContentService.getText("SPECIALISTS_MESSAGE");
    }

    public String getProgramPreschoolDetails() {
        return textContentService.getText("PROGRAM_PRESCHOOL_DETAILS");
    }

    public String getProgramPrimaryDetails() {
        return textContentService.getText("PROGRAM_PRIMARY_DETAILS");
    }

    public String getProgramEnglishDetails() {
        return textContentService.getText("PROGRAM_ENGLISH_DETAILS");
    }

    public String getFaqText() {
        return textContentService.getText("FAQ_TEXT");
    }

    public String getContactsText() {
        return textContentService.getText("CONTACTS_TEXT");
    }

    public String getNewsText() {
        return textContentService.getText("NEWS_TEXT");
    }
}