package wo.org.winter_olympics.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import wo.org.winter_olympics.core.service.CompetitionService;

@Controller
public class PublicCompetitionController {

    private final CompetitionService competitionService;

    public PublicCompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @GetMapping("/competitions")
    public String competitions(Model model) {
        model.addAttribute("competitions", competitionService.getAllCompetitions());
        return "competitions";
    }
}
