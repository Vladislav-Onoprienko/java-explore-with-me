package ru.practicum.explore.main.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.main.category.model.Category;
import ru.practicum.explore.main.category.dto.CategoryDto;
import ru.practicum.explore.main.category.dto.NewCategoryDto;
import ru.practicum.explore.main.exception.ConflictException;
import ru.practicum.explore.main.exception.EntityNotFoundException;
import ru.practicum.explore.main.event.EventRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Создание категории: {}", newCategoryDto.getName());

        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            log.warn("Попытка создания категории с существующим именем: {}", newCategoryDto.getName());
            throw new ConflictException("Category with this name already exists");
        }

        Category category = categoryMapper.toEntity(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);
        log.info("Категория создана успешно: ID {}", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("Удаление категории: ID {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Попытка удаления несуществующей категории: ID {}", categoryId);
                    return new EntityNotFoundException("Category with id=" + categoryId + " was not found");
                });

        if (eventRepository.existsByCategoryId(categoryId)) {
            log.warn("Попытка удаления категории с событиями: ID {}", categoryId);
            throw new ConflictException("The category is not empty");
        }

        categoryRepository.delete(category);
        log.info("Категория удалена: ID {}", categoryId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        log.info("Обновление категории: ID {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Попытка обновления несуществующей категории: ID {}", categoryId);
                    return new EntityNotFoundException("Category with id=" + categoryId + " was not found");
                });

        if (categoryDto.getName() != null &&
                !categoryDto.getName().equals(category.getName()) &&
                categoryRepository.existsByNameAndIdIsNot(categoryDto.getName(), categoryId)) {
            log.warn("Попытка изменения имени на существующее: {}", categoryDto.getName());
            throw new ConflictException("Category with this name already exists");
        }

        categoryMapper.updateCategoryFromDto(categoryDto, category);
        Category updatedCategory = categoryRepository.save(category);
        log.info("Категория обновлена: ID {}", categoryId);
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Получение списка категорий: from={}, size={}", from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAllBy(pageable);

        log.debug("Найдено категорий: {}", categories.size());
        return categories.stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        log.info("Получение категории по ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Категория не найдена: ID {}", categoryId);
                    return new EntityNotFoundException("Category with id=" + categoryId + " was not found");
                });

        return categoryMapper.toDto(category);
    }
}
