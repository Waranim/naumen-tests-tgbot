package org.example.bot.processor.Add;

import org.example.bot.entity.QuestionEntity;
import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractStateProcessor;
import org.example.bot.service.QuestionService;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.service.TestService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.example.bot.util.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик состояния добавления вопроса
 */
@Component
public class AddQuestionProcessor extends AbstractStateProcessor {

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Сервис для управления вопросами.
     */
    private final QuestionService questionService;

    /**
     * Утилита с вспомогательными числовыми методами
     */
    private final NumberUtils numberUtils;

    /**
     * Сервис для управления тестами
     */
    private final TestService testService;

    /**
     * Конструктор для инициализации обработчика добавления вопроса
     *
     * @param stateService    сервис для управления состояниями
     * @param contextService  сервис для управления контекстом
     * @param questionService сервис для управления тестами
     * @param testService сервис для управления тестами
     * @param numberUtils утилита с вспомогательными числовыми методами
     */
    public AddQuestionProcessor(StateService stateService,
                                ContextService contextService,
                                QuestionService questionService,
                                NumberUtils numberUtils,
                                TestService testService) {
        super(stateService, UserState.ADD_QUESTION);
        this.contextService = contextService;
        this.questionService = questionService;
        this.numberUtils = numberUtils;
        this.testService = testService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        if (!numberUtils.isNumber(message)) {
            return new BotResponse("Некорректный id теста. Пожалуйста, введите число.");
        }

        long testId = Long.parseLong(message);
        Optional<TestEntity> testOptional = testService.getTest(testId);
        Optional<List<TestEntity>> testsOptional = testService.getTestsByUserId(userId);
        if (testOptional.isEmpty() || testsOptional.isEmpty() || !testsOptional.get().contains(testOptional.get())) {
            return new BotResponse("Тест не найден!");
        }

        TestEntity selectedTest = testOptional.get();
        QuestionEntity nquestion = questionService.createQuestion(selectedTest);
        contextService.setCurrentQuestion(userId, nquestion);
        stateService.changeStateById(userId, UserState.ADD_QUESTION_TEXT);
        return new BotResponse(String.format("Введите название вопроса для теста “%s”", selectedTest.getTitle()));
    }
}
