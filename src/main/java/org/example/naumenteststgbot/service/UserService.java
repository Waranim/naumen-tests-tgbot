package org.example.naumenteststgbot.service;

import org.example.naumenteststgbot.DTO.UserDTO;
import org.example.naumenteststgbot.entity.*;
import org.example.naumenteststgbot.repository.UserRepository;
import org.example.naumenteststgbot.repository.UserSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Взаимодействие с сущностью пользователя
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

    public UserService(UserRepository userRepository, UserSessionRepository userSessionRepository) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
    }

    /**
     * Создание записи о пользователе в базе данных
     * @param id идентификатор телеграм
     * @param username псевдоним пользователя в телеграм
     */
    public void create(Long id, String username) {
        UserDTO user = get(id);
        if (user != null) {
            return;
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setId(id);
        userEntity.setUsername(username);
        userEntity.setSession(new UserSession(id));
        userRepository.save(userEntity);
    }

    /**
     * Получение пользователя из базы данных
     * @param id идентификатор телеграм
     * @return объект UserDTO, содержащий информацию о пользователе, или null, если пользователь не найден
     */
    public UserDTO get(Long id) {
        UserEntity user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }

        return new UserDTO(user.getId(), user.getUsername());
    }

    /**
     * Получить текущую сессию пользователя
     * @param id идентификатор пользователя
     */
    public UserSession getSession(Long id) {
        UserEntity user = getUserById(id);
        if (user == null) return null;
        return user.getSession();
    }

    private UserEntity getUserById(Long id) {
        UserEntity user = userRepository.findById(id).orElse(null);
        if (user == null) {
            log.error("Пользователь с id: {} не найден", id);
        }
        return user;
    }

    private void updateUser(UserEntity user){
        userRepository.save(user);
        userSessionRepository.save(user.getSession());
    }

    @Transactional
    public void setState(Long id, UserState state) {
        UserEntity user = getUserById(id);
        if (user == null) return;
        user.getSession().setState(state);
        updateUser(user);
    }

    @Transactional
    public void setCurrentTest(Long id, TestEntity testEntity) {
        UserEntity user = getUserById(id);
        if (user == null) return;
        user.getSession().setCurrentTest(testEntity);
        updateUser(user);
    }

    @Transactional
    public void setEditingAnswerIndex(Long id, Integer editingAnswerIndex) {
        UserEntity user = getUserById(id);
        if (user == null) return;
        user.getSession().setEditingAnswerIndex(editingAnswerIndex);
        updateUser(user);
    }

    public List<TestEntity> getTestsById(Long id) {
        UserEntity user = getUserById(id);
        if (user == null) return null;
        return user.getTests();
    }

    public void setCurrentQuestion(Long userId, QuestionEntity question) {
        UserEntity user = getUserById(userId);
        if (user == null) return;
        user.getSession().setCurrentQuestion(question);
        updateUser(user);
    }

    public QuestionEntity getCurrentQuestion(Long userId) {
        UserEntity user = getUserById(userId);
        if (user == null) return null;
        return user.getSession().getCurrentQuestion();
    }
}
