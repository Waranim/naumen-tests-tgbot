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
 * Обработать переход на следующий вопрос
 */
@Component
public class NextQuestionProcessor extends AbstractCallbackProcessor {
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
    public NextQuestionProcessor(ContextService contextService,
                                 StateService stateService,
                                 ButtonUtils buttonUtils,
                                 TestUtils testUtils) {
        super("NEXT_QUESTION");
        this.contextService = contextService;
        this.stateService = stateService;
        this.buttonUtils = buttonUtils;
        this.testUtils = testUtils;
    }

    @Override
    public BotResponse process(Long userId, String callback) {
        Optional<UserState> currentStateOpt = stateService.getCurrentState(userId);
        if (currentStateOpt.isEmpty() || !currentStateOpt.get().equals(UserState.PASSAGE_TEST)) {
            return new BotResponse("");
        }
        Optional<TestEntity> test = contextService.getCurrentTest(userId);
        Optional<QuestionEntity> previousQuestionOpt = contextService.getCurrentQuestion(userId);
        if(test.isEmpty() || previousQuestionOpt.isEmpty())
            return new BotResponse("Произошла ошибка при прохождении теста");
        List<QuestionEntity> questions = test.get().getQuestions();
        QuestionEntity previousQuestion = previousQuestionOpt.get();
        int currentQuestionIndex = questions.indexOf(previousQuestion)+1;
        QuestionEntity currentQuestion = questions.get(currentQuestionIndex);

        String textQuestion = testUtils.createTextQuestion(currentQuestionIndex, questions);

        List<InlineButtonDTO> buttons = new ArrayList<>();
        List<AnswerEntity> answers = currentQuestion.getAnswers();
        for (int i = 0; i < answers.size(); i++) {
            AnswerEntity answer = answers.get(i);
            buttons.add(new InlineButtonDTO(String.valueOf(i + 1), "ANSWER_QUESTION " + answer.getAnswerText()));
        }

        contextService.setCurrentQuestion(userId, currentQuestion);
        return new BotResponse(
                textQuestion,
                buttonUtils.createKeyboardForTest(
                        contextService.getCorrectAnswerCount(userId).orElse(-1),
                        buttons,
                        "",
                        true
                ),
                true);
    }
}
