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
import wo.org.winter_olympics.data.entity.enums.CompetitionType;
import wo.org.winter_olympics.data.repo.AppUserRepository;
import wo.org.winter_olympics.data.repo.CompetitionRegistrationRepository;
import wo.org.winter_olympics.data.repo.CompetitionRepository;
import wo.org.winter_olympics.dto.CompetitionParticipantViewDto;
import wo.org.winter_olympics.dto.FirstRunResultInputDto;
import wo.org.winter_olympics.exception.CompetitionJoinException;
import wo.org.winter_olympics.exception.CompetitionNotFoundException;
import wo.org.winter_olympics.exception.CompetitionResultException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void startSecondRunSavesSubmittedResultsAndQualifiesFastestAthletes() {
        CompetitionEntity competition = createCompetition(CompetitionStatus.FIRST_RUN);
        competition.setSecondRunQualifierCount(2);

        CompetitionRegistrationEntity first = createRegistration(1L, competition, createUser());
        CompetitionRegistrationEntity second = createRegistration(2L, competition, createUser());
        CompetitionRegistrationEntity third = createRegistration(3L, competition, createUser());
        CompetitionRegistrationEntity dnf = createRegistration(4L, competition, createUser());

        List<CompetitionRegistrationEntity> registrations = List.of(first, second, third, dnf);
        List<FirstRunResultInputDto> submittedResults = List.of(
                createFirstRunResultInput(1L, new BigDecimal("59.200"), false),
                createFirstRunResultInput(2L, new BigDecimal("57.100"), false),
                createFirstRunResultInput(3L, new BigDecimal("58.400"), false),
                createFirstRunResultInput(4L, null, true)
        );

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
        when(competitionRegistrationRepository.findAllByCompetitionId(1L)).thenReturn(registrations);

        competitionRegistrationService.startSecondRun(1L, submittedResults);

        assertEquals(CompetitionStatus.SECOND_RUN, competition.getStatus());
        assertEquals(new BigDecimal("59.200"), first.getFirstRunTime());
        assertEquals(new BigDecimal("57.100"), second.getFirstRunTime());
        assertEquals(new BigDecimal("58.400"), third.getFirstRunTime());
        assertNull(dnf.getFirstRunTime());
        assertTrue(dnf.isDidNotFinish());
        assertFalse(first.isQualifiedForSecondRun());
        assertTrue(second.isQualifiedForSecondRun());
        assertTrue(third.isQualifiedForSecondRun());
        assertFalse(dnf.isQualifiedForSecondRun());
        verify(competitionRegistrationRepository).saveAll(registrations);
        verify(competitionRepository).save(competition);
    }

    @Test
    void startSecondRunThrowsWhenAnyAthleteHasMissingResult() {
        CompetitionEntity competition = createCompetition(CompetitionStatus.FIRST_RUN);
        CompetitionRegistrationEntity registration = createRegistration(1L, competition, createUser());
        List<FirstRunResultInputDto> submittedResults = List.of(
                createFirstRunResultInput(1L, null, false)
        );

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
        when(competitionRegistrationRepository.findAllByCompetitionId(1L)).thenReturn(List.of(registration));

        CompetitionResultException exception = assertThrows(
                CompetitionResultException.class,
                () -> competitionRegistrationService.startSecondRun(1L, submittedResults)
        );

        assertEquals("Every athlete needs a first-run time or DNF before second run starts.", exception.getMessage());
        verify(competitionRegistrationRepository, never()).saveAll(any());
        verify(competitionRepository, never()).save(any());
    }

    @Test
    void startSecondRunThrowsWhenSubmittedTimeIsNotPositive() {
        CompetitionEntity competition = createCompetition(CompetitionStatus.FIRST_RUN);
        CompetitionRegistrationEntity registration = createRegistration(1L, competition, createUser());
        List<FirstRunResultInputDto> submittedResults = List.of(
                createFirstRunResultInput(1L, BigDecimal.ZERO, false)
        );

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
        when(competitionRegistrationRepository.findAllByCompetitionId(1L)).thenReturn(List.of(registration));

        CompetitionResultException exception = assertThrows(
                CompetitionResultException.class,
                () -> competitionRegistrationService.startSecondRun(1L, submittedResults)
        );

        assertEquals("First-run time must be greater than zero.", exception.getMessage());
        verify(competitionRegistrationRepository, never()).saveAll(any());
        verify(competitionRepository, never()).save(any());
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
        competition.setType(CompetitionType.SKI_SLALOM);
        competition.setStatus(status);
        competition.setRegistrationDeadline(LocalDate.now().plusDays(10));
        competition.setSecondRunQualifierCount(5);
        return competition;
    }

    private CompetitionRegistrationEntity createRegistration(
            Long id,
            CompetitionEntity competition,
            AppUserEntity user
    ) {
        CompetitionRegistrationEntity registration = new CompetitionRegistrationEntity();
        registration.setId(id);
        registration.setCompetition(competition);
        registration.setUser(user);
        return registration;
    }

    private FirstRunResultInputDto createFirstRunResultInput(
            Long registrationId,
            BigDecimal firstRunTime,
            boolean didNotFinish
    ) {
        FirstRunResultInputDto input = new FirstRunResultInputDto();
        input.setRegistrationId(registrationId);
        input.setFirstRunTime(firstRunTime);
        input.setDidNotFinish(didNotFinish);
        return input;
    }
}
