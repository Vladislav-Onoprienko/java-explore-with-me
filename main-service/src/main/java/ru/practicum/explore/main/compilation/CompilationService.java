package ru.practicum.explore.main.compilation;

import ru.practicum.explore.main.compilation.dto.NewCompilationDto;
import ru.practicum.explore.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explore.main.compilation.dto.CompilationDto;

import java.util.List;

public interface CompilationService {

    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compilationId);

    CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest updateRequest);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationById(Long compilationId);
}
