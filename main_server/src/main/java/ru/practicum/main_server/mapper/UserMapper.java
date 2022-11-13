package ru.practicum.main_server.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.main_server.dto.UserRequestDto;
import ru.practicum.main_server.dto.UserResponseDto;
import ru.practicum.main_server.dto.UserShortDto;
import ru.practicum.main_server.model.User;

@UtilityClass
public class UserMapper {
    public static UserResponseDto toUserDto(User user) {
        return UserResponseDto
                .builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static UserShortDto toUserShortDto(User user) {
        return UserShortDto
                .builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public static User toUser(UserRequestDto userRequestDto) {
        return User
                .builder()
                .name(userRequestDto.getName())
                .email(userRequestDto.getEmail())
                .build();
    }
}
