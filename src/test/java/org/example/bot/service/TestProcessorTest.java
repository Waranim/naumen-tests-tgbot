package org.example.bot.service;

import org.example.bot.entity.*;
import org.example.bot.handler.MessageHandler;
import org.example.bot.processor.*;
import org.example.bot.processor.Add.*;
import org.example.bot.processor.Del.*;
import org.example.bot.processor.Edit.*;
import org.example.bot.processor.View.*;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тестирование обработчика сообщений для управления тестами.
 * Проверяет обработку создания, просмотра, редактирования и удаления тестов.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestProcessorTest {
    /**
     * Репозиторий для тестов
     */
    private TestRepository testRepository;
    
    /**
     *  Репозиторий для пользователей
     */
    private UserRepository userRepository;
    
    /**
     * Репозиторий для контекстов пользователей
     */
    private UserContextRepository contextRepository;

    /**
     * Утилитарный класс
     */
    private final NumberUtils numberUtils = new NumberUtils();

    /**
     * Утилитарный класс для тестов
     */
    private final TestUtils testUtils = new TestUtils();
    
    /**
     * Идентификатор пользователя
     */
    private final Long userId = 42L;
    
    /**
     * Обработчик сообщений
     */
    private MessageHandler messageHandler;

    /** 
     * Инициализация тестов, создание моков и настройка обработчиков сообщений.
     */
    @BeforeAll
    void init() {
        testRepository = mock(TestRepository.class);
        userRepository = mock(UserRepository.class);
        contextRepository = mock(UserContextRepository.class);

        UserService userService = new UserService(userRepository);
        ContextService contextService = new ContextService(contextRepository, userService);
        StateService stateService = new StateService(contextService);
        TestService testService = new TestService(testRepository, userService);

        HelpCommandProcessor helpCommandProcessor = new HelpCommandProcessor();
        StartCommandProcessor startCommandProcessor = new StartCommandProcessor(userService, helpCommandProcessor);
        AddCommandProcessor addCommandProcessor = new AddCommandProcessor(testService, stateService, contextService);
        ViewCommandProcessor viewCommandProcessor = new ViewCommandProcessor(testService, stateService, numberUtils, testUtils);
        EditCommandProcessor editCommandProcessor = new EditCommandProcessor(testService, numberUtils, contextService, stateService);
        DelCommandProcessor delCommandProcessor = new DelCommandProcessor(stateService, testService, testUtils);

        AddTestTitleProcessor addTestTitleProcessor = new AddTestTitleProcessor(stateService, contextService, testService);
        AddTestDescriptionProcessor addTestDescriptionProcessor = new AddTestDescriptionProcessor(stateService, contextService, testService);
        ViewTestProcessor viewTestProcessor = new ViewTestProcessor(stateService, viewCommandProcessor);
        EditTestProcessor editTestProcessor = new EditTestProcessor(stateService);
        EditTestTitleProcessor editTestTitleProcessor = new EditTestTitleProcessor(stateService, contextService, testService);
        EditTestDescriptionProcessor editTestDescriptionProcessor = new EditTestDescriptionProcessor(stateService, contextService, testService);
        DelTestProcessor delTestProcessor = new DelTestProcessor(stateService, testService, numberUtils, contextService);
        ConfirmDelTest confirmDelTest = new ConfirmDelTest(stateService, contextService, testService);

        List<MessageProcessor> processors = Arrays.asList(
                helpCommandProcessor,
                startCommandProcessor,
                addCommandProcessor,
                viewCommandProcessor,
                editCommandProcessor,
                delCommandProcessor,
                addTestTitleProcessor,
                addTestDescriptionProcessor,
                viewTestProcessor,
                editTestProcessor,
                editTestTitleProcessor,
                editTestDescriptionProcessor,
                delTestProcessor,
                confirmDelTest
        );

        messageHandler = new MessageHandler(processors);
    }

    /** 
     * Очистка моков после каждого теста.
     */
    @AfterEach
    void clearMocks() {
        clearInvocations(testRepository, userRepository, contextRepository);
    }

    /**
     * Создает тестовую сущность с заданными параметрами
     * @param userId Идентификатор пользователя
     * @param testId Идентификатор теста
     * @param title Название теста
     * @return Созданная тестовая сущность
     */
    private TestEntity createTest(Long userId, Long testId, String title) {
        TestEntity test = new TestEntity(userId, testId);
        test.setTitle(title);
        return test;
    }

    /**
     * Подготавливает данные для использования в тестах
     */
    @BeforeEach
    void prepareTestData() {
        UserContext context = new UserContext(userId);
        TestEntity test1 = createTest(userId, 123L, "Математический тест");
        TestEntity test2 = createTest(userId, 312L, "Тест по знаниям ПДД");
        UserEntity user = new UserEntity(Arrays.asList(test1, test2));

        user.setUserId(userId);
        user.setContext(context);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(contextRepository.findById(userId)).thenReturn(Optional.of(context));
        when(testRepository.findById(123L)).thenReturn(Optional.of(test1));
        when(testRepository.findById(312L)).thenReturn(Optional.of(test2));
    }

    /**
     * Тестирует процесс создания нового теста
     */
    @Test
    void testAddTest() {
        UserContext context = new UserContext(userId);
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setContext(context);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(contextRepository.findById(userId)).thenReturn(Optional.of(context));
        when(testRepository.save(any(TestEntity.class))).thenAnswer(i -> i.getArgument(0));

        String response1 = messageHandler.handle("/add", userId).getMessage();
        assertEquals("Введите название теста", response1);

        String response2 = messageHandler.handle("test", userId).getMessage();
        assertEquals("Введите описание теста", response2);

        String response3 = messageHandler.handle("Тестовое описание", userId).getMessage();
        assertEquals("Тест “test” создан! Количество вопросов: 0" +
                ". Для добавление вопросов используйте /add_question null, где null - идентификатор теста “test”."
                , response3);
    }

    /**
     * Тестирует просмотр списка тестов
     */
    @Test
    void testViewTests() {
        String response = messageHandler.handle("/view", userId).getMessage();
        assertEquals("Выберите тест для просмотра:\n" +
                "1)  id: 123 Математический тест\n" +
                "2)  id: 312 Тест по знаниям ПДД\n", response);

        String response2 = messageHandler.handle("123", userId).getMessage();
        assertEquals("Тест “Математический тест”. Всего вопросов: 0\n", response2);

        String response3 = messageHandler.handle("/view 312", userId).getMessage();
        assertEquals("Тест “Тест по знаниям ПДД”. Всего вопросов: 0\n", response3);
    }

    /**
     * Тестирует обработку просмотра несуществующего теста
     */
    @Test
    void testViewTestsWithNotExistId() {
        messageHandler.handle("/view", userId);
        String response2 = messageHandler.handle("999", userId).getMessage();
        assertEquals("Тест не найден!", response2);

        String response = messageHandler.handle("/view 999", userId).getMessage();
        assertEquals("Тест не найден!", response);
    }

    /**
     * Тестирует обработку некорректного идентификатора при просмотре теста
     */
    @Test
    void testViewTestsWithInvalidId() {
        messageHandler.handle("/view", userId);
        String response = messageHandler.handle("abc", userId).getMessage();
        assertEquals("Ошибка ввода!", response);

        String response2 = messageHandler.handle("/view abc", userId).getMessage();
        assertEquals("Ошибка ввода!", response2);
    }


    /**
     * Тестирует процесс редактирования названия теста
     */
    @Test
    void testEditTestTitle() {
        BotResponse response1 = messageHandler.handle("/edit 123", userId);
        assertEquals("Вы выбрали тест “Математический тест”. " +
                "Что вы хотите изменить?", response1.getMessage());
        assertEquals("Название теста", response1.getButtons().getFirst().getFirst().text());
        assertEquals("Описание теста", response1.getButtons().getLast().getFirst().text());
        assertEquals("EDIT_TEST 123 EDIT_TEST_TITLE", response1.getButtons().getFirst().getFirst().callbackData());
        assertEquals("EDIT_TEST 123 EDIT_TEST_DESCRIPTION", response1.getButtons().getLast().getFirst().callbackData());

        String response2 = messageHandler.handle("EDIT_TEST 123 EDIT_TEST_TITLE", userId).getMessage();
        assertEquals("Введите новое название теста", response2);

        String response3 = messageHandler.handle("Новый математический тест", userId).getMessage();
        assertEquals("Название изменено на “Новый математический тест”", response3);

        verify(testRepository, times(1)).save(argThat(savedTest -> 
            savedTest.getId().equals(123L) && 
            savedTest.getTitle().equals("Новый математический тест")
        ));
    }

    /**
     * Тестирует обработку редактирования с некорректным идентификатора теста
     */
    @Test
    void testEditTestWithInvalidId() {
        String response1 = messageHandler.handle("/edit 999", userId).getMessage();
        assertEquals("Тест не найден!", response1);

        String response2 = messageHandler.handle("/edit abc", userId).getMessage();
        assertEquals("Ошибка ввода!", response2);
    }

    /**
     * Тестирует процесс удаления теста
     */
    @Test
    void testDeleteTest() {
        BotResponse response1 = messageHandler.handle("/del", userId);
        assertEquals("Выберите тест:\n" +
                "1)  id: 123 Математический тест\n" +
                "2)  id: 312 Тест по знаниям ПДД\n", response1.getMessage());

        BotResponse response2 = messageHandler.handle("123", userId);
        assertEquals("Тест “Математический тест” будет удалён, вы уверены? (Да/Нет)", response2.getMessage());

        assertEquals("Да", response2.getButtons().getFirst().getFirst().text());
        assertEquals("Нет", response2.getButtons().getFirst().getLast().text());
        assertEquals("DEL_TEST_CONFIRM 123 YES", response2.getButtons().getFirst().getFirst().callbackData());
        assertEquals("DEL_TEST_CONFIRM 123 NO", response2.getButtons().getFirst().getLast().callbackData());

        BotResponse response3 = messageHandler.handle("DEL_TEST_CONFIRM 123 YES", userId);
        assertEquals("Тест “Математический тест” удалён", response3.getMessage());

        verify(testRepository, times(1)).delete(argThat(deletedTest -> 
            deletedTest.getId().equals(123L) && 
            deletedTest.getTitle().equals("Математический тест")
        ));
    }

    /**
     * Тестирует отмену удаления теста
     */
    @Test
    void testDeleteTestCancel() {
        messageHandler.handle("/del", userId);
        messageHandler.handle("123", userId);

        String response = messageHandler.handle("DEL_TEST_CONFIRM 123 NO", userId).getMessage();
        assertEquals("Тест “Математический тест” не удалён", response);
        
        verify(testRepository, never()).delete(any(TestEntity.class));
    }

    /**
     * Тестирует обработку некорректного идентификатора при удалении теста
     */
    @Test
    void testDeleteTestWithInvalidId() {
        messageHandler.handle("/del", userId);
        
        String response1 = messageHandler.handle("999", userId).getMessage();
        assertEquals("Тест не найден!", response1);

        String response2 = messageHandler.handle("abc", userId).getMessage();
        assertEquals("Введите число!", response2);
    }

    /**
     * Тестирует редактирование описания теста
     */
    @Test
    void testEditTestDescription() {
        String response1 = messageHandler.handle("/edit 123", userId).getMessage();
        assertEquals("Вы выбрали тест “Математический тест”. Что вы хотите изменить?", response1);

        String response2 = messageHandler.handle("EDIT_TEST 123 EDIT_TEST_DESCRIPTION", userId).getMessage();
        assertEquals("Введите новое описание теста", response2);

        String response3 = messageHandler.handle("Новое описание математического теста", userId).getMessage();
        assertEquals("Описание изменено на “Новое описание математического теста”", response3);

        verify(testRepository, times(1)).save(argThat(savedTest -> 
            savedTest.getId().equals(123L) && 
            savedTest.getDescription().equals("Новое описание математического теста")
        ));
    }
}