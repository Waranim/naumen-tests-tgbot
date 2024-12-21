package org.example.bot.processor.Del;

import org.example.bot.dto.InlineButtonDTO;
import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractStateProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.service.TestService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.example.bot.util.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Обработчик состояния удаления теста.
 */
@Component
public class DelTestProcessor extends AbstractStateProcessor {
    /**
     * Сервис для управления состояниями.
     */
    private final StateService stateService;
    
    /**
     * Сервис для управления тестами.
     */
    private final TestService testService;
    
    /**
     * Утилита с вспомогательными числовыми методами.
     */
    private final NumberUtils numberUtils;
    
    /**
     * Сервис для управления контекстом.
     */
    private final ContextService contextService;

    /**
     * Конструктор для инициализации обработчика удаления теста.
     * 
     * @param stateService сервис для управления состояниями
     * @param testService сервис для управления тестами
     * @param numberUtils утилита с вспомогательными числовыми методами
     * @param contextService сервис для управления контекстом
     */
    public DelTestProcessor(StateService stateService,
                            TestService testService,
                            NumberUtils numberUtils,
                            ContextService contextService) {
        super(stateService, UserState.DELETE_TEST);
        this.stateService = stateService;
        this.testService = testService;
        this.numberUtils = numberUtils;
        this.contextService = contextService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        if(!numberUtils.isNumber(message))
            return new BotResponse("Введите число!");

        Long testId = Long.parseLong(parts[0]);
        List<List<InlineButtonDTO>> buttons = new ArrayList<>();
        buttons.add(List.of(new InlineButtonDTO("Да", "DEL_TEST_CONFIRM " + testId + " YES"),
                new InlineButtonDTO("Нет", "DEL_TEST_CONFIRM " + testId + " NO")));
        Optional<List<TestEntity>> tests = testService.getTestsByUserId(userId);
        Optional<TestEntity> testOptional = testService.getTest(testId);
        if (testOptional.isEmpty() || tests.isEmpty() || !tests.get().contains(testOptional.get()))
            return new BotResponse("Тест не найден!");

        TestEntity test = testOptional.get();
        contextService.setCurrentTest(userId, test);
        stateService.changeStateById(userId, UserState.CONFIRM_DELETE_TEST);
        return new BotResponse(
                String.format("Тест “%s” будет удалён, вы уверены? (Да/Нет)", test.getTitle()),
                buttons,
                false);
    }
}
