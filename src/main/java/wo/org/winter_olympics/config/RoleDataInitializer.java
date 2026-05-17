package wo.org.winter_olympics.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import wo.org.winter_olympics.data.entity.AppUserEntity;
import wo.org.winter_olympics.data.entity.UserRoleEntity;
import wo.org.winter_olympics.data.entity.enums.UserRole;
import wo.org.winter_olympics.data.repo.AppUserRepository;
import wo.org.winter_olympics.data.repo.UserRoleRepository;

@Component
public class RoleDataInitializer implements ApplicationRunner {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public RoleDataInitializer(
            AppUserRepository appUserRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        UserRoleEntity adminRole = createRoleIfMissing(UserRole.ADMIN);
        createRoleIfMissing(UserRole.ATHLETE);
        createDefaultAdminIfMissing(adminRole);
    }

    private UserRoleEntity createRoleIfMissing(UserRole userRole) {
        return userRoleRepository.findByName(userRole).orElseGet(() -> {
            UserRoleEntity roleEntity = new UserRoleEntity();
            roleEntity.setName(userRole);

            return userRoleRepository.save(roleEntity);
        });
    }

    private void createDefaultAdminIfMissing(UserRoleEntity adminRole) {
        if (appUserRepository.existsByUsername(DEFAULT_ADMIN_USERNAME)) {
            return;
        }

        AppUserEntity admin = new AppUserEntity();
        admin.setUsername(DEFAULT_ADMIN_USERNAME);
        admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        admin.setRole(adminRole);
        admin.setEnabled(true);

        appUserRepository.save(admin);
    }
}
