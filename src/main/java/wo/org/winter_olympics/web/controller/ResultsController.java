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

        model.addAttribute("showCountryMedals", showCountryMedals);
        if (showCountryMedals) {
            model.addAttribute("countryMedals", competitionService.getCountryMedalCounts());
        }

        return "results";
    }
}
