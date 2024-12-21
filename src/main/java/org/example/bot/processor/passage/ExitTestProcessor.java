package org.example.bot.processor.passage;

import org.example.bot.processor.AbstractCallbackProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Обработка выхода из теста
 */
@Component
public class ExitTestProcessor extends AbstractCallbackProcessor {

    /**
     * Сервис для управления состояниями
     */
    private final StateService stateService;

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Конструктор для инициализации обработчика callback.
     */
    public ExitTestProcessor(StateService stateService, ContextService contextService) {
        super("EXIT_TEST");
        this.stateService = stateService;
        this.contextService = contextService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        Optional<UserState> currentStateOpt = stateService.getCurrentState(userId);
        if (currentStateOpt.isEmpty() || !currentStateOpt.get().equals(UserState.PASSAGE_TEST)) {
            return new BotResponse("");
        }

        stateService.changeStateById(userId, UserState.DEFAULT);
        contextService.setCurrentTest(userId, null);
        contextService.setCurrentQuestion(userId, null);
        return new BotResponse("Вы вышли из теста");
    }
}
