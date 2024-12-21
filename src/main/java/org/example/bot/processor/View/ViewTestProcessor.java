package org.example.bot.processor.View;

import org.example.bot.processor.AbstractStateProcessor;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

/**
 * Обработчик состояния просмотра тестов.
 */
@Component
public class ViewTestProcessor extends AbstractStateProcessor {
    /**
     * Обработчик команды просмотра тестов.
     */
    private final ViewCommandProcessor viewCommandProcessor;

    /**
     * Конструктор для инициализации обработчика состояния просмотра тестов.
     * 
     * @param stateService сервис для управления состояниями
     * @param viewCommandProcessor обработчик команды просмотра тестов
     */
    public ViewTestProcessor(StateService stateService,
                             ViewCommandProcessor viewCommandProcessor) {
        super(stateService, UserState.VIEW_TEST);
        this.viewCommandProcessor = viewCommandProcessor;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        return viewCommandProcessor.process(userId, "/view " + message);
    }
}
