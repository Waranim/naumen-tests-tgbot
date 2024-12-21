package org.example.bot.processor.View;

import org.example.bot.entity.AnswerEntity;
import org.example.bot.entity.QuestionEntity;
import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractCommandProcessor;
import org.example.bot.service.StateService;
import org.example.bot.service.TestService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.example.bot.util.NumberUtils;
import org.example.bot.util.TestUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды просмотра тестов.
 */
@Component
public class ViewCommandProcessor extends AbstractCommandProcessor {
    /**
     * Сервис для управления тестами.
     */
    private final TestService testService;
    
    /**
     * Сервис для управления состояниями.
     */
    private final StateService stateService;
    
    /**
     * Утилита с вспомогательными числовыми методами.
     */
    private final NumberUtils numberUtils;

    /**
     * Утилита с вспомогательными методами для тестов.
     */
    private final TestUtils testUtils;

    /**
     * Конструктор для инициализации обработчика команды просмотра тестов.
     * 
     * @param testService сервис для управления тестами
     * @param stateService сервис для управления состояниями
     * @param numberUtils утилита с вспомогательными числовыми методами
     * @param testUtils утилита с вспомогательными методами для тестов
     */
    public ViewCommandProcessor(TestService testService,
                                StateService stateService,
                                NumberUtils numberUtils,
                                TestUtils testUtils) {
        super("/view");
        this.testService = testService;
        this.stateService = stateService;
        this.numberUtils = numberUtils;
        this.testUtils = testUtils;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        Optional<List<TestEntity>> testsOptional = testService.getTestsByUserId(userId);

        if (parts.length == 1) {
            stateService.changeStateById(userId, UserState.VIEW_TEST);
            String text = testsOptional.isPresent()? testUtils.testsToString(testsOptional.get()) : "";
            return new BotResponse("Выберите тест для просмотра:\n"
                    + text);
        } else if (numberUtils.isNumber(parts[1])){
            stateService.changeStateById(userId, UserState.DEFAULT);
            Long testId = Long.parseLong(parts[1]);
            Optional<TestEntity> test = testService.getTest(testId);
            if (test.isEmpty() || testsOptional.isEmpty() || !testsOptional.get().contains(test.get()))
                return new BotResponse("Тест не найден!");
            return new BotResponse(testToString(test.get()));
        }
        return new BotResponse("Ошибка ввода!");
    }

    /**
     * Получить развернутое строковое представление сущности теста
     */
    private String testToString(TestEntity test) {
        List<QuestionEntity> questions = test.getQuestions();
        StringBuilder response = new StringBuilder(String.format("Тест “%s”. Всего вопросов: %s\n",
                test.getTitle(),
                questions.size()));

        for (QuestionEntity question : questions) {
            response.append("Вопрос: %s\nВарианты ответов:\n"
                    .formatted(question.getQuestion()));
            List<AnswerEntity> answers = question.getAnswers();
            AnswerEntity correctAnswer = null;

            for (int i = 0; i < answers.size(); i++) {
                var answer = answers.get(i);
                response.append("%s - %s\n"
                        .formatted(i + 1, answer.getAnswerText()));
                if(answer.isCorrect()) correctAnswer = answer;
            }
            response.append("Правильный вариант: ")
                    .append(correctAnswer != null ? correctAnswer
                            .getAnswerText() : null).append("\n\n");
        }
        return response.toString();
    }
}
