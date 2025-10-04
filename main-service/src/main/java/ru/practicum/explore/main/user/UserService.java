package ru.practicum.explore.main.user;

import ru.practicum.explore.main.user.dto.UserDto;
import ru.practicum.explore.main.user.dto.NewUserRequest;
import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest userRequest);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);
}