package org.example.naumenteststgbot.service;

import org.example.naumenteststgbot.entity.UserSession;
import org.example.naumenteststgbot.enums.UserState;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Обработчик сообщений
 */
@Service
public class MessageHandler {
    /**
     * Сервис для взаимодействия с пользователем
     */
    private final UserService userService;

    /**
     * Сервис для взаимодействия с тестами
     */
    private final TestService testService;

    /**
     * Сервис для взаимодействия с вопросами
     */
    private final QuestionService questionService;
    private final ShareService shareService;

    /**
     * Конструктор класса MessageHandler
     */
    public MessageHandler(UserService userService, TestService testService, QuestionService questionService, ShareService shareService) {
        this.userService = userService;
        this.testService = testService;
        this.questionService = questionService;
        this.shareService = shareService;
    }

    /**
     * Обработать сообщение
     */
    public SendMessage handleMessage(Update update) {
        UserSession userSession = userService.getSession(update.getMessage().getFrom().getId());
        UserState userState = userSession.getState();
        String text = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        SendMessage responseMessage = null;

        switch (userState) {
            case ADD_TEST_TITLE:
            case ADD_TEST_DESCRIPTION:
            case EDIT_TEST:
            case EDIT_TEST_TITLE:
            case EDIT_TEST_DESCRIPTION:
            case DELETE_TEST:
            case CONFIRM_DELETE_TEST:
            case VIEW_TEST:
                responseMessage = testService.handleMessage(chatId,userSession, text);
                break;

            case ADD_QUESTION_TEXT:
            case ADD_QUESTION:
            case ADD_ANSWER:
            case SET_CORRECT_ANSWER:
            case EDIT_QUESTION:
            case EDIT_QUESTION_TEXT:
            case EDIT_ANSWER_OPTION:
            case EDIT_ANSWER_OPTION_CHOICE:
            case EDIT_ANSWER_TEXT_CHOICE:
            case EDIT_ANSWER_TEXT:
            case DELETE_QUESTION:
            case CONFIRM_DELETE_QUESTION:
                responseMessage = questionService.handleMessage(chatId,userSession,text);
                break;

            case CHOOSE_USER:
                responseMessage = shareService.handleMessage(chatId, userSession, text);

            default:
                break;
        }

        return responseMessage;
    }
}
