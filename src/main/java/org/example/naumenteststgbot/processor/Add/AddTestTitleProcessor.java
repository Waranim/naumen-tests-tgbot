package org.example.naumenteststgbot.processor.Add;

import org.example.naumenteststgbot.entity.TestEntity;
import org.example.naumenteststgbot.processor.AbstractStateProcessor;
import org.example.naumenteststgbot.service.SessionService;
import org.example.naumenteststgbot.service.StateService;
import org.example.naumenteststgbot.service.TestService;
import org.example.naumenteststgbot.states.UserState;
import org.springframework.stereotype.Component;

/**
 * Обработчик состояния добавления названия теста.
 */
@Component
public class AddTestTitleProcessor extends AbstractStateProcessor {
    /**
     * Сервис для управления состояниями.
     */
    private final StateService stateService;
    
    /**
     * Сервис для управления сессиями.
     */
    private final SessionService sessionService;
    
    /**
     * Сервис для управления тестами.
     */
    private final TestService testService;

    /**
     * Конструктор для инициализации обработчика добавления названия теста.
     * 
     * @param stateService сервис для управления состояниями
     * @param sessionService сервис для управления сессиями
     * @param testService сервис для управления тестами
     */
    public AddTestTitleProcessor(StateService stateService,
                                 SessionService sessionService,
                                 TestService testService) {
        super(stateService, UserState.ADD_TEST_TITLE);
        this.stateService = stateService;
        this.sessionService = sessionService;
        this.testService = testService;
    }

    @Override
    public String process(Long userId, String message) {
        TestEntity currentTest = sessionService.getCurrentTest(userId);
        currentTest.setTitle(message);
        stateService.changeStateById(userId, UserState.ADD_TEST_DESCRIPTION);
        testService.update(currentTest);
        return "Введите описание теста";
    }
}
