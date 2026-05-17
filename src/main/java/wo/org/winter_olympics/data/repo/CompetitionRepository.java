package wo.org.winter_olympics.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import wo.org.winter_olympics.data.entity.CompetitionEntity;

public interface CompetitionRepository extends JpaRepository<CompetitionEntity, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
