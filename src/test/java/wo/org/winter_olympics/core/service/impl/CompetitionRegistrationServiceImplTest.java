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
import wo.org.winter_olympics.dto.CompetitionParticipantViewDto;
import wo.org.winter_olympics.exception.CompetitionJoinException;
import wo.org.winter_olympics.exception.CompetitionNotFoundException;

import java.time.LocalDate;
import java.util.List;
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
        CompetitionEntity competition = createCompetition(CompetitionStatus.STARTING_SOON);

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
        CompetitionEntity competition = createCompetition(CompetitionStatus.STARTING_SOON);
        CompetitionEntity otherCompetition = createCompetition(CompetitionStatus.STARTING_SOON);
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
        CompetitionEntity competition = createCompetition(CompetitionStatus.STARTING_SOON);
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

    @Test
    void getParticipantsForCompetitionReturnsParticipants() {
        AppUserEntity user = createUser();
        user.setFullName("Ivan Petrov");
        user.setCountry("Bulgaria");
        user.setGender(wo.org.winter_olympics.data.entity.enums.Gender.MALE);

        CompetitionEntity competition = createCompetition(CompetitionStatus.STARTING_SOON);
        CompetitionRegistrationEntity registration = new CompetitionRegistrationEntity();
        registration.setCompetition(competition);
        registration.setUser(user);

        when(competitionRepository.existsById(1L)).thenReturn(true);
        when(competitionRegistrationRepository.findAllByCompetitionId(1L)).thenReturn(List.of(registration));

        List<CompetitionParticipantViewDto> participants =
                competitionRegistrationService.getParticipantsForCompetition(1L);

        assertEquals(1, participants.size());
        assertEquals("athlete1", participants.getFirst().getUsername());
        assertEquals("Ivan Petrov", participants.getFirst().getFullName());
        assertEquals("Bulgaria", participants.getFirst().getCountry());
        assertEquals("Pending", participants.getFirst().getResultStatus());
    }

    @Test
    void getParticipantsForCompetitionThrowsWhenCompetitionDoesNotExist() {
        when(competitionRepository.existsById(99L)).thenReturn(false);

        CompetitionNotFoundException exception = assertThrows(
                CompetitionNotFoundException.class,
                () -> competitionRegistrationService.getParticipantsForCompetition(99L)
        );

        assertEquals("Competition was not found: 99", exception.getMessage());
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
