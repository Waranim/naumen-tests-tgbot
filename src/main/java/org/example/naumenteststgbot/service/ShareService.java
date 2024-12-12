package org.example.naumenteststgbot.service;

import org.example.naumenteststgbot.entity.TestEntity;
import org.example.naumenteststgbot.entity.UserEntity;
import org.example.naumenteststgbot.entity.UserSession;
import org.example.naumenteststgbot.enums.UserState;
import org.example.naumenteststgbot.repository.TestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

/**
 * Сервис для шейринга тестов
 */
@Service
public class ShareService {
    private final UserService userService;
    private final MessageBuilder messageBuilder;
    private final KeyboardService keyboardService;
    private final TestService testService;
    private final Utils utils;
    private final TestRepository testRepository;

    public ShareService(UserService userService, MessageBuilder messageBuilder, KeyboardService keyboardService, TestService testService, Utils utils, TestRepository testRepository) {
        this.userService = userService;
        this.messageBuilder = messageBuilder;
        this.keyboardService = keyboardService;
        this.testService = testService;
        this.utils = utils;
        this.testRepository = testRepository;
    }

    /**
     * Обработать команду /share
     */
    public SendMessage handleShare(Long userId, String chatId) {
        List<TestEntity> tests = userService.getTestsById(userId);
        List<String> buttons = tests.stream().map(TestEntity::getTitle).toList();
        List<String> callbacks = tests.stream().map(t -> t.getId().toString()).toList();
        InlineKeyboardMarkup keyboardMarkup = keyboardService.createReply(buttons, callbacks, "SHARE CHOOSE_TEST");
        return messageBuilder.createSendMessage(chatId, "Выберите тест: ", keyboardMarkup);
    }


    /**
     * Обработать callback
     */
    @Transactional
    public SendMessage handleCallback(String chatId, String callbackData, Long userId) {
        String[] callbackDataParts = callbackData.split(" ");
        Long testId = Long.parseLong(callbackDataParts[2]);
        return switch (callbackDataParts[1]){
            case "CHOOSE_TEST" -> handleChooseTest(chatId, userId, testId);
            case "UNSUBSCRIBE_CHOOSE_TEST" -> handleUnsubscribeChooseTest(chatId, userId, testId);
            case "UNSUBSCRIBE" -> handleUnsubscribe(chatId, userId, testId);
            default -> throw new IllegalStateException("Unexpected value: " + callbackDataParts[1]);
        };
    }

    /**
     * Обработать отписку от теста
     */
    private SendMessage handleUnsubscribe(String chatId, Long userId, Long testId) {
        List<TestEntity> receivedTests = userService.getOpenReceivedTests(userId);
        TestEntity test = testService.getTest(testId);
        if(!receivedTests.contains(test))
            return messageBuilder.createErrorMessage(chatId, "У вас нет доступа к тесту");

        userService.removeReceivedTest(userId, test);
        test.getRecipients().remove(userService.getUserById(userId));
        String creatorUsername = userService.getUserById(test.getCreatorId()).getUsername();
        return messageBuilder.createSendMessage(
                chatId,
                "Вы отписались от теста “%s (%s)” Чтобы вернуть доступ к тесту необходимо обратится к его владельцу."
                        .formatted(test.getTitle(), creatorUsername),
                null);
    }

    /**
     * Обработать выбор теста для отписки
     */
    private SendMessage handleUnsubscribeChooseTest(String chatId, Long userId, Long testId) {
        List<TestEntity> receivedTests = userService.getOpenReceivedTests(userId);
        TestEntity test = testService.getTest(testId);
        if(!receivedTests.contains(test))
            return messageBuilder.createErrorMessage(chatId, "У вас нет доступа к тесту");
        InlineKeyboardMarkup keyboard = keyboardService.createReply(
                "Отписаться",
                testId.toString(),
                "SHARE UNSUBSCRIBE"
                );
        String creatorUsername = userService.getUserById(test.getCreatorId()).getUsername();
        return messageBuilder.createSendMessage(chatId,
                "Вы выбрали “%s (%s)”. Всего вопросов: %s."
                        .formatted(test.getTitle(), creatorUsername, test.getQuestions().size())
                , keyboard);
    }

    /**
     * Обработать выбор теста
     */
    private SendMessage handleChooseTest(String chatId, Long userId, Long testId) {
        TestEntity currentTest = testService.getTest(testId);
        if(!currentTest.isAccessOpen())
            return messageBuilder.createErrorMessage(chatId, "У теста закрыт доступ!");
        userService.setCurrentTest(userId, currentTest);
        userService.changeStateById(userId, UserState.CHOOSE_USER);

        return messageBuilder.createSendMessage(
                chatId, 
                "Введите идентификатор пользователя (его можно посмотреть командой /info)",
                null);
    }

    /**
     * Обработать сообщение
     */
    @Transactional
    public SendMessage handleMessage(String chatId, UserSession userSession, String text) {
        return switch (userSession.getState()){
            case CHOOSE_USER -> handleChooseUser(chatId, userSession, text);
            default -> throw new IllegalStateException("Unexpected value: " + userSession.getState());
        };
    }

    /**
     * Обработать выбор пользователя
     */
    private SendMessage handleChooseUser(String chatId, UserSession userSession, String text) {
        if(!utils.isNumber(text))
            return messageBuilder.createErrorMessage(chatId, "Некорректный id пользователя");
        UserEntity recipientUser = userService.getUserById(Long.parseLong(text));
        if(recipientUser == null)
            return messageBuilder.createErrorMessage(chatId, "Пользователь не найден");
        TestEntity test = userSession.getCurrentTest();
        List<TestEntity> receivedTests = recipientUser.getReceivedTests();
        if(receivedTests.contains(test) || recipientUser.getTests().contains(test))
            return messageBuilder.createErrorMessage(chatId, "Пользователь уже имеет доступ к этому тесту");

        test.getRecipients().add(recipientUser);
        userService.addReceivedTest(recipientUser.getId(), test);
        userService.changeStateById(userSession.getUserId(), UserState.DEFAULT);
        testRepository.save(test);
        return messageBuilder.createSendMessage(chatId, "Пользователь " + text + " получил доступ к тесту", null);
    }

    /**
     * Обработать команду /shared_tests
     */
    public SendMessage handleShareTests(Long userId, String chatId) {
        List<TestEntity> receivedTests = userService.getOpenReceivedTests(userId);
        List<String> buttons = receivedTests.stream().map(TestEntity::getTitle).toList();
        List<String> callbacks = receivedTests.stream().map(t -> t.getId().toString()).toList();
        InlineKeyboardMarkup inlineKeyboardMarkup = keyboardService.createReply(
                buttons,
                callbacks,
                "SHARE UNSUBSCRIBE_CHOOSE_TEST");
        return messageBuilder.createSendMessage(chatId, "Выберите тест:", inlineKeyboardMarkup);
    }
}
