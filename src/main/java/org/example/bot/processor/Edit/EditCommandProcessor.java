package org.example.bot.processor.Edit;

import org.example.bot.dto.InlineButtonDTO;
import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractCommandProcessor;
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
 * Обработчик команды редактирования теста.
 */
@Component
public class EditCommandProcessor extends AbstractCommandProcessor {
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
     * Сервис для управления состояниями.
     */
    private final StateService stateService;

    /**
     * Конструктор для инициализации обработчика команды редактирования теста.
     * 
     * @param testService сервис для управления тестами
     * @param numberUtils утилита с вспомогательными числовыми методами
     * @param contextService сервис для управления контекстом
     * @param stateService сервис для управления состояниями
     */
    public EditCommandProcessor(TestService testService,
                                NumberUtils numberUtils,
                                ContextService contextService,
                                StateService stateService) {
        super("/edit");
        this.testService = testService;
        this.numberUtils = numberUtils;
        this.contextService = contextService;
        this.stateService = stateService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        Optional<List<TestEntity>> testsOptional = testService.getTestsByUserId(userId);
        if (parts.length == 1)
            return new BotResponse("Используйте команду вместе с идентификатором теста!");
        else if (!numberUtils.isNumber(parts[1]))
            return new BotResponse("Ошибка ввода!");

        Long testId = Long.parseLong(parts[1]);
        List<List<InlineButtonDTO>> buttons = new ArrayList<>();
        buttons.add(List.of(new InlineButtonDTO("Название теста",
                "EDIT_TEST " + testId + " EDIT_TEST_TITLE")));

        buttons.add(List.of(new InlineButtonDTO("Описание теста",
                "EDIT_TEST " + testId + " EDIT_TEST_DESCRIPTION")));
        Optional<TestEntity> testOptional = testService.getTest(testId);
        if (testOptional.isEmpty() || testsOptional.isEmpty() || !testsOptional.get().contains(testOptional.get()))
            return new BotResponse("Тест не найден!");

        TestEntity test = testOptional.get();
        contextService.setCurrentTest(userId, test);
        stateService.changeStateById(userId, UserState.EDIT_TEST);
        return new BotResponse(
                String.format("Вы выбрали тест “%s”. Что вы хотите изменить?", test.getTitle()),
                buttons,
                false);
    }
}
