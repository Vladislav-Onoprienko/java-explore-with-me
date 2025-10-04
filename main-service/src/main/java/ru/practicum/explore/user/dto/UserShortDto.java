package ru.practicum.explore.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {
    private Long id;

    @NotBlank
    @Size(min = 2, max = 250)
    private String name;
}