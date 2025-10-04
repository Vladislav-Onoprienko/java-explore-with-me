package ru.practicum.explore.main.category;

import ru.practicum.explore.main.category.dto.CategoryDto;
import ru.practicum.explore.main.category.dto.NewCategoryDto;
import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long categoryId);

    CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto);

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategoryById(Long categoryId);
}
