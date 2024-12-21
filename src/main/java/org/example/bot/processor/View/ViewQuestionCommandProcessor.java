package org.example.bot.processor.View;

import org.example.bot.entity.QuestionEntity;
import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractCommandProcessor;
import org.example.bot.service.TestService;
import org.example.bot.telegram.BotResponse;
import org.example.bot.util.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды просмотра вопросов
 */
@Component
public class ViewQuestionCommandProcessor extends AbstractCommandProcessor {

    /**
     * Сервис для управления тестами
     */
    private final TestService testService;

    /**
     * Утилита с вспомогательными числовыми методами
     */
    private final NumberUtils numberUtils;

    /**
     * Конструктор для инициализации обработчика команды просмотра вопросов
     *
     * @param testService сервис для управления тестами
     * @param numberUtils утилита с вспомогательными числовыми методами
     */
    public ViewQuestionCommandProcessor(TestService testService,
                                        NumberUtils numberUtils) {
        super("/view_question");
        this.testService = testService;
        this.numberUtils = numberUtils;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        if (parts.length == 1) {
            return new BotResponse("Используйте команду вместе с идентификатором вопроса!");
        }

        if (!numberUtils.isNumber(parts[1])) {
            return new BotResponse("Ошибка ввода. Укажите корректный id теста.");

        }

        Long testId = Long.parseLong(parts[1]);
        Optional<TestEntity> testOptional = testService.getTest(testId);
        if (testOptional.isEmpty() || !testOptional.get().getCreatorId().equals(userId)) {
            return new BotResponse("Тест не найден!");
        }

        TestEntity test = testOptional.get();
        List<QuestionEntity> questions = test.getQuestions();
        if (questions.isEmpty()) {
            return new BotResponse("В этом тесте пока нет вопросов.");
        }

        StringBuilder response = new StringBuilder();
        response.append(String.format("Вопросы теста \"%s\":\n", test.getTitle()));
        for (int i = 0; i < questions.size(); i++) {
            QuestionEntity question = questions.get(i);
            response.append(String.format("%d) id:%d  \"%s\"\n", i + 1, question.getId(), question.getQuestion()));
        }

        return new BotResponse(response.toString());
    }
}
