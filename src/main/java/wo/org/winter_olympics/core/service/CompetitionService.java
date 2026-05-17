package wo.org.winter_olympics.core.service;

import wo.org.winter_olympics.dto.CompetitionCreateDto;
import wo.org.winter_olympics.dto.CompetitionViewDto;

import java.util.List;

public interface CompetitionService {

    List<CompetitionViewDto> getAllCompetitions();

    void createCompetition(CompetitionCreateDto competitionCreateDto);
}
