package wo.org.winter_olympics.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import wo.org.winter_olympics.core.service.CompetitionService;

@Controller
public class ResultsController {

    private final CompetitionService competitionService;

    public ResultsController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @GetMapping("/results")
    public String results(
            @RequestParam(name = "query", required = false) String query,
            Model model
    ) {
        boolean showCountryMedals = "country-medals".equals(query);
        boolean showAverageAge = "average-age".equals(query);
        boolean showMedalistAges = "medalist-ages".equals(query);

        model.addAttribute("showCountryMedals", showCountryMedals);
        model.addAttribute("showAverageAge", showAverageAge);
        model.addAttribute("showMedalistAges", showMedalistAges);
        if (showCountryMedals) {
            model.addAttribute("countryMedals", competitionService.getCountryMedalCounts());
        }
        if (showAverageAge) {
            model.addAttribute("participantAgeStats", competitionService.getParticipantAgeStats());
        }
        if (showMedalistAges) {
            model.addAttribute("medalistAges", competitionService.getYoungestAndOldestMedalists());
        }

        return "results";
    }
}
