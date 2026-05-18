package wo.org.winter_olympics.core.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wo.org.winter_olympics.core.service.CompetitionRegistrationService;
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
import wo.org.winter_olympics.exception.UserNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CompetitionRegistrationServiceImpl implements CompetitionRegistrationService {

    private final AppUserRepository appUserRepository;
    private final CompetitionRepository competitionRepository;
    private final CompetitionRegistrationRepository competitionRegistrationRepository;

    public CompetitionRegistrationServiceImpl(
            AppUserRepository appUserRepository,
            CompetitionRepository competitionRepository,
            CompetitionRegistrationRepository competitionRegistrationRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.competitionRepository = competitionRepository;
        this.competitionRegistrationRepository = competitionRegistrationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getJoinedCompetitionId(String username) {
        return competitionRegistrationRepository.findByUserUsername(username)
                .map(registration -> registration.getCompetition().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompetitionParticipantViewDto> getParticipantsForCompetition(Long competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new CompetitionNotFoundException(competitionId);
        }

        return competitionRegistrationRepository.findAllByCompetitionId(competitionId)
                .stream()
                .map(this::mapToParticipantViewDto)
                .toList();
    }

    @Override
    @Transactional
    public void joinCompetition(Long competitionId, String username) {
        AppUserEntity user = getUser(username);
        CompetitionEntity competition = getCompetition(competitionId);

        validateCompetitionCanBeJoined(competition);

        Optional<CompetitionRegistrationEntity> currentRegistration =
                competitionRegistrationRepository.findByUserUsername(username);

        if (currentRegistration.isPresent()) {
            Long currentCompetitionId = currentRegistration.get().getCompetition().getId();
            if (competitionId.equals(currentCompetitionId)) {
                return;
            }

            throw new CompetitionJoinException("You are already registered for another competition.");
        }

        CompetitionRegistrationEntity registration = new CompetitionRegistrationEntity();
        registration.setCompetition(competition);
        registration.setUser(user);

        competitionRegistrationRepository.save(registration);
    }

    @Override
    @Transactional
    public void leaveCompetition(Long competitionId, String username) {
        CompetitionRegistrationEntity registration = competitionRegistrationRepository.findByUserUsername(username)
                .orElseThrow(() -> new CompetitionJoinException("You are not registered for this competition."));

        CompetitionEntity competition = registration.getCompetition();
        if (!competitionId.equals(competition.getId())) {
            throw new CompetitionJoinException("You are not registered for this competition.");
        }

        if (competition.getStatus() != CompetitionStatus.STARTING_SOON) {
            throw new CompetitionJoinException("You can only leave a competition before it starts.");
        }

        competitionRegistrationRepository.delete(registration);
    }

    @Override
    @Transactional
    public void startSecondRun(Long competitionId, List<FirstRunResultInputDto> firstRunResults) {
        CompetitionEntity competition = getCompetition(competitionId);

        if (competition.getType() != CompetitionType.SKI_SLALOM) {
            throw new CompetitionResultException(competitionId, "Second run is available only for ski slalom competitions.");
        }

        if (competition.getStatus() != CompetitionStatus.FIRST_RUN) {
            throw new CompetitionResultException(competitionId, "Second run can be started only after the first run starts.");
        }

        List<CompetitionRegistrationEntity> registrations =
                competitionRegistrationRepository.findAllByCompetitionId(competitionId);

        Integer qualifierCount = competition.getSecondRunQualifierCount();
        if (qualifierCount == null) {
            throw new CompetitionResultException(competitionId, "Set a second-run qualifier count before starting the second run.");
        }

        applyFirstRunResults(competitionId, registrations, firstRunResults);

        registrations.forEach(registration -> registration.setQualifiedForSecondRun(false));
        registrations.stream()
                .filter(registration -> !registration.isDidNotFinish())
                .sorted(Comparator.comparing(CompetitionRegistrationEntity::getFirstRunTime))
                .limit(qualifierCount)
                .forEach(registration -> registration.setQualifiedForSecondRun(true));

        competition.setStatus(CompetitionStatus.SECOND_RUN);

        competitionRegistrationRepository.saveAll(registrations);
        competitionRepository.save(competition);
    }

    private AppUserEntity getUser(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private CompetitionEntity getCompetition(Long competitionId) {
        return competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException(competitionId));
    }

    private void applyFirstRunResults(
            Long competitionId,
            List<CompetitionRegistrationEntity> registrations,
            List<FirstRunResultInputDto> firstRunResults
    ) {
        List<FirstRunResultInputDto> submittedResults =
                firstRunResults == null ? Collections.emptyList() : firstRunResults;

        Map<Long, CompetitionRegistrationEntity> registrationsById = registrations.stream()
                .collect(Collectors.toMap(CompetitionRegistrationEntity::getId, Function.identity()));

        Map<Long, FirstRunResultInputDto> resultsByRegistrationId = submittedResults.stream()
                .filter(result -> result.getRegistrationId() != null)
                .collect(Collectors.toMap(
                        FirstRunResultInputDto::getRegistrationId,
                        Function.identity(),
                        (first, second) -> first
                ));

        for (CompetitionRegistrationEntity registration : registrations) {
            FirstRunResultInputDto result = resultsByRegistrationId.get(registration.getId());
            if (result == null) {
                throw new CompetitionResultException(competitionId, "Every athlete needs a first-run time or DNF before second run starts.");
            }

            validateFirstRunResult(competitionId, result);
        }

        for (FirstRunResultInputDto result : submittedResults) {
            CompetitionRegistrationEntity registration = registrationsById.get(result.getRegistrationId());
            if (registration == null) {
                throw new CompetitionResultException(competitionId, "This athlete is not registered for this competition.");
            }

            registration.setDidNotFinish(result.isDidNotFinish());
            registration.setFirstRunTime(result.isDidNotFinish() ? null : result.getFirstRunTime());
            registration.setQualifiedForSecondRun(false);
        }
    }

    private void validateFirstRunResult(Long competitionId, FirstRunResultInputDto result) {
        if (result.isDidNotFinish()) {
            return;
        }

        BigDecimal firstRunTime = result.getFirstRunTime();
        if (firstRunTime == null) {
            throw new CompetitionResultException(competitionId, "Every athlete needs a first-run time or DNF before second run starts.");
        }

        if (firstRunTime.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CompetitionResultException(competitionId, "First-run time must be greater than zero.");
        }
    }

    private void validateCompetitionCanBeJoined(CompetitionEntity competition) {
        if (competition.getStatus() != CompetitionStatus.STARTING_SOON) {
            throw new CompetitionJoinException("You cannot join a competition after it has started.");
        }

        if (competition.getRegistrationDeadline().isBefore(LocalDate.now())) {
            throw new CompetitionJoinException("Registration for this competition is closed.");
        }
    }

    private CompetitionParticipantViewDto mapToParticipantViewDto(CompetitionRegistrationEntity registration) {
        AppUserEntity user = registration.getUser();

        CompetitionParticipantViewDto participantViewDto = new CompetitionParticipantViewDto();
        participantViewDto.setRegistrationId(registration.getId());
        participantViewDto.setUsername(user.getUsername());
        participantViewDto.setFullName(user.getFullName());
        participantViewDto.setCountry(user.getCountry());
        participantViewDto.setGender(user.getGender());
        participantViewDto.setFirstRunTime(registration.getFirstRunTime());
        participantViewDto.setDidNotFinish(registration.isDidNotFinish());
        participantViewDto.setQualifiedForSecondRun(registration.isQualifiedForSecondRun());
        participantViewDto.setResultStatus(resolveResultStatus(registration));

        return participantViewDto;
    }

    private String resolveResultStatus(CompetitionRegistrationEntity registration) {
        if (registration.isDidNotFinish()) {
            return "DNF";
        }

        if (registration.isQualifiedForSecondRun()) {
            return "Qualified";
        }

        if (registration.getCompetition().getStatus() == CompetitionStatus.SECOND_RUN
                && registration.getFirstRunTime() != null) {
            return "Not qualified";
        }

        if (registration.getFirstRunTime() != null) {
            return "First run saved";
        }

        return "Pending";
    }
}
