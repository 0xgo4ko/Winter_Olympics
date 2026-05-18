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
import wo.org.winter_olympics.dto.SecondRunResultInputDto;
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
        CompetitionEntity competition = getCompetition(competitionId);

        List<CompetitionRegistrationEntity> registrations =
                competitionRegistrationRepository.findAllByCompetitionId(competitionId);

        List<CompetitionParticipantViewDto> participants = filterAndSortRegistrations(competition, registrations)
                .stream()
                .map(this::mapToParticipantViewDto)
                .toList();

        if (competition.getStatus() == CompetitionStatus.ENDED) {
            assignFinalRanks(participants);
        }

        return participants;
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
        registrations.forEach(registration -> {
            registration.setSecondRunDidNotFinish(false);
            registration.setSecondRunTime(null);
        });

        competition.setStatus(CompetitionStatus.SECOND_RUN);

        competitionRegistrationRepository.saveAll(registrations);
        competitionRepository.save(competition);
    }

    @Override
    @Transactional
    public void endCompetition(Long competitionId, List<SecondRunResultInputDto> secondRunResults) {
        CompetitionEntity competition = getCompetition(competitionId);

        if (competition.getType() != CompetitionType.SKI_SLALOM) {
            throw new CompetitionResultException(competitionId, "Ending this flow is available only for ski slalom competitions.");
        }

        if (competition.getStatus() != CompetitionStatus.SECOND_RUN) {
            throw new CompetitionResultException(competitionId, "Competition can be ended only during the second run.");
        }

        List<CompetitionRegistrationEntity> registrations =
                competitionRegistrationRepository.findAllByCompetitionId(competitionId);
        List<CompetitionRegistrationEntity> qualifiedRegistrations = registrations.stream()
                .filter(CompetitionRegistrationEntity::isQualifiedForSecondRun)
                .toList();

        applySecondRunResults(competitionId, qualifiedRegistrations, secondRunResults);

        competition.setStatus(CompetitionStatus.ENDED);

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

    private void applySecondRunResults(
            Long competitionId,
            List<CompetitionRegistrationEntity> qualifiedRegistrations,
            List<SecondRunResultInputDto> secondRunResults
    ) {
        List<SecondRunResultInputDto> submittedResults =
                secondRunResults == null ? Collections.emptyList() : secondRunResults;

        Map<Long, CompetitionRegistrationEntity> registrationsById = qualifiedRegistrations.stream()
                .collect(Collectors.toMap(CompetitionRegistrationEntity::getId, Function.identity()));

        Map<Long, SecondRunResultInputDto> resultsByRegistrationId = submittedResults.stream()
                .filter(result -> result.getRegistrationId() != null)
                .collect(Collectors.toMap(
                        SecondRunResultInputDto::getRegistrationId,
                        Function.identity(),
                        (first, second) -> first
                ));

        for (CompetitionRegistrationEntity registration : qualifiedRegistrations) {
            SecondRunResultInputDto result = resultsByRegistrationId.get(registration.getId());
            if (result == null) {
                throw new CompetitionResultException(competitionId, "Every qualified athlete needs a second-run time or DNF before the competition ends.");
            }

            validateSecondRunResult(competitionId, result);
        }

        for (SecondRunResultInputDto result : submittedResults) {
            CompetitionRegistrationEntity registration = registrationsById.get(result.getRegistrationId());
            if (registration == null) {
                throw new CompetitionResultException(competitionId, "This athlete is not qualified for the second run.");
            }

            registration.setSecondRunDidNotFinish(result.isSecondRunDidNotFinish());
            registration.setSecondRunTime(result.isSecondRunDidNotFinish() ? null : result.getSecondRunTime());
        }
    }

    private void validateSecondRunResult(Long competitionId, SecondRunResultInputDto result) {
        if (result.isSecondRunDidNotFinish()) {
            return;
        }

        BigDecimal secondRunTime = result.getSecondRunTime();
        if (secondRunTime == null) {
            throw new CompetitionResultException(competitionId, "Every qualified athlete needs a second-run time or DNF before the competition ends.");
        }

        if (secondRunTime.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CompetitionResultException(competitionId, "Second-run time must be greater than zero.");
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
        participantViewDto.setSecondRunTime(registration.getSecondRunTime());
        participantViewDto.setSecondRunDidNotFinish(registration.isSecondRunDidNotFinish());
        participantViewDto.setTotalTime(calculateTotalTime(registration));
        participantViewDto.setResultStatus(resolveResultStatus(registration));

        return participantViewDto;
    }

    private List<CompetitionRegistrationEntity> filterAndSortRegistrations(
            CompetitionEntity competition,
            List<CompetitionRegistrationEntity> registrations
    ) {
        if (competition.getStatus() == CompetitionStatus.SECOND_RUN) {
            return registrations.stream()
                    .filter(CompetitionRegistrationEntity::isQualifiedForSecondRun)
                    .sorted(Comparator.comparing(CompetitionRegistrationEntity::getFirstRunTime))
                    .toList();
        }

        if (competition.getStatus() == CompetitionStatus.ENDED) {
            return registrations.stream()
                    .filter(CompetitionRegistrationEntity::isQualifiedForSecondRun)
                    .sorted(finalStandingsComparator())
                    .toList();
        }

        return registrations;
    }

    private Comparator<CompetitionRegistrationEntity> finalStandingsComparator() {
        return Comparator
                .comparing(CompetitionRegistrationEntity::isSecondRunDidNotFinish)
                .thenComparing(this::calculateTotalTime, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private BigDecimal calculateTotalTime(CompetitionRegistrationEntity registration) {
        if (registration.isDidNotFinish() || registration.isSecondRunDidNotFinish()) {
            return null;
        }

        if (registration.getFirstRunTime() == null || registration.getSecondRunTime() == null) {
            return null;
        }

        return registration.getFirstRunTime().add(registration.getSecondRunTime());
    }

    private void assignFinalRanks(List<CompetitionParticipantViewDto> participants) {
        int rank = 1;
        for (CompetitionParticipantViewDto participant : participants) {
            if (participant.getTotalTime() == null) {
                continue;
            }

            participant.setRank(rank);
            participant.setMedal(resolveMedal(rank));
            participant.setPodiumCssClass(resolvePodiumCssClass(rank));
            rank++;
        }
    }

    private String resolveMedal(int rank) {
        if (rank == 1) {
            return "Gold";
        }

        if (rank == 2) {
            return "Silver";
        }

        if (rank == 3) {
            return "Bronze";
        }

        return null;
    }

    private String resolvePodiumCssClass(int rank) {
        if (rank == 1) {
            return "standings-row standings-row--gold";
        }

        if (rank == 2) {
            return "standings-row standings-row--silver";
        }

        if (rank == 3) {
            return "standings-row standings-row--bronze";
        }

        return "";
    }

    private String resolveResultStatus(CompetitionRegistrationEntity registration) {
        if (registration.isDidNotFinish()) {
            return "DNF";
        }

        if (registration.getCompetition().getStatus() == CompetitionStatus.ENDED) {
            if (registration.isSecondRunDidNotFinish()) {
                return "DNF";
            }

            return "Finished";
        }

        if (registration.getCompetition().getStatus() == CompetitionStatus.SECOND_RUN) {
            if (registration.isSecondRunDidNotFinish()) {
                return "DNF";
            }

            if (registration.getSecondRunTime() != null) {
                return "Second run saved";
            }

            return "Qualified";
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
