package net.wickedshell.ticketz.adapter.jpa.repository;

import net.wickedshell.ticketz.adapter.jpa.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for ProjectEntity.
 */
@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    
    /**
     * Find project by unique code.
     * 
     * @param code the project code
     * @return Optional containing the project entity if found
     */
    Optional<ProjectEntity> findByCode(String code);
}
