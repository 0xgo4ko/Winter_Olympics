package wo.org.winter_olympics.data.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import wo.org.winter_olympics.data.entity.AppUserEntity;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {

    @EntityGraph(attributePaths = "role")
    Optional<AppUserEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
