package org.example.bot.processor.Add;

import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractCommandProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.service.TestService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;


/**
 * Обработчик команды добавления теста.
 */
@Component
public class AddCommandProcessor extends AbstractCommandProcessor {

    /**
     * Сервис для управления тестами.
     */
    private final TestService testService;

    /**
     * Сервис для управления состояниями.
     */
    private final StateService stateService;

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Конструктор для инициализации обработчика команды добавления теста.
     * 
     * @param testService сервис для управления тестами
     * @param stateService сервис для управления состояниями
     * @param contextService сервис для управления контекстом
     */
    public AddCommandProcessor(TestService testService,
                               StateService stateService,
                               ContextService contextService) {
        super("/add");
        this.testService = testService;
        this.stateService = stateService;
        this.contextService = contextService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        TestEntity test = testService.createTest(userId);
        stateService.changeStateById(userId, UserState.ADD_TEST_TITLE);
        contextService.setCurrentTest(userId, test);
        return new BotResponse("Введите название теста");
    }
}
