package org.example.bot.processor.Add;

import org.example.bot.entity.QuestionEntity;
import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractCommandProcessor;
import org.example.bot.service.QuestionService;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.service.TestService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.example.bot.util.TestUtils;
import org.example.bot.util.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды добавления вопроса в тест
 */
@Component
public class AddQuestionCommandProcessor extends AbstractCommandProcessor {

    /**
     * Сервис для управления тестами
     */
    private final TestService testService;

    /**
     * Сервис для управления вопросами
     */
    private final QuestionService questionService;

    /**
     * Сервис для управления состояниями
     */
    private final StateService stateService;

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Утилита с вспомогательными числовыми методами
     */
    private final NumberUtils numberUtils;

    /**
     * Утилита с вспомогательными методами для тестов
     */
    private final TestUtils testUtils;

    /**
     * Конструктор для инициализации обработчика команды добавления вопроса в тест
     *
     * @param testService     сервис для управления тестами
     * @param questionService сервис для управления вопросами
     * @param stateService    сервис для управления состояниями
     * @param contextService  сервис для управления контекстом
     * @param numberUtils     утилита с вспомогательными числовыми методами
     */
    public AddQuestionCommandProcessor(TestService testService,
                                       QuestionService questionService,
                                       StateService stateService,
                                       ContextService contextService,
                                       NumberUtils numberUtils,
                                       TestUtils testUtils) {
        super("/add_question");
        this.testService = testService;
        this.questionService = questionService;
        this.stateService = stateService;
        this.contextService = contextService;
        this.numberUtils = numberUtils;
        this.testUtils = testUtils;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        Optional<List<TestEntity>> tests = testService.getTestsByUserId(userId);
        if (parts.length == 1) {
            if (tests.isEmpty()) {
                return new BotResponse("У вас нет доступных тестов для добавления вопросов.");
            }
            stateService.changeStateById(userId, UserState.ADD_QUESTION);
            return new BotResponse("Выберите тест:\n" + testUtils.testsToString(tests.get()));
        }

        String testIdStr = parts[1];
        if (!numberUtils.isNumber(testIdStr)) {
            return new BotResponse("Ошибка ввода. Укажите корректный id теста.");
        }

        long testId = Long.parseLong(testIdStr);
        Optional<TestEntity> testOptional = testService.getTest(testId);
        if (tests.isEmpty() || testOptional.isEmpty() || !tests.get().contains(testOptional.get())) {
            return new BotResponse("Тест не найден!");
        }

        TestEntity test = testOptional.get();
        QuestionEntity question = questionService.createQuestion(test);
        stateService.changeStateById(userId, UserState.ADD_QUESTION_TEXT);
        contextService.setCurrentQuestion(userId, question);

        return new BotResponse(String.format("Введите название вопроса для теста “%s”", test.getTitle()));
    }
}
