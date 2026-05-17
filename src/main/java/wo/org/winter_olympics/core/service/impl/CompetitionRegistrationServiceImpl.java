package wo.org.winter_olympics.core.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wo.org.winter_olympics.core.service.CompetitionRegistrationService;
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
import wo.org.winter_olympics.exception.UserNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

        if (competition.getStatus() != CompetitionStatus.OPEN) {
            throw new CompetitionJoinException("You can only leave a competition before it starts.");
        }

        competitionRegistrationRepository.delete(registration);
    }

    private AppUserEntity getUser(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private CompetitionEntity getCompetition(Long competitionId) {
        return competitionRepository.findById(competitionId)
                .orElseThrow(() -> new CompetitionNotFoundException(competitionId));
    }

    private void validateCompetitionCanBeJoined(CompetitionEntity competition) {
        if (competition.getStatus() != CompetitionStatus.OPEN) {
            throw new CompetitionJoinException("You cannot join a competition after it has started.");
        }

        if (competition.getRegistrationDeadline().isBefore(LocalDate.now())) {
            throw new CompetitionJoinException("Registration for this competition is closed.");
        }
    }

    private CompetitionParticipantViewDto mapToParticipantViewDto(CompetitionRegistrationEntity registration) {
        AppUserEntity user = registration.getUser();

        CompetitionParticipantViewDto participantViewDto = new CompetitionParticipantViewDto();
        participantViewDto.setUsername(user.getUsername());
        participantViewDto.setFullName(user.getFullName());
        participantViewDto.setCountry(user.getCountry());
        participantViewDto.setGender(user.getGender());
        participantViewDto.setResultStatus("Pending");

        return participantViewDto;
    }
}
