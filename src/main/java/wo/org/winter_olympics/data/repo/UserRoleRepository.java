package wo.org.winter_olympics.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import wo.org.winter_olympics.data.entity.UserRoleEntity;
import wo.org.winter_olympics.data.entity.enums.UserRole;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    Optional<UserRoleEntity> findByName(UserRole name);

    boolean existsByName(UserRole name);
}
