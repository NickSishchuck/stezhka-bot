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

    // Main menu texts
    public String getWelcomeMessage() {
        return textContentService.getText("WELCOME_MESSAGE");
    }

    public String getMainMenuMessage() {
        return textContentService.getText("MAIN_MENU_MESSAGE");
    }

    public String getProgramsMenuMessage() {
        return textContentService.getText("PROGRAMS_MENU_MESSAGE");
    }

    // NEW: Vacation menu
    public String getVacationMenuMessage() {
        return textContentService.getText("VACATION_MENU_MESSAGE");
    }

    // Age group messages
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

    // Program details - Age 4-6
    public String getProgramPreschoolDetails() {
        return textContentService.getText("PROGRAM_PRESCHOOL_DETAILS");
    }

    // NEW: Neuropsychologist for preschoolers
    public String getProgramNeuropsychologistPreschoolDetails() {
        return textContentService.getText("PROGRAM_NEUROPSYCHOLOGIST_PRESCHOOL_DETAILS");
    }

    // Program details - Age 6-10
    public String getProgramPrimaryDetails() {
        return textContentService.getText("PROGRAM_PRIMARY_DETAILS");
    }

    public String getProgramEnglishDetails() {
        return textContentService.getText("PROGRAM_ENGLISH_DETAILS");
    }

    public String getProgramFinancialDetails() {
        return textContentService.getText("PROGRAM_FINANCIAL_DETAILS");
    }

    public String getProgramCreativeDetails() {
        return textContentService.getText("PROGRAM_CREATIVE_DETAILS");
    }

    // Program details - Age 11-15
    public String getProgramTeenPsychologyDetails() {
        return textContentService.getText("PROGRAM_TEEN_PSYCHOLOGY_DETAILS");
    }

    // NEW: English for middle school
    public String getProgramEnglishMiddleDetails() {
        return textContentService.getText("PROGRAM_ENGLISH_MIDDLE_DETAILS");
    }

    // Program details - Age 15-18
    public String getProgramNmtDetails() {
        return textContentService.getText("PROGRAM_NMT_DETAILS");
    }

    // Specialists programs
    public String getProgramPsychologistDetails() {
        return textContentService.getText("PROGRAM_PSYCHOLOGIST_DETAILS");
    }

    public String getProgramSpeechTherapistDetails() {
        return textContentService.getText("PROGRAM_SPEECH_THERAPIST_DETAILS");
    }

    // NEW: Neuropedagog specialist
    public String getProgramNeuropedagogDetails() {
        return textContentService.getText("PROGRAM_NEUROPEDAGOG_DETAILS");
    }

    // NEW: Vacation programs
    public String getProgramAutumnVacationDetails() {
        return textContentService.getText("PROGRAM_AUTUMN_VACATION_DETAILS");
    }

    public String getProgramWinterVacationDetails() {
        return textContentService.getText("PROGRAM_WINTER_VACATION_DETAILS");
    }

    public String getProgramSpringVacationDetails() {
        return textContentService.getText("PROGRAM_SPRING_VACATION_DETAILS");
    }

    public String getProgramSummerVacationDetails() {
        return textContentService.getText("PROGRAM_SUMMER_VACATION_DETAILS");
    }

    // Static content
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