package wo.org.winter_olympics.core.service;

import wo.org.winter_olympics.dto.CompetitionCreateDto;
import wo.org.winter_olympics.dto.CompetitionViewDto;

import java.util.List;

public interface CompetitionService {

    List<CompetitionViewDto> getAllCompetitions();

    CompetitionCreateDto getCompetitionForEdit(Long id);

    CompetitionViewDto getCompetitionById(Long id);

    void createCompetition(CompetitionCreateDto competitionCreateDto);

    void updateCompetition(Long id, CompetitionCreateDto competitionCreateDto);

    void deleteCompetition(Long id);
}
