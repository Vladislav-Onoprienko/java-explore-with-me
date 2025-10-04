package ru.practicum.explore.category;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explore.category.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Boolean existsByName(String name);

    List<Category> findAllBy(Pageable pageable);

    Boolean existsByNameAndIdIsNot(String name, Long id);
}
