package ru.practicum.explore.compilation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explore.compilation.model.Compilation;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("SELECT DISTINCT c FROM Compilation c " +
            "LEFT JOIN FETCH c.events e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    List<Compilation> findByPinnedWithEvents(@Param("pinned") Boolean pinned, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Compilation c " +
            "LEFT JOIN FETCH c.events e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator")
    List<Compilation> findAllWithEvents(Pageable pageable);

    @Query("SELECT c FROM Compilation c " +
            "LEFT JOIN FETCH c.events e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "WHERE c.id = :compilationId")
    Optional<Compilation> findByIdWithEvents(@Param("compilationId") Long compilationId);

    Boolean existsByTitle(String title);

    Boolean existsByTitleAndIdNot(String title, Long id);

}
