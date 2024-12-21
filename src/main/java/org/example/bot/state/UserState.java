package org.example.bot.state;

/**
 * Перечисление состояний пользователя в системе
 */
public enum UserState {
    /** Состояние по умолчанию */
    DEFAULT,

    /** Добавление нового вопроса */
    ADD_QUESTION,

    /** Добавление названия теста */
    ADD_TEST_TITLE,

    /** Добавление описания теста */
    ADD_TEST_DESCRIPTION,

    /** Редактирование теста */
    EDIT_TEST,

    /** Редактирование названия теста */
    EDIT_TEST_TITLE,

    /** Выбор теста для удаления */
    CHOOSE_DELETE_TEST,

    /** Удаление теста */
    DELETE_TEST,

    /** Подтверждение удаления теста */
    CONFIRM_DELETE_TEST,

    /** Редактирование описания теста */
    EDIT_TEST_DESCRIPTION,

    /** Добавление текста вопроса */
    ADD_QUESTION_TEXT,

    /** Добавление варианта ответа */
    ADD_ANSWER,

    /** Удаление вопроса */
    DELETE_QUESTION,

    /** Подтверждение удаления вопроса */
    CONFIRM_DELETE_QUESTION,

    /** Установка правильного ответа */
    SET_CORRECT_ANSWER,

    /** Редактирование вопроса */
    EDIT_QUESTION,

    /** Редактирование текста вопроса */
    EDIT_QUESTION_TEXT,

    /** Выбор варианта ответа для редактирования */
    EDIT_ANSWER_OPTION_CHOICE,

    /** Выбор варианта ответа для редактирования текста ответа*/
    EDIT_ANSWER_TEXT_CHOICE,

    /** Редактирование текста ответа */
    EDIT_ANSWER_TEXT,

    /** Просмотр теста */
    VIEW_TEST,

    /** Прохождение теста */
    PASSAGE_TEST,
}
