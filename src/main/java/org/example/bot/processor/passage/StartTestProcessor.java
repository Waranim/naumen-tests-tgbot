package org.example.bot.processor.passage;

import org.example.bot.dto.InlineButtonDTO;
import org.example.bot.entity.AnswerEntity;
import org.example.bot.entity.QuestionEntity;
import org.example.bot.entity.TestEntity;
import org.example.bot.processor.AbstractCallbackProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.example.bot.util.ButtonUtils;
import org.example.bot.util.TestUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Обработать начало теста
 */
@Component
public class StartTestProcessor extends AbstractCallbackProcessor {

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Сервис для управления состояниями
     */
    private final StateService stateService;

    /**
     * Утилита для кнопок
     */
    private final ButtonUtils buttonUtils;

    /**
     * Утилита для тестов
     */
    private final TestUtils testUtils;

    /**
     * Конструктор для инициализации обработчика callback.
     */
    public StartTestProcessor(ContextService contextService,
                                 StateService stateService,
                                 ButtonUtils buttonUtils,
                                 TestUtils testUtils) {
        super("START_TEST");
        this.contextService = contextService;
        this.stateService = stateService;
        this.buttonUtils = buttonUtils;
        this.testUtils = testUtils;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        Optional<TestEntity> testOpt = contextService.getCurrentTest(userId);
        if(testOpt.isEmpty())
            return new BotResponse("Тест не найден");

        List<QuestionEntity> questions = testOpt.get().getQuestions();
        if (questions.isEmpty()) {
            return new BotResponse("Вопросы в тесте отсутствуют.");
        }

        stateService.changeStateById(userId, UserState.PASSAGE_TEST);
        contextService.setCurrentQuestion(userId, questions.getFirst());
        contextService.clearCorrectAnswerCount(userId);
        contextService.clearCountAnsweredQuestions(userId);
        return createQuestionMessage(questions);
    }

    /**
     * Создать сообщение с вопросом
     */
    private BotResponse createQuestionMessage(List<QuestionEntity> questions) {
        QuestionEntity question = questions.getFirst();
        List<InlineButtonDTO> buttons = new ArrayList<>();
        String textQuestion = testUtils.createTextQuestion(0, questions);
        List<AnswerEntity> answers = question.getAnswers();

        for (int i = 0; i < answers.size(); i++) {
            AnswerEntity answer = answers.get(i);
            buttons.add(new InlineButtonDTO(String.valueOf(i + 1), "ANSWER_QUESTION " + answer.getAnswerText()));
        }

        return new BotResponse(
                textQuestion,
                buttonUtils.createKeyboardForTest(0, buttons, "EDIT TEST", false),
                true);
    }
}
