package wo.org.winter_olympics.core.service;

import wo.org.winter_olympics.dto.CompetitionCreateDto;
import wo.org.winter_olympics.dto.CompetitionParticipantViewDto;
import wo.org.winter_olympics.dto.CompetitionViewDto;
import wo.org.winter_olympics.dto.FirstRunResultInputDto;
import wo.org.winter_olympics.dto.SecondRunResultInputDto;

import java.util.List;
import java.util.Optional;

public interface CompetitionService {

    List<CompetitionViewDto> getAllCompetitions();

    CompetitionCreateDto getCompetitionForEdit(Long id);

    CompetitionViewDto getCompetitionById(Long id);

    void createCompetition(CompetitionCreateDto competitionCreateDto);

    void updateCompetition(Long id, CompetitionCreateDto competitionCreateDto);

    void startCompetition(Long id);

    void deleteCompetition(Long id);

    Optional<Long> getJoinedCompetitionId(String username);

    List<CompetitionParticipantViewDto> getParticipantsForCompetition(Long competitionId);

    void joinCompetition(Long competitionId, String username);

    void leaveCompetition(Long competitionId, String username);

    void startSecondRun(Long competitionId, List<FirstRunResultInputDto> firstRunResults);

    void endCompetition(Long competitionId, List<SecondRunResultInputDto> secondRunResults);
}
