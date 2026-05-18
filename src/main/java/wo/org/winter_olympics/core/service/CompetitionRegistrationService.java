package wo.org.winter_olympics.core.service;

import wo.org.winter_olympics.dto.CompetitionParticipantViewDto;
import wo.org.winter_olympics.dto.FirstRunResultInputDto;

import java.util.List;
import java.util.Optional;

public interface CompetitionRegistrationService {

    Optional<Long> getJoinedCompetitionId(String username);

    List<CompetitionParticipantViewDto> getParticipantsForCompetition(Long competitionId);

    void joinCompetition(Long competitionId, String username);

    void leaveCompetition(Long competitionId, String username);

    void startSecondRun(Long competitionId, List<FirstRunResultInputDto> firstRunResults);
}
