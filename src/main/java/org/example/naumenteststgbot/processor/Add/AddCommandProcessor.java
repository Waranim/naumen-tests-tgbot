package org.example.naumenteststgbot.processor.Add;

import org.example.naumenteststgbot.entity.TestEntity;
import org.example.naumenteststgbot.processor.AbstractCommandProcessor;
import org.example.naumenteststgbot.service.SessionService;
import org.example.naumenteststgbot.service.StateService;
import org.example.naumenteststgbot.service.TestService;
import org.example.naumenteststgbot.states.UserState;
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
     * Сервис для управления сессиями.
     */
    private final SessionService sessionService;

    /**
     * Конструктор для инициализации обработчика команды добавления теста.
     * 
     * @param testService сервис для управления тестами
     * @param stateService сервис для управления состояниями
     * @param sessionService сервис для управления сессиями
     */
    public AddCommandProcessor(TestService testService,
                               StateService stateService,
                               SessionService sessionService) {
        super("/add");
        this.testService = testService;
        this.stateService = stateService;
        this.sessionService = sessionService;
    }

    @Override
    public String process(Long userId, String message) {
        TestEntity test = testService.createTest(userId);
        stateService.changeStateById(userId, UserState.ADD_TEST_TITLE);
        sessionService.setCurrentTest(userId, test);
        return "Введите название теста";
    }
}
