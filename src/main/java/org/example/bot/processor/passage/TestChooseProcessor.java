package org.example.bot.processor.passage;

import org.example.bot.dto.InlineButtonDTO;
import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractCallbackProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.TestService;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик состояния просмотра тестов.
 */
@Component
public class TestChooseProcessor extends AbstractCallbackProcessor {

    /**
     * Сервис для управления тестами.
     */
    private final TestService testService;

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Конструктор для инициализации обработчика callback.
     */
    protected TestChooseProcessor(TestService testService, ContextService contextService) {
        super("TEST_CHOOSE");
        this.testService = testService;
        this.contextService = contextService;
    }

    @Override
    public BotResponse process(Long userId, String callback) {
        Long testId = Long.parseLong(callback.split(" ")[1]);

        Optional<TestEntity> test = testService.getTest(testId);
        if (test.isEmpty()) {
            return new BotResponse("Тест с указанным ID не найден.");
        }

        contextService.setCurrentTest(userId, test.get());
        return new BotResponse(String.format(
                "Вы выбрали тест “%s”. Всего вопросов: %d.",
                test.get().getTitle(),
                test.get().getQuestions().size()),
                List.of(List.of(new InlineButtonDTO("Начать", "START_TEST"))),
                false);
    }
}
