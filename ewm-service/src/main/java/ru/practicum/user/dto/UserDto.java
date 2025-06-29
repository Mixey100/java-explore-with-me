package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;

    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 2, max = 250)
    String name;

    @NotBlank(message = "Почта не должна быть пустой")
    @Email(message = "Формат почты должен соответствовть шаблону name@domain.xx")
    @Size(min = 6, max = 254)
    String email;
}
