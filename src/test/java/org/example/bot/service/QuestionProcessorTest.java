package org.example.bot.service;

import org.example.bot.dto.InlineButtonDTO;
import org.example.bot.entity.*;
import org.example.bot.handler.MessageHandler;
import org.example.bot.processor.Add.*;
import org.example.bot.processor.Del.*;
import org.example.bot.processor.Edit.*;
import org.example.bot.processor.MessageProcessor;
import org.example.bot.processor.Add.StopCommandProcessor;
import org.example.bot.processor.View.ViewQuestionCommandProcessor;
import org.example.bot.repository.QuestionRepository;
import org.example.bot.repository.TestRepository;
import org.example.bot.repository.UserRepository;
import org.example.bot.repository.UserContextRepository;
import org.example.bot.telegram.BotResponse;
import org.example.bot.util.TestUtils;
import org.example.bot.util.NumberUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * Тестирование обработчика сообщений для управления вопросами
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class QuestionProcessorTest {

    /**
     * Репозиторий для работы с тестом в базе данных
     */
    private TestRepository testRepository;

    /**
     * Репозиторий для взаимодействия над сущностью пользователя в базе данных
     */
    private UserRepository userRepository;

    /**
     * Репозиторий для взаимодействия над сущностью сессии пользователя в базе данных
     */
    private UserContextRepository userContextRepository;

    /**
     * Репозиторий для работы с вопросом в базе данных
     */
    private QuestionRepository questionRepository;

    /**
     * Утилита с вспомогательными методами
     */
    private final NumberUtils numberUtils = new NumberUtils();

    /**
     * Утилита с вспомогательными методами для тестов
     */
    private final TestUtils testUtils = new TestUtils();

    /**
     * Идентификатор пользователя
     */
    private final Long userId = 1L;

    /**
     * Обработчик сообщений
     */
    private MessageHandler messageHandler;
    /**
     * Инициализация всех необходимых объектов перед выполнением тестов
     */
    @BeforeAll
    void init() {
        testRepository = mock(TestRepository.class);
        userRepository = mock(UserRepository.class);
        userContextRepository = mock(UserContextRepository.class);
        questionRepository = mock(QuestionRepository.class);


        UserService userService = new UserService(userRepository);
        ContextService contextService = new ContextService(userContextRepository, userService);
        StateService stateService = new StateService(contextService);
        TestService testService = new TestService(testRepository, userService);
        QuestionService questionService = new QuestionService(questionRepository, testService);


        AddQuestionCommandProcessor addQuestionCommandProcessor = new AddQuestionCommandProcessor(testService,
                questionService, stateService,
                contextService, numberUtils, testUtils);
        AddQuestionProcessor addQuestionProcessor = new AddQuestionProcessor(stateService,
                contextService, questionService,
                numberUtils, testService);
        AddQuestionTextProcessor addQuestionTextProcessor = new AddQuestionTextProcessor(stateService,
                contextService, questionService);
        StopCommandProcessor stopCommandProcessor = new StopCommandProcessor(stateService,
                contextService);
        AddAnswerQuestionProcessor addAnswerQuestionProcessor = new AddAnswerQuestionProcessor(stateService,
                contextService, questionService,
                stopCommandProcessor);
        DelQuestionProcessor delQuestionProcessor = new DelQuestionProcessor(stateService,
                contextService, questionService);
        DelQuestionCommandProcessor delQuestionCommandProcessor = new DelQuestionCommandProcessor(stateService,
                delQuestionProcessor);
        ConfirmDelQuestion confirmDelQuestion = new ConfirmDelQuestion(stateService,
                contextService, questionService);

        EditQuestionCommandProcessor editQuestionCommandProcessor = new EditQuestionCommandProcessor(stateService,
                contextService,
                questionService, numberUtils);
        EditQuestionProcessor editQuestionProcessor = new EditQuestionProcessor(stateService);
        EditQuestionTextProcessor editQuestionTextProcessor = new EditQuestionTextProcessor(stateService,
                contextService, questionService);
        EditAnswerOptionChoiceProcessor editAnswerOptionChoiceProcessor = new EditAnswerOptionChoiceProcessor(stateService,
                contextService);
        EditAnswerTextChoiceProcessor editAnswerTextChoiceProcessor = new EditAnswerTextChoiceProcessor(stateService,
                contextService);
        EditAnswerTextProcessor editAnswerTextProcessor = new EditAnswerTextProcessor(stateService,
                contextService, questionService);
        EditSetCorrectAnswerProcessor editSetCorrectAnswerProcessor = new EditSetCorrectAnswerProcessor(stateService,
                contextService, questionService);

        ViewQuestionCommandProcessor viewQuestionCommandProcessor = new ViewQuestionCommandProcessor(testService, numberUtils);

        List<MessageProcessor> processors = Arrays.asList(
                addAnswerQuestionProcessor,
                addQuestionCommandProcessor,
                addQuestionProcessor,
                addQuestionTextProcessor,
                stopCommandProcessor,
                delQuestionCommandProcessor,
                delQuestionProcessor,
                confirmDelQuestion,
                editQuestionCommandProcessor,
                editQuestionProcessor,
                editQuestionTextProcessor,
                editAnswerOptionChoiceProcessor,
                editAnswerTextChoiceProcessor,
                editAnswerTextProcessor,
                editSetCorrectAnswerProcessor,
                viewQuestionCommandProcessor
        );
        messageHandler = new MessageHandler(processors);

    }

    /**
     * Сброс вызовов моков перед выполнением каждого теста
     */
    @BeforeEach
    void initEach() {
        UserContext session = new UserContext(userId);
        TestEntity test1 = createTest(userId, 1L, "Математический тест");
        QuestionEntity question1 = createQuestion(test1, 1L, "Сколько будет 2 + 2?");

        createAnswer(question1, "1", false);
        createAnswer(question1, "4", true);

        UserEntity user = new UserEntity(Arrays.asList(test1));
        user.setUserId(userId);
        user.setContext(session);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userContextRepository.findById(userId)).thenReturn(Optional.of(session));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test1));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question1));

    }

    /**
     * Очистка моков после каждого теста.
     */
    @AfterEach
    void clearMocks() {
        clearInvocations(testRepository, userRepository, userContextRepository, questionRepository);
    }

    /**
     * Создает тест с заданными параметрами
     *
     * @param userId Идентификатор пользователя
     * @param testId Идентификатор теста
     * @param title  Название теста
     * @return созданный тест
     */
    private TestEntity createTest(Long userId, Long testId, String title) {
        TestEntity test = new TestEntity(userId, testId);
        test.setTitle(title);
        return test;
    }

    /**
     * Создает вопрос с заданными параметрами
     *
     * @param test       Тест для которого создается вопрос
     * @param questionId Идентификатор вопроса
     * @param title      Формулировка вопроса
     * @return созданный вопрос
     */
    private QuestionEntity createQuestion(TestEntity test, Long questionId, String title) {
        QuestionEntity question = new QuestionEntity(test, questionId);
        question.setQuestion(title);
        test.getQuestions().add(question);
        return question;
    }

    /**
     * Создает ответ с заданными параметрами
     *
     * @param question  Вопрос для которого создается ответ
     * @param title     Формулировка ответа
     * @param isCorrect Флаг правильности ответа
     * @return созданный ответ
     */
    private AnswerEntity createAnswer(QuestionEntity question, String title, boolean isCorrect) {
        AnswerEntity answer = new AnswerEntity();
        answer.setAnswerText(title);
        answer.setCorrect(isCorrect);
        answer.setQuestion(question);
        question.getAnswers().add(answer);
        return answer;
    }

    /**
     * Тестирует обработку команды добавления вопроса без указания id вопроса
     */
    @Test
    void testAddQuestionWithoutId() {
        UserContext session = new UserContext(userId);
        TestEntity test = createTest(userId, 1L, "Математический тест");
        UserEntity user = new UserEntity(List.of(test));
        user.setUserId(userId);
        user.setContext(session);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userContextRepository.findById(userId)).thenReturn(Optional.of(session));
        when(questionRepository.save(any(QuestionEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));

        BotResponse response1 = messageHandler.handle("/add_question", userId);
        assertEquals("Выберите тест:\n1)  id: 1 Математический тест\n", response1.getMessage());

        BotResponse response2 = messageHandler.handle("1", userId);
        assertEquals("Введите название вопроса для теста “Математический тест”", response2.getMessage());

        BotResponse response3 = messageHandler.handle("Сколько будет 2 + 2?", userId);
        assertEquals("Введите вариант ответа. " +
                        "Если вы хотите закончить добавлять варианты, " +
                        "введите команду /stop.",
                response3.getMessage());

        BotResponse response4 = messageHandler.handle("1", userId);
        assertEquals("Введите вариант ответа. " +
                        "Если вы хотите закончить добавлять варианты, " +
                        "введите команду “/stop”.",
                response4.getMessage());

        BotResponse response5 = messageHandler.handle("/stop", userId);
        assertEquals("Вы не создали необходимый минимум ответов (минимум: 2). " +
                        "Введите варианты ответа.",
                response5.getMessage());

        BotResponse response6 = messageHandler.handle("4", userId);
        assertEquals("Введите вариант ответа. " +
                        "Если вы хотите закончить добавлять варианты, " +
                        "введите команду “/stop”.",
                response6.getMessage());


        BotResponse response7 = messageHandler.handle("/stop", userId);


        assertEquals("Укажите правильный вариант ответа:\n1: 1\n2: 4\n", response7.getMessage());

        assertNotNull(response7.getButtons());
        assertEquals(2, response7.getButtons().size());

        InlineButtonDTO button = response7.getButtons().getFirst().getFirst();
        assertEquals("1", button.text());
        assertEquals("SET_CORRECT_ANSWER 0", button.callbackData());

        InlineButtonDTO button6 = response7.getButtons().get(1).getFirst();
        assertEquals("4", button6.text());
        assertEquals("SET_CORRECT_ANSWER 1", button6.callbackData());

        BotResponse response8 = messageHandler.handle("SET_CORRECT_ANSWER 3", userId);
        assertEquals("Некорректный номер варианта ответа. " +
                        "Введите число от 1 до 2",
                response8.getMessage());

        BotResponse response9 = messageHandler.handle("SET_CORRECT_ANSWER 1", userId);
        assertEquals("Вариант ответа 2 назначен правильным.",
                response9.getMessage());

        verify(questionRepository, times(5))
                .save(argThat(savedQuestion ->
                        savedQuestion.getQuestion().equals("Сколько будет 2 + 2?")
                                && savedQuestion.getAnswers().size() == 2
                ));
    }

    /**
     * Тестирует обработку команды добавления вопроса с указанием Id теста
     */
    @Test
    void testAddQuestionWithId() {
        UserContext session = new UserContext(userId);
        TestEntity test = createTest(userId, 1L, "Математический тест");
        UserEntity user = new UserEntity(List.of(test));
        user.setUserId(userId);
        user.setContext(session);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userContextRepository.findById(userId)).thenReturn(Optional.of(session));
        when(questionRepository.save(any(QuestionEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));

        BotResponse response1 = messageHandler.handle("/add_question 1", userId);
        assertEquals("Введите название вопроса для теста “Математический тест”", response1.getMessage());

        BotResponse response2 = messageHandler.handle("Сколько будет 2 + 2?", userId);
        assertEquals("Введите вариант ответа. " +
                        "Если вы хотите закончить добавлять варианты, " +
                        "введите команду /stop.",
                response2.getMessage());

        BotResponse response3 = messageHandler.handle("1", userId);
        assertEquals("Введите вариант ответа. " +
                        "Если вы хотите закончить добавлять варианты, " +
                        "введите команду “/stop”.",
                response3.getMessage());

        BotResponse response4 = messageHandler.handle("/stop", userId);
        assertEquals("Вы не создали необходимый минимум ответов (минимум: 2). " +
                        "Введите варианты ответа.",
                response4.getMessage());

        BotResponse response5 = messageHandler.handle("4", userId);
        assertEquals("Введите вариант ответа. " +
                        "Если вы хотите закончить добавлять варианты, " +
                        "введите команду “/stop”.",
                response5.getMessage());


        BotResponse response6 = messageHandler.handle("/stop", userId);


        assertEquals("Укажите правильный вариант ответа:\n1: 1\n2: 4\n", response6.getMessage());

        assertNotNull(response6.getButtons());
        assertEquals(2, response6.getButtons().size());

        InlineButtonDTO button = response6.getButtons().getFirst().getFirst();
        assertEquals("1", button.text());
        assertEquals("SET_CORRECT_ANSWER 0", button.callbackData());

        InlineButtonDTO button6 = response6.getButtons().get(1).getFirst();
        assertEquals("4", button6.text());
        assertEquals("SET_CORRECT_ANSWER 1", button6.callbackData());

        BotResponse response8 = messageHandler.handle("SET_CORRECT_ANSWER 3", userId);
        assertEquals("Некорректный номер варианта ответа. " +
                        "Введите число от 1 до 2",
                response8.getMessage());

        BotResponse response9 = messageHandler.handle("SET_CORRECT_ANSWER 1", userId);
        assertEquals("Вариант ответа 2 назначен правильным.",
                response9.getMessage());

        verify(questionRepository, times(5))
                .save(argThat(savedQuestion ->
                        savedQuestion.getQuestion().equals("Сколько будет 2 + 2?")
                                && savedQuestion.getAnswers().size() == 2
                ));
    }

    /**
     * Тестирует обработку команды добавления вопроса с отсутствующим тестом
     */
    @Test
    void testAddQuestionNotFound() {
        BotResponse response = messageHandler.handle("/add_question 1234", userId);
        assertEquals("Тест не найден!", response.getMessage());
    }

    /**
     * Тестирует обработку команды добавления вопроса с некорректным id вопроса
     */
    @Test
    void testAddQuestionWithInvalidId() {
        BotResponse response = messageHandler.handle("/add_question f", userId);
        assertEquals("Ошибка ввода. Укажите корректный id теста.", response.getMessage());
    }

    /**
     * Тестирует обработку команды просмотра вопросов у теста
     */
    @Test
    void testViewQuestion() {
        BotResponse response = messageHandler.handle("/view_question 1", userId);
        assertEquals("Вопросы теста \"Математический тест\":\n" +
                "1) id:1  \"Сколько будет 2 + 2?\"\n", response.getMessage());
    }

    /**
     * Тестирует обработку команды просмотра вопросов с отсутствующим тестом
     */
    @Test
    void testViewQuestionNotFound() {
        BotResponse response = messageHandler.handle("/view_question 1234", userId);
        assertEquals("Тест не найден!", response.getMessage());
    }

    /**
     * Тестирует обработку команды добавления вопроса без указания id вопроса
     */
    @Test
    void testViewQuestionWithoutId() {
        BotResponse response = messageHandler.handle("/view_question", userId);
        assertEquals("Используйте команду вместе с идентификатором вопроса!", response.getMessage());
    }

    /**
     * Тестирует обработку команды просмотра вопросов с некорректным id вопроса
     */
    @Test
    void testViewQuestionWithInvalidId() {
        BotResponse response = messageHandler.handle("/view_question f", userId);
        assertEquals("Ошибка ввода. Укажите корректный id теста.", response.getMessage());
    }

    /**
     * Тестирует редактирование формулировки вопроса
     */
    @Test
    void testEditQuestionTitle() {
        BotResponse response1 = messageHandler.handle("/edit_question 1", userId);
        assertEquals("Вы выбрали вопрос “Сколько будет 2 + 2?”. Что вы хотите изменить в вопросе?",
                response1.getMessage());
        assertNotNull(response1.getButtons());
        assertEquals(2, response1.getButtons().size());

        BotResponse response2 = messageHandler.handle("EDIT_QUESTION 1 1", userId);
        assertEquals("Введите новый текст вопроса",
                response2.getMessage());

        BotResponse response3 = messageHandler.handle("Сколько будет 4-2?", userId);
        assertEquals("Текст вопроса изменен на “Сколько будет 4-2?”",
                response3.getMessage());

        verify(questionRepository, times(1))
                .save(argThat(savedQuestion ->
                        savedQuestion.getId().equals(1L) &&
                                savedQuestion.getQuestion().equals("Сколько будет 4-2?")
                ));
    }

    /**
     * Тестирует редактирование формулировки варианта ответа
     */
    @Test
    void testEditAnswerText() {
        BotResponse response1 = messageHandler.handle("/edit_question 1", userId);
        assertEquals("Вы выбрали вопрос “Сколько будет 2 + 2?”. Что вы хотите изменить в вопросе?", response1.getMessage());

        BotResponse response2 = messageHandler.handle("EDIT_QUESTION 1 2", userId);
        assertEquals("Что вы хотите сделать с вариантом ответа?\n", response2.getMessage());

        BotResponse response3 = messageHandler.handle("EDIT_ANSWER_TEXT_CHOICE 0", userId);
        assertEquals("Введите новую формулировку ответа", response3.getMessage());

        BotResponse response4 = messageHandler.handle("2", userId);
        assertEquals("Формулировка изменена на “2”", response4.getMessage());

        verify(questionRepository, times(1))
                .save(argThat(savedQuestion ->
                        savedQuestion.getId().equals(1L) &&
                                savedQuestion.getAnswers().stream()
                                        .filter(a -> a.getAnswerText() == "2")
                                        .findFirst().isPresent()
                ));
    }

    /**
     * Тестирует редактирование правильного ответа
     */
    @Test
    void testEditCorrectAnswer() {
        BotResponse response1 = messageHandler.handle("/edit_question 1", userId);
        assertEquals("Вы выбрали вопрос “Сколько будет 2 + 2?”. Что вы хотите изменить в вопросе?",
                response1.getMessage());

        BotResponse response2 = messageHandler.handle("EDIT_QUESTION 1 2", userId);
        assertEquals("Что вы хотите сделать с вариантом ответа?\n",
                response2.getMessage());

        BotResponse response3 = messageHandler.handle("SET_CORRECT_ANSWER 0", userId);
        assertEquals("Вариант ответа 1 назначен правильным.",
                response3.getMessage());

        verify(questionRepository, times(1))
                .save(argThat(savedQuestion ->
                        savedQuestion.getId().equals(1L) &&
                                savedQuestion.getAnswers().stream()
                                        .filter(AnswerEntity::isCorrect)
                                        .findFirst().get()
                                        .getAnswerText().equals("1")
                ));
    }

    /**
     * Тестирует обработку команды редактирования вопроса с отсутствующим вопросом
     */
    @Test
    void testEditQuestionNotFound() {
        BotResponse response = messageHandler.handle("/edit_question 1234", userId);
        assertEquals("Вопрос не найден!", response.getMessage());
    }

    /**
     * Тестирует обработку команды редактирования вопроса без указания id
     */
    @Test
    void testEditQuestionWithoutId() {
        BotResponse response = messageHandler.handle("/edit_question", userId);
        assertEquals("Используйте команду вместе с идентификатором вопроса!", response.getMessage());
    }

    /**
     * Тестирует обработку команды редактирования вопроса с некорректным id вопроса
     */
    @Test
    void testEditQuestionWithInvalidId() {
        BotResponse response = messageHandler.handle("/edit_question f", userId);
        assertEquals("Ошибка ввода. Укажите корректный id теста.", response.getMessage());
    }

    /**
     * Тестирует обработку команды удаления вопроса с указанием id вопроса
     */
    @Test
    void testConfirmDeleteQuestionWithQuestionId() {
        UserContext session = new UserContext(userId);
        TestEntity test = createTest(userId, 1L, "Математический тест");
        UserEntity user = new UserEntity(List.of(test));
        user.setUserId(userId);
        user.setContext(session);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userContextRepository.findById(userId)).thenReturn(Optional.of(session));
        when(questionRepository.save(any(QuestionEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));

        BotResponse response1 = messageHandler.handle("/del_question 1", userId);
        assertEquals("Вопрос “Сколько будет 2 + 2?” " +
                        "будет удалён, вы уверены?",
                response1.getMessage());
        assertNotNull(response1.getButtons());
        assertEquals(2, response1.getButtons().getFirst().size());

        InlineButtonDTO yesButton = response1.getButtons().getFirst().getFirst();
        assertEquals("Да", yesButton.text());
        assertEquals("DEL_QUESTION_CONFIRM 1 YES", yesButton.callbackData());

        BotResponse response2 = messageHandler.handle("DEL_QUESTION_CONFIRM 1 YES", userId);
        assertEquals("Вопрос “Сколько будет 2 + 2?” " +
                        "из теста “Математический тест” удален.",
                response2.getMessage());

        verify(questionRepository, times(1))
                .delete(argThat(savedQuestion ->
                        savedQuestion.getId().equals(1L) &&
                                savedQuestion.getQuestion().equals("Сколько будет 2 + 2?")
                ));
    }

    /**
     * Тестирует обработку команды отмены удаления вопроса с указанием id вопроса
     */
    @Test
    void testCancelDeleteQuestionWithQuestionId() {
        BotResponse response1 = messageHandler.handle("/del_question 1", userId);
        assertEquals("Вопрос “Сколько будет 2 + 2?” " +
                "будет удалён, вы уверены?", response1.getMessage());

        assertEquals("Вопрос “Сколько будет 2 + 2?” " +
                        "будет удалён, вы уверены?",
                response1.getMessage());
        assertNotNull(response1.getButtons());
        assertEquals(2, response1.getButtons().getFirst().size());

        InlineButtonDTO noButton = response1.getButtons().getFirst().get(1);
        assertEquals("Нет", noButton.text());
        assertEquals("DEL_QUESTION_CONFIRM 1 NO", noButton.callbackData());

        BotResponse response2 = messageHandler.handle("DEL_QUESTION_CONFIRM 1 NO", userId);
        assertEquals("Вопрос “Сколько будет 2 + 2?” " +
                        "из теста “Математический тест” не удален.",
                response2.getMessage());

        verify(questionRepository, never()).delete(any(QuestionEntity.class));
    }

    /**
     * Тестирует обработку команды удаления вопроса без указания id вопроса
     */
    @Test
    void testConfirmDeleteQuestionWithoutQuestionId() {
        BotResponse response1 = messageHandler.handle("/del_question", userId);
        assertEquals("Введите id вопроса для удаления:\n", response1.getMessage());

        BotResponse response2 = messageHandler.handle("1", userId);
        assertEquals("Вопрос “Сколько будет 2 + 2?” будет удалён, вы уверены?",
                response2.getMessage());
        assertNotNull(response2.getButtons());
        assertEquals(2, response2.getButtons().getFirst().size());

        InlineButtonDTO yesButton = response2.getButtons().getFirst().getFirst();
        assertEquals("Да", yesButton.text());
        assertEquals("DEL_QUESTION_CONFIRM 1 YES", yesButton.callbackData());

        BotResponse response3 = messageHandler.handle("DEL_QUESTION_CONFIRM 1 YES", userId);
        assertEquals("Вопрос “Сколько будет 2 + 2?” " +
                        "из теста “Математический тест” удален.",
                response3.getMessage());

        verify(questionRepository, times(1))
                .delete(argThat(savedQuestion ->
                        savedQuestion.getId().equals(1L) &&
                                savedQuestion.getQuestion().equals("Сколько будет 2 + 2?")
                ));
    }

    /**
     * Тестирует обработку команды отмены удаления вопроса без указания id вопроса
     */
    @Test
    void testCancelDeleteQuestionWithoutQuestionId() {
        BotResponse response1 = messageHandler.handle("/del_question", userId);
        assertEquals("Введите id вопроса для удаления:\n",
                response1.getMessage());

        BotResponse response2 = messageHandler.handle("1", userId);
        assertEquals("Вопрос “Сколько будет 2 + 2?” " +
                "будет удалён, вы уверены?", response2.getMessage());

        assertEquals("Вопрос “Сколько будет 2 + 2?” будет удалён, вы уверены?",
                response2.getMessage());
        assertNotNull(response2.getButtons());
        assertEquals(2, response2.getButtons().getFirst().size());

        InlineButtonDTO noButton = response2.getButtons().getFirst().get(1);
        assertEquals("Нет", noButton.text());
        assertEquals("DEL_QUESTION_CONFIRM 1 NO", noButton.callbackData());

        BotResponse response3 = messageHandler.handle("DEL_QUESTION_CONFIRM 1 NO", userId);
        assertEquals("Вопрос “Сколько будет 2 + 2?” " +
                        "из теста “Математический тест” не удален.",
                response3.getMessage());

        verify(questionRepository, never()).delete(any(QuestionEntity.class));
    }

    /**
     * Тестирует обработку команды удаления вопроса с отсутствующим вопросом
     */
    @Test
    void testDeleteQuestionNotFound() {
        BotResponse response = messageHandler.handle("/del_question 1234", userId);
        assertEquals("Вопрос не найден!", response.getMessage());
    }

    /**
     * Тестирует обработку команды удаления вопроса с некорректным id вопроса
     */
    @Test
    void testDeleteQuestionWithInvalidId() {
        BotResponse response = messageHandler.handle("/del_question f", userId);
        assertEquals("Некорректный формат идентификатора вопроса. Пожалуйста, введите число.", response.getMessage());
    }
}