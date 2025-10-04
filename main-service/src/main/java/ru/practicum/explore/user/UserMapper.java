package ru.practicum.explore.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.user.dto.NewUserRequest;
import ru.practicum.explore.user.dto.UserDto;
import ru.practicum.explore.user.dto.UserShortDto;
import ru.practicum.explore.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest newUserRequest);

    UserDto toDto(User user);

    UserShortDto toShortDto(User user);
}