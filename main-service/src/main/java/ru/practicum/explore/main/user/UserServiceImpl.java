package ru.practicum.explore.main.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.main.exception.ConflictException;
import ru.practicum.explore.main.exception.EntityNotFoundException;
import ru.practicum.explore.main.user.model.User;
import ru.practicum.explore.main.user.dto.UserDto;
import ru.practicum.explore.main.user.dto.NewUserRequest;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest userRequest) {
        log.info("Создание пользователя: {}", userRequest.getEmail());

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            log.warn("Попытка создания пользователя с существующим email: {}", userRequest.getEmail());
            throw new ConflictException("User with this email already exists");
        }

        User user = userMapper.toEntity(userRequest);
        User savedUser = userRepository.save(user);
        log.info("Пользователь создан успешно: ID {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Получение пользователей: ids={}, from={}, size={}", ids, from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAllBy(pageable);
            log.debug("Найдено всех пользователей: {}", users.size());
        } else {
            users = userRepository.findAllByIdIn(ids, pageable);
            log.debug("Найдено пользователей по ids: {}", users.size());
        }

        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя: ID {}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("Попытка удаления несуществующего пользователя: ID {}", userId);
            throw new EntityNotFoundException("User with id=" + userId + " was not found");
        }

        userRepository.deleteById(userId);
        log.info("Пользователь удален: ID {}", userId);
    }
}