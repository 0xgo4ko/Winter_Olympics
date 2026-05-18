package wo.org.winter_olympics.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import wo.org.winter_olympics.data.entity.AppUserEntity;
import wo.org.winter_olympics.data.entity.CompetitionEntity;
import wo.org.winter_olympics.data.entity.CompetitionRegistrationEntity;
import wo.org.winter_olympics.data.entity.UserRoleEntity;
import wo.org.winter_olympics.data.entity.enums.CompetitionStatus;
import wo.org.winter_olympics.data.entity.enums.CompetitionType;
import wo.org.winter_olympics.data.entity.enums.Gender;
import wo.org.winter_olympics.data.entity.enums.UserRole;
import wo.org.winter_olympics.data.repo.AppUserRepository;
import wo.org.winter_olympics.data.repo.CompetitionRegistrationRepository;
import wo.org.winter_olympics.data.repo.CompetitionRepository;
import wo.org.winter_olympics.data.repo.UserRoleRepository;

import java.time.LocalDate;
import java.util.List;

@Component
public class RoleDataInitializer implements ApplicationRunner {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_COMPETITION_NAME = "Men Ski Slalom";
    private static final String DEFAULT_ATHLETE_PASSWORD = "athlete123";

    private final AppUserRepository appUserRepository;
    private final CompetitionRegistrationRepository competitionRegistrationRepository;
    private final CompetitionRepository competitionRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public RoleDataInitializer(
            AppUserRepository appUserRepository,
            CompetitionRegistrationRepository competitionRegistrationRepository,
            CompetitionRepository competitionRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.competitionRegistrationRepository = competitionRegistrationRepository;
        this.competitionRepository = competitionRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        UserRoleEntity adminRole = createRoleIfMissing(UserRole.ADMIN);
        UserRoleEntity athleteRole = createRoleIfMissing(UserRole.ATHLETE);
        createDefaultAdminIfMissing(adminRole);
        CompetitionEntity competition = createDefaultCompetitionIfMissing();
        createDefaultAthletesAndRegistrations(athleteRole, competition);
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

    private CompetitionEntity createDefaultCompetitionIfMissing() {
        return competitionRepository.findByName(DEFAULT_COMPETITION_NAME)
                .map(this::updateDefaultCompetitionSettings)
                .orElseGet(this::createDefaultCompetition);
    }

    private CompetitionEntity createDefaultCompetition() {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setName(DEFAULT_COMPETITION_NAME);
        competition.setType(CompetitionType.SKI_SLALOM);
        competition.setGender(Gender.MALE);
        competition.setMinimumAge(18);
        competition.setRegistrationDeadline(LocalDate.now());
        competition.setStatus(CompetitionStatus.STARTING_SOON);
        competition.setSecondRunQualifierCount(5);

        return competitionRepository.save(competition);
    }

    private CompetitionEntity updateDefaultCompetitionSettings(CompetitionEntity competition) {
        competition.setSecondRunQualifierCount(5);

        if (competition.getStatus() == CompetitionStatus.STARTING_SOON) {
            competition.setRegistrationDeadline(LocalDate.now());
        }

        return competitionRepository.save(competition);
    }

    private void createDefaultAthletesAndRegistrations(UserRoleEntity athleteRole, CompetitionEntity competition) {
        for (SeedAthlete seedAthlete : seedAthletes()) {
            AppUserEntity athlete = createDefaultAthleteIfMissing(seedAthlete, athleteRole);
            registerAthleteIfMissing(athlete, competition);
        }
    }

    private AppUserEntity createDefaultAthleteIfMissing(SeedAthlete seedAthlete, UserRoleEntity athleteRole) {
        return appUserRepository.findByUsername(seedAthlete.username())
                .orElseGet(() -> {
                    AppUserEntity athlete = new AppUserEntity();
                    athlete.setUsername(seedAthlete.username());
                    athlete.setPassword(passwordEncoder.encode(DEFAULT_ATHLETE_PASSWORD));
                    athlete.setFullName(seedAthlete.fullName());
                    athlete.setCountry(seedAthlete.country());
                    athlete.setGender(Gender.MALE);
                    athlete.setDateOfBirth(seedAthlete.dateOfBirth());
                    athlete.setRole(athleteRole);
                    athlete.setEnabled(true);

                    return appUserRepository.save(athlete);
                });
    }

    private void registerAthleteIfMissing(AppUserEntity athlete, CompetitionEntity competition) {
        if (competitionRegistrationRepository.existsByUserUsername(athlete.getUsername())) {
            return;
        }

        CompetitionRegistrationEntity registration = new CompetitionRegistrationEntity();
        registration.setUser(athlete);
        registration.setCompetition(competition);

        competitionRegistrationRepository.save(registration);
    }

    private List<SeedAthlete> seedAthletes() {
        return List.of(
                new SeedAthlete("athlete1", "Ivan Petrov", "Bulgaria", LocalDate.of(2000, 1, 15)),
                new SeedAthlete("athlete2", "Georgi Ivanov", "Bulgaria", LocalDate.of(1999, 3, 8)),
                new SeedAthlete("athlete3", "Lukas Bauer", "Germany", LocalDate.of(1998, 7, 21)),
                new SeedAthlete("athlete4", "Marco Rossi", "Italy", LocalDate.of(2001, 11, 2)),
                new SeedAthlete("athlete5", "Erik Johansen", "Norway", LocalDate.of(1997, 4, 17)),
                new SeedAthlete("athlete6", "Anton Meyer", "Austria", LocalDate.of(2002, 6, 30)),
                new SeedAthlete("athlete7", "Pierre Martin", "France", LocalDate.of(1996, 9, 12)),
                new SeedAthlete("athlete8", "Jan Novak", "Czech Republic", LocalDate.of(2000, 12, 4)),
                new SeedAthlete("athlete9", "Mika Koskinen", "Finland", LocalDate.of(1999, 2, 26)),
                new SeedAthlete("athlete10", "Alex Turner", "United States", LocalDate.of(1998, 10, 9))
        );
    }

    private record SeedAthlete(String username, String fullName, String country, LocalDate dateOfBirth) {
    }
}
