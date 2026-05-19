package wo.org.winter_olympics.core.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wo.org.winter_olympics.core.service.CompetitionService;
import wo.org.winter_olympics.data.entity.AppUserEntity;
import wo.org.winter_olympics.data.entity.CompetitionEntity;
import wo.org.winter_olympics.data.entity.CompetitionRegistrationEntity;
import wo.org.winter_olympics.data.entity.enums.CompetitionStatus;
import wo.org.winter_olympics.data.entity.enums.CompetitionType;
import wo.org.winter_olympics.data.repo.AppUserRepository;
import wo.org.winter_olympics.data.repo.CompetitionRegistrationRepository;
import wo.org.winter_olympics.data.repo.CompetitionRepository;
import wo.org.winter_olympics.dto.BiathlonResultInputDto;
import wo.org.winter_olympics.dto.CompetitionCreateDto;
import wo.org.winter_olympics.dto.CompetitionParticipantViewDto;
import wo.org.winter_olympics.dto.CompetitionViewDto;
import wo.org.winter_olympics.dto.CountryMedalCountDto;
import wo.org.winter_olympics.dto.FirstRunResultInputDto;
import wo.org.winter_olympics.dto.SecondRunResultInputDto;
import wo.org.winter_olympics.exception.CompetitionJoinException;
import wo.org.winter_olympics.exception.CompetitionNameAlreadyExistsException;
import wo.org.winter_olympics.exception.CompetitionNotFoundException;
import wo.org.winter_olympics.exception.CompetitionResultException;
import wo.org.winter_olympics.exception.CompetitionStartException;
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
public class CompetitionServiceImpl implements CompetitionService {

    private final AppUserRepository appUserRepository;
    private final CompetitionRepository competitionRepository;
    private final CompetitionRegistrationRepository competitionRegistrationRepository;

    public CompetitionServiceImpl(
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
            competition.setNumberOfLaps(competitionCreateDto.getNumberOfLaps());
            competition.setNumberOfTargets(competitionCreateDto.getNumberOfTargets());
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
        competition.setNumberOfLaps(null);
        competition.setNumberOfTargets(null);

        if (competitionCreateDto.getType() == CompetitionType.SKI_SLALOM) {
            competition.setSecondRunQualifierCount(competitionCreateDto.getSecondRunQualifierCount());
        }

        if (competitionCreateDto.getType() == CompetitionType.BIATHLON) {
            competition.setPenaltySecondsPerMiss(competitionCreateDto.getPenaltySecondsPerMiss());
            competition.setNumberOfLaps(competitionCreateDto.getNumberOfLaps());
            competition.setNumberOfTargets(competitionCreateDto.getNumberOfTargets());
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

        if (!competitionRegistrationRepository.existsByCompetitionId(id)) {
            throw new CompetitionStartException(id, "At least one athlete must join before the competition can start.");
        }

        if (competition.getType() == CompetitionType.SKI_SLALOM) {
            competition.setStatus(CompetitionStatus.FIRST_RUN);
        } else if (competition.getType() == CompetitionType.BIATHLON) {
            competition.setStatus(CompetitionStatus.IN_PROGRESS);
        }

        competitionRepository.save(competition);
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
        CompetitionEntity competition = getCompetitionEntityById(competitionId);

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
        CompetitionEntity competition = getCompetitionEntityById(competitionId);

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
        CompetitionEntity competition = getCompetitionEntityById(competitionId);

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
        CompetitionEntity competition = getCompetitionEntityById(competitionId);

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

    @Override
    @Transactional
    public void endBiathlonCompetition(Long competitionId, List<BiathlonResultInputDto> biathlonResults) {
        CompetitionEntity competition = getCompetitionEntityById(competitionId);

        if (competition.getType() != CompetitionType.BIATHLON) {
            throw new CompetitionResultException(competitionId, "Ending this flow is available only for biathlon competitions.");
        }

        if (competition.getStatus() != CompetitionStatus.IN_PROGRESS) {
            throw new CompetitionResultException(competitionId, "Biathlon can be ended only while it is in progress.");
        }

        List<CompetitionRegistrationEntity> registrations =
                competitionRegistrationRepository.findAllByCompetitionId(competitionId);

        applyBiathlonResults(competition, registrations, biathlonResults);

        competition.setStatus(CompetitionStatus.ENDED);

        competitionRegistrationRepository.saveAll(registrations);
        competitionRepository.save(competition);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryMedalCountDto> getCountryMedalCounts() {
        List<CompetitionRegistrationEntity> endedRegistrations =
                competitionRegistrationRepository.findAllByCompetitionStatus(CompetitionStatus.ENDED);

        Map<String, CountryMedalCountDto> countriesByName = new java.util.HashMap<>();

        endedRegistrations.forEach(registration -> countriesByName
                .computeIfAbsent(registration.getUser().getCountry(), CountryMedalCountDto::new)
                .incrementContestantsCount());

        endedRegistrations.stream()
                .collect(Collectors.groupingBy(registration -> registration.getCompetition().getId()))
                .values()
                .forEach(registrations -> awardMedalsForCompetition(registrations, countriesByName));

        return countriesByName.values()
                .stream()
                .sorted(Comparator
                        .comparing(CountryMedalCountDto::getMedalsCount).reversed()
                        .thenComparing(Comparator.comparingInt(CountryMedalCountDto::getContestantsCount).reversed())
                        .thenComparing(CountryMedalCountDto::getCountry))
                .toList();
    }

    private AppUserEntity getUser(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private CompetitionEntity getCompetitionEntityById(Long id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));
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

    private void applyBiathlonResults(
            CompetitionEntity competition,
            List<CompetitionRegistrationEntity> registrations,
            List<BiathlonResultInputDto> biathlonResults
    ) {
        Long competitionId = competition.getId();
        List<BiathlonResultInputDto> submittedResults =
                biathlonResults == null ? Collections.emptyList() : biathlonResults;

        Map<Long, CompetitionRegistrationEntity> registrationsById = registrations.stream()
                .collect(Collectors.toMap(CompetitionRegistrationEntity::getId, Function.identity()));

        Map<Long, BiathlonResultInputDto> resultsByRegistrationId = submittedResults.stream()
                .filter(result -> result.getRegistrationId() != null)
                .collect(Collectors.toMap(
                        BiathlonResultInputDto::getRegistrationId,
                        Function.identity(),
                        (first, second) -> first
                ));

        for (CompetitionRegistrationEntity registration : registrations) {
            BiathlonResultInputDto result = resultsByRegistrationId.get(registration.getId());
            if (result == null) {
                throw new CompetitionResultException(competitionId, "Every athlete needs a time or DNF before the competition ends.");
            }

            validateBiathlonResult(competition, result);
        }

        for (BiathlonResultInputDto result : submittedResults) {
            CompetitionRegistrationEntity registration = registrationsById.get(result.getRegistrationId());
            if (registration == null) {
                throw new CompetitionResultException(competitionId, "This athlete is not registered for this competition.");
            }

            registration.setDidNotFinish(result.isDidNotFinish());
            registration.setBiathlonTime(result.isDidNotFinish() ? null : result.getBiathlonTime());
            registration.setMissedTargets(result.isDidNotFinish() ? null : result.getMissedTargets());
        }
    }

    private void validateBiathlonResult(CompetitionEntity competition, BiathlonResultInputDto result) {
        if (result.isDidNotFinish()) {
            return;
        }

        if (result.getBiathlonTime() == null) {
            throw new CompetitionResultException(competition.getId(), "Every athlete needs a time or DNF before the competition ends.");
        }

        if (result.getBiathlonTime().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CompetitionResultException(competition.getId(), "Biathlon time must be greater than zero.");
        }

        Integer missedTargets = result.getMissedTargets();
        if (missedTargets == null) {
            throw new CompetitionResultException(competition.getId(), "Missed targets are required unless athlete is DNF.");
        }

        if (missedTargets < 0 || missedTargets > competition.getNumberOfTargets()) {
            throw new CompetitionResultException(
                    competition.getId(),
                    "Missed targets must be between 0 and " + competition.getNumberOfTargets() + "."
            );
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

    private void awardMedalsForCompetition(
            List<CompetitionRegistrationEntity> registrations,
            Map<String, CountryMedalCountDto> countriesByName
    ) {
        CompetitionEntity competition = registrations.getFirst().getCompetition();
        List<CompetitionRegistrationEntity> medalCandidates = filterAndSortRegistrations(competition, registrations)
                .stream()
                .filter(registration -> calculateTotalTime(registration) != null)
                .limit(3)
                .toList();

        medalCandidates.forEach(registration -> countriesByName
                .computeIfAbsent(registration.getUser().getCountry(), CountryMedalCountDto::new)
                .incrementMedalsCount());
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
        viewDto.setNumberOfLaps(competition.getNumberOfLaps());
        viewDto.setNumberOfTargets(competition.getNumberOfTargets());
        viewDto.setStartingSoon(competition.getStatus() == CompetitionStatus.STARTING_SOON);
        viewDto.setFirstRun(competition.getStatus() == CompetitionStatus.FIRST_RUN);
        viewDto.setSecondRun(competition.getStatus() == CompetitionStatus.SECOND_RUN);
        viewDto.setInProgress(competition.getStatus() == CompetitionStatus.IN_PROGRESS);
        viewDto.setEnded(competition.getStatus() == CompetitionStatus.ENDED);
        viewDto.setSkiSlalom(competition.getType() == CompetitionType.SKI_SLALOM);
        viewDto.setBiathlon(competition.getType() == CompetitionType.BIATHLON);

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
        createDto.setNumberOfLaps(competition.getNumberOfLaps());
        createDto.setNumberOfTargets(competition.getNumberOfTargets());

        return createDto;
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
        participantViewDto.setBiathlonTime(registration.getBiathlonTime());
        participantViewDto.setMissedTargets(registration.getMissedTargets());
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
            if (competition.getType() == CompetitionType.SKI_SLALOM) {
                return registrations.stream()
                        .filter(CompetitionRegistrationEntity::isQualifiedForSecondRun)
                        .sorted(finalStandingsComparator())
                        .toList();
            }

            return registrations.stream()
                    .sorted(finalStandingsComparator())
                    .toList();
        }

        return registrations;
    }

    private Comparator<CompetitionRegistrationEntity> finalStandingsComparator() {
        return Comparator
                .comparing(this::isFinalDnf)
                .thenComparing(this::calculateTotalTime, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private boolean isFinalDnf(CompetitionRegistrationEntity registration) {
        if (registration.getCompetition().getType() == CompetitionType.BIATHLON) {
            return registration.isDidNotFinish();
        }

        return registration.isSecondRunDidNotFinish();
    }

    private BigDecimal calculateTotalTime(CompetitionRegistrationEntity registration) {
        if (registration.getCompetition().getType() == CompetitionType.BIATHLON) {
            return calculateBiathlonTotalTime(registration);
        }

        if (registration.isDidNotFinish() || registration.isSecondRunDidNotFinish()) {
            return null;
        }

        if (registration.getFirstRunTime() == null || registration.getSecondRunTime() == null) {
            return null;
        }

        return registration.getFirstRunTime().add(registration.getSecondRunTime());
    }

    private BigDecimal calculateBiathlonTotalTime(CompetitionRegistrationEntity registration) {
        if (registration.isDidNotFinish()) {
            return null;
        }

        CompetitionEntity competition = registration.getCompetition();
        if (registration.getBiathlonTime() == null
                || registration.getMissedTargets() == null
                || competition.getPenaltySecondsPerMiss() == null) {
            return null;
        }

        BigDecimal penaltySeconds = BigDecimal.valueOf(
                (long) registration.getMissedTargets() * competition.getPenaltySecondsPerMiss()
        );

        return registration.getBiathlonTime().add(penaltySeconds);
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

        if (registration.getCompetition().getType() == CompetitionType.BIATHLON) {
            if (registration.getCompetition().getStatus() == CompetitionStatus.ENDED) {
                return "Finished";
            }

            if (registration.getCompetition().getStatus() == CompetitionStatus.IN_PROGRESS) {
                return "Pending";
            }
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
