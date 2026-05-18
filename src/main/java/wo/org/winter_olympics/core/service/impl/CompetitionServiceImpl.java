package wo.org.winter_olympics.core.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wo.org.winter_olympics.core.service.CompetitionService;
import wo.org.winter_olympics.data.entity.CompetitionEntity;
import wo.org.winter_olympics.data.entity.enums.CompetitionStatus;
import wo.org.winter_olympics.data.entity.enums.CompetitionType;
import wo.org.winter_olympics.data.repo.CompetitionRepository;
import wo.org.winter_olympics.dto.CompetitionCreateDto;
import wo.org.winter_olympics.dto.CompetitionViewDto;
import wo.org.winter_olympics.exception.CompetitionNameAlreadyExistsException;
import wo.org.winter_olympics.exception.CompetitionNotFoundException;
import wo.org.winter_olympics.exception.CompetitionStartException;

import java.time.LocalDate;
import java.util.List;

@Service
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionRepository competitionRepository;

    public CompetitionServiceImpl(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompetitionViewDto> getAllCompetitions() {
        return competitionRepository.findAll()
                .stream()
                .map(this::mapToViewDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompetitionCreateDto getCompetitionForEdit(Long id) {
        CompetitionEntity competition = getCompetitionEntityById(id);

        return mapToCreateDto(competition);
    }

    @Override
    @Transactional(readOnly = true)
    public CompetitionViewDto getCompetitionById(Long id) {
        return mapToViewDto(getCompetitionEntityById(id));
    }

    @Override
    @Transactional
    public void createCompetition(CompetitionCreateDto competitionCreateDto) {
        if (competitionRepository.existsByName(competitionCreateDto.getName())) {
            throw new CompetitionNameAlreadyExistsException(competitionCreateDto.getName());
        }

        CompetitionEntity competition = new CompetitionEntity();
        competition.setName(competitionCreateDto.getName());
        competition.setType(competitionCreateDto.getType());
        competition.setGender(competitionCreateDto.getGender());
        competition.setMinimumAge(competitionCreateDto.getMinimumAge());
        competition.setRegistrationDeadline(competitionCreateDto.getRegistrationDeadline());
        competition.setStatus(CompetitionStatus.STARTING_SOON);

        if (competitionCreateDto.getType() == CompetitionType.SKI_SLALOM) {
            competition.setSecondRunQualifierCount(competitionCreateDto.getSecondRunQualifierCount());
        }

        if (competitionCreateDto.getType() == CompetitionType.BIATHLON) {
            competition.setPenaltySecondsPerMiss(competitionCreateDto.getPenaltySecondsPerMiss());
        }

        competitionRepository.save(competition);
    }

    @Override
    @Transactional
    public void updateCompetition(Long id, CompetitionCreateDto competitionCreateDto) {
        CompetitionEntity competition = getCompetitionEntityById(id);

        if (competitionRepository.existsByNameAndIdNot(competitionCreateDto.getName(), id)) {
            throw new CompetitionNameAlreadyExistsException(competitionCreateDto.getName());
        }

        competition.setName(competitionCreateDto.getName());
        competition.setType(competitionCreateDto.getType());
        competition.setGender(competitionCreateDto.getGender());
        competition.setMinimumAge(competitionCreateDto.getMinimumAge());
        competition.setRegistrationDeadline(competitionCreateDto.getRegistrationDeadline());
        competition.setSecondRunQualifierCount(null);
        competition.setPenaltySecondsPerMiss(null);

        if (competitionCreateDto.getType() == CompetitionType.SKI_SLALOM) {
            competition.setSecondRunQualifierCount(competitionCreateDto.getSecondRunQualifierCount());
        }

        if (competitionCreateDto.getType() == CompetitionType.BIATHLON) {
            competition.setPenaltySecondsPerMiss(competitionCreateDto.getPenaltySecondsPerMiss());
        }

        competitionRepository.save(competition);
    }

    @Override
    @Transactional
    public void deleteCompetition(Long id) {
        CompetitionEntity competition = getCompetitionEntityById(id);
        competitionRepository.delete(competition);
    }

    @Override
    @Transactional
    public void startCompetition(Long id) {
        CompetitionEntity competition = getCompetitionEntityById(id);

        if (competition.getStatus() != CompetitionStatus.STARTING_SOON) {
            throw new CompetitionStartException(id, "This competition has already started or ended.");
        }

        if (competition.getRegistrationDeadline().isAfter(LocalDate.now())) {
            throw new CompetitionStartException(id, "You can start the competition after the registration deadline is reached.");
        }

        if (competition.getType() == CompetitionType.SKI_SLALOM) {
            competition.setStatus(CompetitionStatus.FIRST_RUN);
        } else if (competition.getType() == CompetitionType.BIATHLON) {
            competition.setStatus(CompetitionStatus.IN_PROGRESS);
        }

        competitionRepository.save(competition);
    }

    private CompetitionEntity getCompetitionEntityById(Long id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));
    }

    private CompetitionViewDto mapToViewDto(CompetitionEntity competition) {
        CompetitionViewDto viewDto = new CompetitionViewDto();
        viewDto.setId(competition.getId());
        viewDto.setName(competition.getName());
        viewDto.setType(competition.getType());
        viewDto.setGender(competition.getGender());
        viewDto.setMinimumAge(competition.getMinimumAge());
        viewDto.setRegistrationDeadline(competition.getRegistrationDeadline());
        viewDto.setStatus(competition.getStatus());
        viewDto.setSecondRunQualifierCount(competition.getSecondRunQualifierCount());
        viewDto.setPenaltySecondsPerMiss(competition.getPenaltySecondsPerMiss());
        viewDto.setStartingSoon(competition.getStatus() == CompetitionStatus.STARTING_SOON);
        viewDto.setFirstRun(competition.getStatus() == CompetitionStatus.FIRST_RUN);
        viewDto.setSecondRun(competition.getStatus() == CompetitionStatus.SECOND_RUN);
        viewDto.setEnded(competition.getStatus() == CompetitionStatus.ENDED);

        return viewDto;
    }

    private CompetitionCreateDto mapToCreateDto(CompetitionEntity competition) {
        CompetitionCreateDto createDto = new CompetitionCreateDto();
        createDto.setName(competition.getName());
        createDto.setType(competition.getType());
        createDto.setGender(competition.getGender());
        createDto.setMinimumAge(competition.getMinimumAge());
        createDto.setRegistrationDeadline(competition.getRegistrationDeadline());
        createDto.setSecondRunQualifierCount(competition.getSecondRunQualifierCount());
        createDto.setPenaltySecondsPerMiss(competition.getPenaltySecondsPerMiss());

        return createDto;
    }
}
