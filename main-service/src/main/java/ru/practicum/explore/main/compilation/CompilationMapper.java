package ru.practicum.explore.main.compilation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.explore.main.compilation.model.Compilation;
import ru.practicum.explore.main.compilation.dto.NewCompilationDto;
import ru.practicum.explore.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explore.main.compilation.dto.CompilationDto;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompilationMapper {

    @Mapping(target = "events", ignore = true)
    @Mapping(target = "id", ignore = true)
    Compilation toEntity(NewCompilationDto newCompilationDto);

    CompilationDto toDto(Compilation compilation);

    @Mapping(target = "events", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateCompilationFromRequest(UpdateCompilationRequest updateRequest, @MappingTarget Compilation compilation);
}
