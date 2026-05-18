package wo.org.winter_olympics.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import wo.org.winter_olympics.data.entity.CompetitionEntity;

import java.util.Optional;

public interface CompetitionRepository extends JpaRepository<CompetitionEntity, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<CompetitionEntity> findByName(String name);
}
