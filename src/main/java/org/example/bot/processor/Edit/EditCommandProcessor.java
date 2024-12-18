package org.example.bot.processor.Edit;

import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractCommandProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.service.TestService;
import org.example.bot.state.UserState;
import org.example.bot.util.NumberUtils;
import org.springframework.stereotype.Component;

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
    public String process(Long userId, String message) {
        String[] parts = message.split(" ");
        List<TestEntity> tests = testService.getTestsByUserId(userId);
        if (parts.length == 1)
            return "Используйте команду вместе с идентификатором теста!";
        else if (!numberUtils.isNumber(parts[1]))
            return "Ошибка ввода!";
        Long testId = Long.parseLong(parts[1]);
        Optional<TestEntity> testOptional = testService.getTest(testId);
        if (testOptional.isEmpty() || !tests.contains(testOptional.get()))
            return "Тест не найден!";

        TestEntity test = testOptional.get();
        contextService.setCurrentTest(userId, test);
        stateService.changeStateById(userId, UserState.EDIT_TEST);
        return String.format("""
                Вы выбрали тест “%s”. Что вы хотите изменить?
                1: Название теста
                2: Описание теста
                """, test.getTitle());
    }
}
