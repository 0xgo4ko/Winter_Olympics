package wo.org.winter_olympics.core.service;

import java.util.Optional;

public interface CompetitionRegistrationService {

    Optional<Long> getJoinedCompetitionId(String username);

    void joinCompetition(Long competitionId, String username);

    void leaveCompetition(Long competitionId, String username);
}
