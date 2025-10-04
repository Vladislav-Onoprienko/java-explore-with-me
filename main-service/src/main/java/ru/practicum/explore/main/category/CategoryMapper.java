package ru.practicum.explore.main.category;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.explore.main.category.model.Category;
import ru.practicum.explore.main.category.dto.CategoryDto;
import ru.practicum.explore.main.category.dto.NewCategoryDto;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toEntity(NewCategoryDto newCategoryDto);

    CategoryDto toDto(Category category);

    void updateCategoryFromDto(CategoryDto categoryDto, @MappingTarget Category category);
}
