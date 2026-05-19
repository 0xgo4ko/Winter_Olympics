package wo.org.winter_olympics.data.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import wo.org.winter_olympics.data.entity.CompetitionRegistrationEntity;
import wo.org.winter_olympics.data.entity.enums.CompetitionStatus;

import java.util.List;
import java.util.Optional;

public interface CompetitionRegistrationRepository extends JpaRepository<CompetitionRegistrationEntity, Long> {

    @EntityGraph(attributePaths = "competition")
    Optional<CompetitionRegistrationEntity> findByUserUsername(String username);

    @EntityGraph(attributePaths = {"user", "competition"})
    List<CompetitionRegistrationEntity> findAllByCompetitionId(Long competitionId);

    @EntityGraph(attributePaths = {"user", "competition"})
    List<CompetitionRegistrationEntity> findAllByCompetitionStatus(CompetitionStatus competitionStatus);

    @EntityGraph(attributePaths = {"user", "competition"})
    Optional<CompetitionRegistrationEntity> findWithUserAndCompetitionById(Long id);

    boolean existsByUserUsername(String username);

    boolean existsByCompetitionId(Long competitionId);
}
