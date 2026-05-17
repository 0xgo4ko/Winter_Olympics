package wo.org.winter_olympics.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import wo.org.winter_olympics.data.entity.AppUserEntity;
import wo.org.winter_olympics.data.entity.CompetitionEntity;
import wo.org.winter_olympics.data.entity.UserRoleEntity;
import wo.org.winter_olympics.data.entity.enums.CompetitionStatus;
import wo.org.winter_olympics.data.entity.enums.CompetitionType;
import wo.org.winter_olympics.data.entity.enums.Gender;
import wo.org.winter_olympics.data.entity.enums.UserRole;
import wo.org.winter_olympics.data.repo.AppUserRepository;
import wo.org.winter_olympics.data.repo.CompetitionRepository;
import wo.org.winter_olympics.data.repo.UserRoleRepository;

import java.time.LocalDate;

@Component
public class RoleDataInitializer implements ApplicationRunner {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_COMPETITION_NAME = "Men Ski Slalom";

    private final AppUserRepository appUserRepository;
    private final CompetitionRepository competitionRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public RoleDataInitializer(
            AppUserRepository appUserRepository,
            CompetitionRepository competitionRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.competitionRepository = competitionRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        UserRoleEntity adminRole = createRoleIfMissing(UserRole.ADMIN);
        createRoleIfMissing(UserRole.ATHLETE);
        createDefaultAdminIfMissing(adminRole);
        createDefaultCompetitionIfMissing();
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

    private void createDefaultCompetitionIfMissing() {
        if (competitionRepository.existsByName(DEFAULT_COMPETITION_NAME)) {
            return;
        }

        CompetitionEntity competition = new CompetitionEntity();
        competition.setName(DEFAULT_COMPETITION_NAME);
        competition.setType(CompetitionType.SKI_SLALOM);
        competition.setGender(Gender.MALE);
        competition.setMinimumAge(18);
        competition.setRegistrationDeadline(LocalDate.now().plusMonths(1));
        competition.setStatus(CompetitionStatus.OPEN);
        competition.setSecondRunQualifierCount(30);

        competitionRepository.save(competition);
    }
}
