package org.example.bot.processor.Edit;

import org.example.bot.dto.InlineButtonDTO;
import org.example.bot.entity.AnswerEntity;
import org.example.bot.entity.QuestionEntity;
import org.example.bot.processor.AbstractCallbackProcessor;
import org.example.bot.service.ContextService;
import org.example.bot.service.StateService;
import org.example.bot.state.UserState;
import org.example.bot.telegram.BotResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Oбработчик состояния редактирования ответа
 */
@Component
public class EditAnswerOptionChoiceProcessor extends AbstractCallbackProcessor {

    /**
     * Сервис для управления состояниями
     */
    private final StateService stateService;

    /**
     * Сервис для управления контекстом
     */
    private final ContextService contextService;

    /**
     * Конструктор для инициализации обработчика редактирования ответа
     *
     * @param stateService   сервис для управления состояниями
     * @param contextService сервис для управления контекстом
     */
    public EditAnswerOptionChoiceProcessor(StateService stateService,
                                           ContextService contextService) {
        super("EDIT_ANSWER_OPTION_CHOICE");
        this.stateService = stateService;
        this.contextService = contextService;
    }

    @Override
    public BotResponse process(Long userId, String message) {
        String[] parts = message.split(" ");
        Optional<QuestionEntity> optionalCurrentQuestion = contextService.getCurrentQuestion(userId);
        if (optionalCurrentQuestion.isEmpty()) {
            return new BotResponse("Вопрос не найден");
        }
        QuestionEntity currentQuestion = optionalCurrentQuestion.get();
        List<AnswerEntity> answers = currentQuestion.getAnswers();
        List<List<InlineButtonDTO>> buttons = new ArrayList<>();
        if (currentQuestion.getId() == Integer.parseInt(parts[1])) {
            if (parts[2].equals("1")) {
                stateService.changeStateById(userId, UserState.EDIT_ANSWER_TEXT_CHOICE);
                for (int i = 0; i < answers.size(); i++) {
                    AnswerEntity answer = answers.get(i);
                    String answerText = answer.isCorrect() ? answer.getAnswerText() + " (верный)" : answer.getAnswerText();
                    buttons.add(List.of(new InlineButtonDTO(answerText, "EDIT_ANSWER_TEXT_CHOICE " + i)));
                }
                return new BotResponse(
                        "Какой вариант ответа вы хотите изменить?",
                        buttons,
                        false);

            } else if (parts[2].equals("2")) {
                stateService.changeStateById(userId, UserState.SET_CORRECT_ANSWER);
                for (int i = 0; i < answers.size(); i++) {
                    AnswerEntity answer = answers.get(i);
                    String text = answer.isCorrect() ? answer.getAnswerText() + " (верный)" : answer.getAnswerText();
                    buttons.add(List.of(new InlineButtonDTO(text, "SET_CORRECT_ANSWER " + i)));
                }
                return new BotResponse(
                        "Какой вариант ответа вы хотите сделать правильным?",
                        buttons,
                        false);
            }
        }
        return new BotResponse("Некорректный ввод");
    }
}
