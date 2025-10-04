package ru.practicum.explore.user;

import ru.practicum.explore.user.dto.UserDto;
import ru.practicum.explore.user.dto.NewUserRequest;
import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest userRequest);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);
}