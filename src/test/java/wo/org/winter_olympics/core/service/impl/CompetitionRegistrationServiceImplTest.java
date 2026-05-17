package wo.org.winter_olympics.core.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wo.org.winter_olympics.data.entity.AppUserEntity;
import wo.org.winter_olympics.data.entity.CompetitionEntity;
import wo.org.winter_olympics.data.entity.CompetitionRegistrationEntity;
import wo.org.winter_olympics.data.entity.enums.CompetitionStatus;
import wo.org.winter_olympics.data.repo.AppUserRepository;
import wo.org.winter_olympics.data.repo.CompetitionRegistrationRepository;
import wo.org.winter_olympics.data.repo.CompetitionRepository;
import wo.org.winter_olympics.exception.CompetitionJoinException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionRegistrationServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private CompetitionRegistrationRepository competitionRegistrationRepository;

    private CompetitionRegistrationServiceImpl competitionRegistrationService;

    @BeforeEach
    void setUp() {
        competitionRegistrationService = new CompetitionRegistrationServiceImpl(
                appUserRepository,
                competitionRepository,
                competitionRegistrationRepository
        );
    }

    @Test
    void joinCompetitionCreatesRegistration() {
        AppUserEntity user = createUser();
        CompetitionEntity competition = createCompetition(CompetitionStatus.OPEN);

        when(appUserRepository.findByUsername("athlete1")).thenReturn(Optional.of(user));
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
        when(competitionRegistrationRepository.findByUserUsername("athlete1")).thenReturn(Optional.empty());

        competitionRegistrationService.joinCompetition(1L, "athlete1");

        ArgumentCaptor<CompetitionRegistrationEntity> registrationCaptor =
                ArgumentCaptor.forClass(CompetitionRegistrationEntity.class);
        verify(competitionRegistrationRepository).save(registrationCaptor.capture());

        CompetitionRegistrationEntity savedRegistration = registrationCaptor.getValue();
        assertEquals(user, savedRegistration.getUser());
        assertEquals(competition, savedRegistration.getCompetition());
    }

    @Test
    void joinCompetitionThrowsWhenCompetitionStarted() {
        AppUserEntity user = createUser();
        CompetitionEntity competition = createCompetition(CompetitionStatus.IN_PROGRESS);

        when(appUserRepository.findByUsername("athlete1")).thenReturn(Optional.of(user));
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));

        CompetitionJoinException exception = assertThrows(
                CompetitionJoinException.class,
                () -> competitionRegistrationService.joinCompetition(1L, "athlete1")
        );

        assertEquals("You cannot join a competition after it has started.", exception.getMessage());
        verify(competitionRegistrationRepository, never()).save(any());
    }

    @Test
    void joinCompetitionThrowsWhenUserAlreadyJoinedAnotherCompetition() {
        AppUserEntity user = createUser();
        CompetitionEntity competition = createCompetition(CompetitionStatus.OPEN);
        CompetitionEntity otherCompetition = createCompetition(CompetitionStatus.OPEN);
        otherCompetition.setId(2L);

        CompetitionRegistrationEntity currentRegistration = new CompetitionRegistrationEntity();
        currentRegistration.setCompetition(otherCompetition);
        currentRegistration.setUser(user);

        when(appUserRepository.findByUsername("athlete1")).thenReturn(Optional.of(user));
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
        when(competitionRegistrationRepository.findByUserUsername("athlete1")).thenReturn(Optional.of(currentRegistration));

        CompetitionJoinException exception = assertThrows(
                CompetitionJoinException.class,
                () -> competitionRegistrationService.joinCompetition(1L, "athlete1")
        );

        assertEquals("You are already registered for another competition.", exception.getMessage());
        verify(competitionRegistrationRepository, never()).save(any());
    }

    @Test
    void leaveCompetitionDeletesRegistrationWhenCompetitionIsOpen() {
        AppUserEntity user = createUser();
        CompetitionEntity competition = createCompetition(CompetitionStatus.OPEN);
        CompetitionRegistrationEntity registration = new CompetitionRegistrationEntity();
        registration.setCompetition(competition);
        registration.setUser(user);

        when(competitionRegistrationRepository.findByUserUsername("athlete1")).thenReturn(Optional.of(registration));

        competitionRegistrationService.leaveCompetition(1L, "athlete1");

        verify(competitionRegistrationRepository).delete(registration);
    }

    @Test
    void leaveCompetitionThrowsWhenCompetitionStarted() {
        AppUserEntity user = createUser();
        CompetitionEntity competition = createCompetition(CompetitionStatus.IN_PROGRESS);
        CompetitionRegistrationEntity registration = new CompetitionRegistrationEntity();
        registration.setCompetition(competition);
        registration.setUser(user);

        when(competitionRegistrationRepository.findByUserUsername("athlete1")).thenReturn(Optional.of(registration));

        CompetitionJoinException exception = assertThrows(
                CompetitionJoinException.class,
                () -> competitionRegistrationService.leaveCompetition(1L, "athlete1")
        );

        assertEquals("You can only leave a competition before it starts.", exception.getMessage());
        verify(competitionRegistrationRepository, never()).delete(any());
    }

    private AppUserEntity createUser() {
        AppUserEntity user = new AppUserEntity();
        user.setUsername("athlete1");
        return user;
    }

    private CompetitionEntity createCompetition(CompetitionStatus status) {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setId(1L);
        competition.setName("Men Ski Slalom");
        competition.setStatus(status);
        competition.setRegistrationDeadline(LocalDate.now().plusDays(10));
        return competition;
    }
}
