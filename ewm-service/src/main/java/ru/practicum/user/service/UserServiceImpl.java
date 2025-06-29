package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Запрос на получение пользователей с ids: {}, from: {}, size: {}", ids, from, size);
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<User> user;
        if (ids == null || ids.isEmpty()) {
            user = userRepository.findAll(pageable).getContent();
        } else {
            user = userRepository.findByIdIn(ids, pageable).getContent();
        }
        return user.stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Запрос на создание пользователя с name {} c email {}", userDto.getName(), userDto.getEmail());
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Пользователь с данной почтой уже существует");
        }
        User user = userRepository.save(UserMapper.mapToUser(userDto));
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Запрос на удаление пользователя с id {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с Id " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }
}
