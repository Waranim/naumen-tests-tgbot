package org.example.bot.processor.Edit;

import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractStateProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.service.TestService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Обработчик состояния редактирования описания теста.
 */
@Component
public class EditTestDescriptionProcessor extends AbstractStateProcessor {
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
     * Конструктор для инициализации обработчика редактирования описания теста.
     * 
     * @param stateService сервис для управления состояниями
     * @param contextService сервис для управления контекстом
     * @param testService сервис для управления тестами
     */
    public EditTestDescriptionProcessor(StateService stateService,
                                        ContextService contextService,
                                        TestService testService) {
        super(stateService, UserState.EDIT_TEST_DESCRIPTION);
        this.stateService = stateService;
        this.contextService = contextService;
        this.testService = testService;
    }


    @Override
    public BotResponse process(Long userId, String message) {
        Optional<TestEntity> optionalCurrentTest = contextService.getCurrentTest(userId);
        if (optionalCurrentTest.isEmpty()) {
            return new BotResponse("Тест не найден");
        }

        TestEntity currentTest = optionalCurrentTest.get();
        currentTest.setDescription(message);
        testService.update(currentTest);
        stateService.changeStateById(userId, UserState.DEFAULT);

        return new BotResponse(String.format("Описание изменено на “%s”", message));
    }
}
