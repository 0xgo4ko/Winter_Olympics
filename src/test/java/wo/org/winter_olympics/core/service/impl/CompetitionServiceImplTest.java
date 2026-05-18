package wo.org.winter_olympics.core.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wo.org.winter_olympics.data.entity.CompetitionEntity;
import wo.org.winter_olympics.data.entity.enums.CompetitionStatus;
import wo.org.winter_olympics.data.entity.enums.CompetitionType;
import wo.org.winter_olympics.data.entity.enums.Gender;
import wo.org.winter_olympics.data.repo.AppUserRepository;
import wo.org.winter_olympics.data.repo.CompetitionRegistrationRepository;
import wo.org.winter_olympics.data.repo.CompetitionRepository;
import wo.org.winter_olympics.dto.CompetitionCreateDto;
import wo.org.winter_olympics.dto.CompetitionViewDto;
import wo.org.winter_olympics.exception.CompetitionNameAlreadyExistsException;
import wo.org.winter_olympics.exception.CompetitionNotFoundException;
import wo.org.winter_olympics.exception.CompetitionStartException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private CompetitionRegistrationRepository competitionRegistrationRepository;

    private CompetitionServiceImpl competitionService;

    @BeforeEach
    void setUp() {
        competitionService = new CompetitionServiceImpl(
                appUserRepository,
                competitionRepository,
                competitionRegistrationRepository
        );
    }

    @Test
    void createCompetitionCreatesSkiSlalomCompetition() {
        CompetitionCreateDto createDto = createSkiSlalomDto();
        when(competitionRepository.existsByName("Men Ski Slalom")).thenReturn(false);

        competitionService.createCompetition(createDto);

        ArgumentCaptor<CompetitionEntity> competitionCaptor = ArgumentCaptor.forClass(CompetitionEntity.class);
        verify(competitionRepository).save(competitionCaptor.capture());

        CompetitionEntity savedCompetition = competitionCaptor.getValue();
        assertEquals("Men Ski Slalom", savedCompetition.getName());
        assertEquals(CompetitionType.SKI_SLALOM, savedCompetition.getType());
        assertEquals(Gender.MALE, savedCompetition.getGender());
        assertEquals(18, savedCompetition.getMinimumAge());
        assertEquals(LocalDate.of(2026, 6, 1), savedCompetition.getRegistrationDeadline());
        assertEquals(CompetitionStatus.STARTING_SOON, savedCompetition.getStatus());
        assertEquals(30, savedCompetition.getSecondRunQualifierCount());
        assertNull(savedCompetition.getPenaltySecondsPerMiss());
    }

    @Test
    void createCompetitionCreatesBiathlonCompetition() {
        CompetitionCreateDto createDto = createBiathlonDto();
        when(competitionRepository.existsByName("Women Biathlon")).thenReturn(false);

        competitionService.createCompetition(createDto);

        ArgumentCaptor<CompetitionEntity> competitionCaptor = ArgumentCaptor.forClass(CompetitionEntity.class);
        verify(competitionRepository).save(competitionCaptor.capture());

        CompetitionEntity savedCompetition = competitionCaptor.getValue();
        assertEquals("Women Biathlon", savedCompetition.getName());
        assertEquals(CompetitionType.BIATHLON, savedCompetition.getType());
        assertEquals(Gender.FEMALE, savedCompetition.getGender());
        assertEquals(60, savedCompetition.getPenaltySecondsPerMiss());
        assertNull(savedCompetition.getSecondRunQualifierCount());
    }

    @Test
    void createCompetitionThrowsWhenNameAlreadyExists() {
        CompetitionCreateDto createDto = createSkiSlalomDto();
        when(competitionRepository.existsByName("Men Ski Slalom")).thenReturn(true);

        CompetitionNameAlreadyExistsException exception = assertThrows(
                CompetitionNameAlreadyExistsException.class,
                () -> competitionService.createCompetition(createDto)
        );

        assertEquals("Competition name is already taken: Men Ski Slalom", exception.getMessage());
        verify(competitionRepository, never()).save(any());
    }

    @Test
    void getAllCompetitionsReturnsViewDtos() {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setName("Men Ski Slalom");
        competition.setType(CompetitionType.SKI_SLALOM);
        competition.setGender(Gender.MALE);
        competition.setMinimumAge(18);
        competition.setRegistrationDeadline(LocalDate.of(2026, 6, 1));
        competition.setStatus(CompetitionStatus.STARTING_SOON);
        competition.setSecondRunQualifierCount(30);

        when(competitionRepository.findAll()).thenReturn(List.of(competition));

        List<CompetitionViewDto> competitions = competitionService.getAllCompetitions();

        assertEquals(1, competitions.size());
        assertEquals("Men Ski Slalom", competitions.getFirst().getName());
        assertEquals(CompetitionType.SKI_SLALOM, competitions.getFirst().getType());
        assertEquals(Gender.MALE, competitions.getFirst().getGender());
        assertEquals(18, competitions.getFirst().getMinimumAge());
        assertEquals(CompetitionStatus.STARTING_SOON, competitions.getFirst().getStatus());
        assertEquals(30, competitions.getFirst().getSecondRunQualifierCount());
    }

    @Test
    void getCompetitionForEditReturnsCreateDto() {
        CompetitionEntity competition = createSkiSlalomEntity();
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));

        CompetitionCreateDto editDto = competitionService.getCompetitionForEdit(1L);

        assertEquals("Men Ski Slalom", editDto.getName());
        assertEquals(CompetitionType.SKI_SLALOM, editDto.getType());
        assertEquals(Gender.MALE, editDto.getGender());
        assertEquals(18, editDto.getMinimumAge());
        assertEquals(LocalDate.of(2026, 6, 1), editDto.getRegistrationDeadline());
        assertEquals(30, editDto.getSecondRunQualifierCount());
    }

    @Test
    void updateCompetitionUpdatesEditableFields() {
        CompetitionEntity competition = createSkiSlalomEntity();
        CompetitionCreateDto updateDto = createBiathlonDto();

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
        when(competitionRepository.existsByNameAndIdNot("Women Biathlon", 1L)).thenReturn(false);

        competitionService.updateCompetition(1L, updateDto);

        ArgumentCaptor<CompetitionEntity> competitionCaptor = ArgumentCaptor.forClass(CompetitionEntity.class);
        verify(competitionRepository).save(competitionCaptor.capture());

        CompetitionEntity savedCompetition = competitionCaptor.getValue();
        assertEquals("Women Biathlon", savedCompetition.getName());
        assertEquals(CompetitionType.BIATHLON, savedCompetition.getType());
        assertEquals(Gender.FEMALE, savedCompetition.getGender());
        assertEquals(18, savedCompetition.getMinimumAge());
        assertEquals(LocalDate.of(2026, 6, 1), savedCompetition.getRegistrationDeadline());
        assertEquals(CompetitionStatus.STARTING_SOON, savedCompetition.getStatus());
        assertEquals(60, savedCompetition.getPenaltySecondsPerMiss());
        assertNull(savedCompetition.getSecondRunQualifierCount());
    }

    @Test
    void updateCompetitionThrowsWhenNameBelongsToAnotherCompetition() {
        CompetitionEntity competition = createSkiSlalomEntity();
        CompetitionCreateDto updateDto = createBiathlonDto();

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
        when(competitionRepository.existsByNameAndIdNot("Women Biathlon", 1L)).thenReturn(true);

        CompetitionNameAlreadyExistsException exception = assertThrows(
                CompetitionNameAlreadyExistsException.class,
                () -> competitionService.updateCompetition(1L, updateDto)
        );

        assertEquals("Competition name is already taken: Women Biathlon", exception.getMessage());
    }

    @Test
    void deleteCompetitionDeletesEntity() {
        CompetitionEntity competition = createSkiSlalomEntity();
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));

        competitionService.deleteCompetition(1L);

        verify(competitionRepository).delete(competition);
    }

    @Test
    void deleteCompetitionThrowsWhenCompetitionDoesNotExist() {
        when(competitionRepository.findById(99L)).thenReturn(Optional.empty());

        CompetitionNotFoundException exception = assertThrows(
                CompetitionNotFoundException.class,
                () -> competitionService.deleteCompetition(99L)
        );

        assertEquals("Competition was not found: 99", exception.getMessage());
    }

    @Test
    void startCompetitionMovesSkiSlalomToFirstRun() {
        CompetitionEntity competition = createSkiSlalomEntity();
        competition.setRegistrationDeadline(LocalDate.now());

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));

        competitionService.startCompetition(1L);

        ArgumentCaptor<CompetitionEntity> competitionCaptor = ArgumentCaptor.forClass(CompetitionEntity.class);
        verify(competitionRepository).save(competitionCaptor.capture());

        assertEquals(CompetitionStatus.FIRST_RUN, competitionCaptor.getValue().getStatus());
    }

    @Test
    void startCompetitionMovesBiathlonToInProgress() {
        CompetitionEntity competition = createSkiSlalomEntity();
        competition.setType(CompetitionType.BIATHLON);
        competition.setRegistrationDeadline(LocalDate.now());

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));

        competitionService.startCompetition(1L);

        ArgumentCaptor<CompetitionEntity> competitionCaptor = ArgumentCaptor.forClass(CompetitionEntity.class);
        verify(competitionRepository).save(competitionCaptor.capture());

        assertEquals(CompetitionStatus.IN_PROGRESS, competitionCaptor.getValue().getStatus());
    }

    @Test
    void startCompetitionThrowsWhenDeadlineIsInFuture() {
        CompetitionEntity competition = createSkiSlalomEntity();
        competition.setRegistrationDeadline(LocalDate.now().plusDays(1));

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));

        CompetitionStartException exception = assertThrows(
                CompetitionStartException.class,
                () -> competitionService.startCompetition(1L)
        );

        assertEquals("You can start the competition after the registration deadline is reached.", exception.getMessage());
    }

    private CompetitionCreateDto createSkiSlalomDto() {
        CompetitionCreateDto createDto = new CompetitionCreateDto();
        createDto.setName("Men Ski Slalom");
        createDto.setType(CompetitionType.SKI_SLALOM);
        createDto.setGender(Gender.MALE);
        createDto.setMinimumAge(18);
        createDto.setRegistrationDeadline(LocalDate.of(2026, 6, 1));
        createDto.setSecondRunQualifierCount(30);

        return createDto;
    }

    private CompetitionCreateDto createBiathlonDto() {
        CompetitionCreateDto createDto = new CompetitionCreateDto();
        createDto.setName("Women Biathlon");
        createDto.setType(CompetitionType.BIATHLON);
        createDto.setGender(Gender.FEMALE);
        createDto.setMinimumAge(18);
        createDto.setRegistrationDeadline(LocalDate.of(2026, 6, 1));
        createDto.setPenaltySecondsPerMiss(60);

        return createDto;
    }

    private CompetitionEntity createSkiSlalomEntity() {
        CompetitionEntity competition = new CompetitionEntity();
        competition.setName("Men Ski Slalom");
        competition.setType(CompetitionType.SKI_SLALOM);
        competition.setGender(Gender.MALE);
        competition.setMinimumAge(18);
        competition.setRegistrationDeadline(LocalDate.of(2026, 6, 1));
        competition.setStatus(CompetitionStatus.STARTING_SOON);
        competition.setSecondRunQualifierCount(30);

        return competition;
    }
}
