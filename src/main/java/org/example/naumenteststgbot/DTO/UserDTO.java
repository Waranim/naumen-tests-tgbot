package org.example.naumenteststgbot.DTO;

/**
 * DTO Пользователя телеграм
 * @param id телеграм идентификатор
 * @param username псевдоним в телеграм
 */
public record UserDTO(Long id, String username) {
}
