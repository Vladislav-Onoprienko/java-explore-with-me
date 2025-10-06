package ru.practicum.explore.ewm.user;

import ru.practicum.explore.ewm.user.dto.UserDto;
import ru.practicum.explore.ewm.user.dto.NewUserRequest;
import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest userRequest);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);
}