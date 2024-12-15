package org.example.bot.service;

import org.example.bot.entity.*;
import org.example.bot.states.UserState;
import org.example.bot.repository.AnswerRepository;
import org.example.bot.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Класс для управления вопросами
 * Предостволяет функционал добавления, редактирования, просмотра и удаления вопросов
 */
@Service
public class QuestionService {

    /**
     * Репозиторий для взаимодействия над сущностью вопросов в базе данных
     */
    private final QuestionRepository questionRepository;

    /**
     * Репозиторий для взаимодействия над сущностью ответов в базе данных
     */
    private final AnswerRepository answerRepository;

    /**
     * Сервис для взаимодействия с тестами
     */
    private final TestService testService;
    /**
     * Сервис для управления состояниями пользователей
     */
    private final StateService stateService;

    /**
     * Сервис для управления сессиями пользователей
     */
    private final SessionService sessionService;

    /**
     * Конструктор для инициализации сервисов и репозиториев
     */
    public QuestionService(QuestionRepository questionRepository,
                           AnswerRepository answerRepository,
                           TestService testService,
                           StateService stateService,
                           SessionService sessionService) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.testService = testService;
        this.stateService = stateService;
        this.sessionService = sessionService;
    }

    /**
     * Управляет переходами между состояниями пользователя в зависимости от полученной команды
     */
    public String handleMessage(UserSession userSession, String text) {
        UserState userState = userSession.getState();
        Long userId = userSession.getUserId();
        QuestionEntity currentQuestion = userSession.getCurrentQuestion();
        String response = "Ошибка";

        switch (userState) {
            case DEFAULT:
                break;
            case ADD_QUESTION:
                if (!text.matches("^\\d+$")) {
                    return "Некорректный id теста. Пожалуйста, введите число.";
                }

                long testId = Long.parseLong(text);
                TestEntity selectedTest = testService.getTest(testId);
                if (selectedTest == null || !testService.getTestsByUserId(userId).contains(selectedTest)) {
                    return "Тест не найден!";
                }
                QuestionEntity nquestion = createQuestion(selectedTest);
                sessionService.setCurrentQuestion(userId, nquestion);
                stateService.changeStateById(userId, UserState.ADD_QUESTION_TEXT);
                return String.format("Введите название вопроса для теста “%s”", selectedTest.getTitle());

            case ADD_QUESTION_TEXT:
                currentQuestion.setQuestion(text);
                questionRepository.save(currentQuestion);
                response = "Введите вариант ответа. Если вы хотите закончить добавлять варианты, введите команду /stop.";
                stateService.changeStateById(userId, UserState.ADD_ANSWER);
                break;

            case ADD_ANSWER:
                addAnswerOption(currentQuestion, text);
                response = "Введите вариант ответа. Если вы хотите закончить добавлять варианты, введите команду “/stop”.";
                break;

            case EDIT_QUESTION:
                if (text.equals("1")) {
                    response = "Введите новый текст вопроса";
                    stateService.changeStateById(userId, UserState.EDIT_QUESTION_TEXT);
                } else if (text.equals("2")) {
                    response = "Что вы хотите сделать с вариантом ответа?\n1: Изменить формулировку ответа\n2: Изменить правильность варианта ответа";
                    stateService.changeStateById(userId, UserState.EDIT_ANSWER_OPTION_CHOICE);
                }
                break;

            case EDIT_QUESTION_TEXT:
                currentQuestion.setQuestion(text);
                questionRepository.save(currentQuestion);
                response = String.format("Текст вопроса изменен на “%s”", text);
                stateService.changeStateById(userId, UserState.DEFAULT);
                break;

            case EDIT_ANSWER_OPTION_CHOICE:
                if (text.equals("1")) {
                    response = "Сейчас варианты ответа выглядят так\n" + answersListToString(currentQuestion.getAnswers());
                    response += "\nКакой вариант ответа вы хотите изменить?";
                    stateService.changeStateById(userId, UserState.EDIT_ANSWER_TEXT_CHOICE);
                } else if (text.equals("2")) {
                    response = "Сейчас варианты ответа выглядят так:\n" + answersListToString(currentQuestion.getAnswers());
                    response += "\nКакой вариант ответа вы хотите сделать правильным?";
                    stateService.changeStateById(userId, UserState.SET_CORRECT_ANSWER);
                }
                break;

            case EDIT_ANSWER_TEXT_CHOICE:
                int answerIndex = Integer.parseInt(text) - 1;
                if (answerIndex < 0 || answerIndex >= currentQuestion.getAnswers().size()) {
                    response = "Некорректный номер ответа. Попробуйте еще раз.";
                } else {
                    sessionService.setEditingAnswerIndex(userId, answerIndex);
                    response = "Введите новую формулировку ответа";
                    stateService.changeStateById(userId, UserState.EDIT_ANSWER_TEXT);
                }
                break;

            case EDIT_ANSWER_TEXT:
                int editingAnswerIndex = userSession.getEditingAnswerIndex();
                currentQuestion.getAnswers().get(editingAnswerIndex).setAnswerText(text);
                questionRepository.save(currentQuestion);
                response = String.format("Формулировка изменена на “%s”", text);
                stateService.changeStateById(userId, UserState.DEFAULT);
                break;

            case SET_CORRECT_ANSWER:
                String setCorrectAnswer = setCorrectAnswer(currentQuestion, Integer.parseInt(text));
                if (setCorrectAnswer.startsWith("Некорректный")) {
                    response = setCorrectAnswer;
                } else {
                    response = setCorrectAnswer;
                    stateService.changeStateById(userId, UserState.DEFAULT);
                }
                break;

            case DELETE_QUESTION:
                QuestionEntity question = questionRepository.findById(Long.parseLong(text)).orElse(null);
                if (question == null) return "Вопрос не найден!";
                response = String.format("Вопрос “%s” будет удалён, вы уверены? (Да/Нет)", question.getQuestion());
                sessionService.setCurrentQuestion(userId, question);
                stateService.changeStateById(userId, UserState.CONFIRM_DELETE_QUESTION);
                break;

            case CONFIRM_DELETE_QUESTION:
                text = text.toLowerCase();
                if (text.equals("да")) {
                    sessionService.setCurrentQuestion(userId, null);
                    questionRepository.delete(currentQuestion);
                    response = String.format("Вопрос “%s” из теста “%s” удален.", currentQuestion.getQuestion(), currentQuestion.getTest().getTitle());
                } else {
                    response = String.format("Вопрос “%s” из теста “%s” не удален.", currentQuestion.getQuestion(), currentQuestion.getTest().getTitle());
                }
                stateService.changeStateById(userId, UserState.DEFAULT);
                break;
        }

        return response;
    }

    /**
     * Обрабатывает команду добавления вопроса в тест
     */
    public String handleAddQuestion(Long userId, String message) {
        String[] parts = message.split(" ");
        List<TestEntity> tests = testService.getTestsByUserId(userId);

        if (parts.length == 1) {
            if (tests.isEmpty()) {
                return "У вас нет доступных тестов для добавления вопросов.";
            }
            stateService.changeStateById(userId, UserState.ADD_QUESTION);
            return "Выберите тест:\n" + testsListToString(tests);
        }

        String testIdStr = parts[1];
        if (!testIdStr.matches("^?\\d+$")) {
            return "Ошибка ввода. Укажите корректный id теста.";
        }
        long testId = Long.parseLong(testIdStr);
        TestEntity test = testService.getTest(testId);
        if (test == null || !tests.contains(test)) {
            return "Тест не найден!";
        }

        QuestionEntity question = createQuestion(test);
        stateService.changeStateById(userId, UserState.ADD_QUESTION_TEXT);
        sessionService.setCurrentQuestion(userId, question);
        return String.format("Введите название вопроса для теста “%s”", test.getTitle());
    }

    /**
     * Обрабатывает команду просмотра вопросов в тесте
     */
    public String handleViewQuestions(Long userId, String message) {
        String[] parts = message.split(" ");
        if (parts.length == 1) {
            return "Используйте команду вместе с идентификатором вопроса!";
        }
        if (parts[1].matches("^\\d+$")) {
            Long testId = Long.parseLong(parts[1]);
            TestEntity test = testService.getTest(testId);
            if (test == null || !test.getCreatorId().equals(userId)) {
                return "Тест не найден!";
            }
            List<QuestionEntity> questions = test.getQuestions();
            if (questions.isEmpty()) {
                return "В этом тесте пока нет вопросов.";
            }
            StringBuilder response = new StringBuilder();
            response.append(String.format("Вопросы теста \"%s\":\n", test.getTitle()));
            for (int i = 0; i < questions.size(); i++) {
                QuestionEntity question = questions.get(i);
                response.append(String.format("%d) id:%d  \"%s\"\n", i + 1, question.getId(), question.getQuestion()));
            }
            return response.toString();
        }

        return "Ошибка ввода. Укажите корректный id теста.";
    }

    /**
     * Обрабатывает команду редактирования вопроса
     */
    public String handleEditQuestion(Long userId, String message) {
        String[] parts = message.split(" ");
        if (parts.length == 1) {
            return "Используйте команду вместе с идентификатором вопроса!";
        }
        if (parts[1].matches("^\\d+$")) {
            Long questionId = Long.parseLong(parts[1]);
            QuestionEntity question = questionRepository.findById(questionId).orElse(null);
            if (question == null) {
                return "Вопрос не найден!";
            }
            sessionService.setCurrentQuestion(userId, question);
            stateService.changeStateById(userId, UserState.EDIT_QUESTION);
            return String.format("""
                    Вы выбрали вопрос “%s”. Что вы хотите изменить в вопросе?
                    1: Формулировку вопроса
                    2: Варианты ответа
                    """, question.getQuestion());
        }
        return "Ошибка ввода. Укажите корректный id теста.";
    }

    /**
     * Обрабатывает команду удаления вопроса
     */
    public String handleDeleteQuestion(Long userId, String message) {
        UserSession userSession = sessionService.getSession(userId);
        if (userSession.getState() == UserState.CONFIRM_DELETE_QUESTION) {
            QuestionEntity question = userSession.getCurrentQuestion();
            if (question == null) {
                return "Вопрос не найден!";
            }
            message = message.toLowerCase();
            if (message.equals("да")) {
                sessionService.setCurrentQuestion(userId, null);
                questionRepository.delete(question);
                stateService.changeStateById(userId, UserState.DEFAULT);
                return "Вопрос успешно удален.";
            } else if (message.equals("нет")) {
                stateService.changeStateById(userId, UserState.DEFAULT);
                return "Удаление вопроса отменено.";
            } else {
                return "Некорректный ввод. Пожалуйста, введите 'Да' или 'Нет'.";
            }
        }
        String[] parts = message.split(" ");
        if (parts.length == 2) {
            String questionIdStr = parts[1];
            if (!questionIdStr.matches("^\\d+$")) {
                return "Некорректный формат id вопроса. Пожалуйста, введите число.";
            }
            Long questionId = Long.parseLong(questionIdStr);
            QuestionEntity question = questionRepository.findById(questionId).orElse(null);
            if (question == null) {
                return "Вопрос не найден!";
            }
            sessionService.setCurrentQuestion(userId, question);
            stateService.changeStateById(userId, UserState.CONFIRM_DELETE_QUESTION);
            return String.format("Вопрос “%s” будет удалён, вы уверены? (Да/Нет)", question.getQuestion());
        }
        if (parts.length == 1){
                stateService.changeStateById(userId, UserState.DELETE_QUESTION);
                return "Введите id вопроса для удаления:\n";
        }
        return "Ошибка ввода. Укажите корректный id теста.";
    }

    /**
     * Завершает добавление вариантов ответа и переходит к выбору правильного ответа
     */
    public String handleStop(Long userId) {
        UserState userState = sessionService.getSession(userId).getState();
        QuestionEntity currentQuestion = sessionService.getCurrentQuestion(userId);
        if (currentQuestion == null) {
            return "Нет текущего вопроса. Пожалуйста, выберите или создайте вопрос.";
        }
        List<AnswerEntity> answers = currentQuestion.getAnswers();
        if (userState == UserState.ADD_ANSWER) {
            if (answers.size() < 2) {
                return "Вы не создали необходимый минимум ответов (минимум: 2). Введите варианты ответа.";
            }
            stateService.changeStateById(userId, UserState.SET_CORRECT_ANSWER);
            return "Укажите правильный вариант ответа:\n" + answersListToString(currentQuestion.getAnswers());
        }
        return "Команда /stop используется только при создании вопроса";
    }

    /**
     * Создает новый вопрос в заданном тесте и сохраняет его в базе данных
     */
    private QuestionEntity createQuestion(TestEntity test) {
        QuestionEntity question = new QuestionEntity(test);
        questionRepository.save(question);

        return question;
    }

    /**
     * Добавляет новый вариант ответа к текущему вопросу
     */
    private void addAnswerOption(QuestionEntity question, String answerText) {
        AnswerEntity newAnswer = new AnswerEntity(answerText);
        newAnswer.setQuestion(question);
        answerRepository.save(newAnswer);
        question.getAnswers().add(newAnswer);
    }

    /**
     * Устанавливает правильный ответ для вопроса.
     */
    private String setCorrectAnswer(QuestionEntity question, int optionIndex) {
        List<AnswerEntity> answers = question.getAnswers();
        if (optionIndex < 1 || optionIndex > answers.size()) {
            return "Некорректный номер варианта ответа. Введите число от 1 до " + answers.size();
        }
        for (int i = 0; i < answers.size(); i++) {
            answers.get(i).setCorrect(i == optionIndex - 1);
        }
        questionRepository.save(question);
        return String.format("Вариант ответа %s назначен правильным.", optionIndex);
    }

    /**
     * Преобразует список ответов у вопроса в строку
     */
    private String answersListToString(List<AnswerEntity> answers) {
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < answers.size(); i++) {
            AnswerEntity answer = answers.get(i);
            response.append(String.format("%d: %s%s\n", i + 1, answer.getAnswerText(), answer.isCorrect() ? " (верный)" : ""));
        }
        return response.toString();
    }

    /**
     * Преобразует список тестов в строку
     */
    private String testsListToString(List<TestEntity> tests) {
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < tests.size(); i++) {
            TestEntity test = tests.get(i);
            response.append(String.format("%d) id: %d Название: %s\n", i + 1, test.getId(), test.getTitle()));
        }
        return response.toString();
    }
}