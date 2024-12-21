package org.example.bot.util;

import org.example.bot.dto.InlineButtonDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Утилитарный класс для работы с кнопками
 */
@Component
public class ButtonUtils {

    /**
     * Создать inline клавиатуру для вопросов теста
     *
     * @param correctAnswerCount количество верно решённых вопросов
     * @param answerButtons кнопки вариантов ответа
     * @param prefix префикс для callback
     * @param hiddenNextButton скрытие кнопки перехода на след. вопрос
     * @return список кнопок для инлайн клавиатуры
     */
    public List<List<InlineButtonDTO>> createKeyboardForTest(int correctAnswerCount,
                                                             List<InlineButtonDTO> answerButtons,
                                                             String prefix,
                                                             boolean hiddenNextButton) {
        List<List<InlineButtonDTO>> buttons = new ArrayList<>();

        for (InlineButtonDTO button : answerButtons) {
            buttons.add(List.of(button));
        }

        if (prefix.isEmpty() && !hiddenNextButton) {
            buttons.add(List.of(
                    new InlineButtonDTO("След. вопрос", "NEXT_QUESTION")));
        }

        buttons.add(List.of(
                new InlineButtonDTO(String.format("Верно: %d", correctAnswerCount), "IGNORE")));

        buttons.add(List.of(
                new InlineButtonDTO("Выйти", "EXIT_TEST"),
                new InlineButtonDTO("Завершить", "FINISH_TEST")));

        return buttons;
    }
}
