package org.example.bot.processor.Del;

import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractCallbackProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.service.TestService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Обработчик состояния подтверждения удаления теста.
 */
@Component
public class ConfirmDelTest extends AbstractCallbackProcessor {
    /**
     * Сервис для управления состояниями.
     */
    private final StateService stateService;

    /**
     * Сервис для управления контекстом.
     */
    private final ContextService contextService;

    /**
     * Сервис для управления тестами.
     */
    private final TestService testService;

    /**
     * Конструктор для инициализации обработчика подтверждения удаления теста.
     *
     * @param stateService сервис для управления состояниями
     * @param contextService сервис для управления контекстом
     * @param testService сервис для управления тестами
     */
    public ConfirmDelTest(StateService stateService,
                          ContextService contextService,
                          TestService testService) {
        super("DEL_TEST_CONFIRM");
        this.stateService = stateService;
        this.contextService = contextService;
        this.testService = testService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        Optional<TestEntity> optionalCurrentTest = contextService.getCurrentTest(userId);
        if (optionalCurrentTest.isEmpty()) {
            return new BotResponse("Тест не найден");
        }

        TestEntity currentTest = optionalCurrentTest.get();
        if (currentTest.getId() == Integer.parseInt(parts[1])) {
            stateService.changeStateById(userId, UserState.DEFAULT);
            if (!parts[2].equals("YES")) {
                return new BotResponse(String.format("Тест “%s” не удалён", currentTest.getTitle()));
            }
            contextService.setCurrentTest(userId, null);
            contextService.setCurrentQuestion(userId, null);
            testService.delete(currentTest);
            return new BotResponse(String.format("Тест “%s” удалён", currentTest.getTitle()));
        }
        return new BotResponse("");
    }
}
