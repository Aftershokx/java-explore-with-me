package ru.practicum.main_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_server.dto.UserRequestDto;
import ru.practicum.main_server.dto.UserResponseDto;
import ru.practicum.main_server.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "admin/users")
public class UserAdminController {
    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponseDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0", required = false) int from,
            @RequestParam(defaultValue = "10", required = false) int size) {
        log.info("Get Users(), ids " + ids + " , from " + from + " , size" + size);
        return userService.getUsers(ids, from, size);
    }

    @PostMapping
    public UserResponseDto saveUser(@RequestBody UserRequestDto userRequestDto) {
        log.info("Post createUser(), body " + userRequestDto);
        return userService.saveUser(userRequestDto);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Delete delete user(), userId " + userId);
        userService.deleteUser(userId);
    }
}
