package ru.practicum.explore.main.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.main.user.dto.NewUserRequest;
import ru.practicum.explore.main.user.dto.UserDto;
import ru.practicum.explore.main.user.dto.UserShortDto;
import ru.practicum.explore.main.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest newUserRequest);

    UserDto toDto(User user);

    UserShortDto toShortDto(User user);
}