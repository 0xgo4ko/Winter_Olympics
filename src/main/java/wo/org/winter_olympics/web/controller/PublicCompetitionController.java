package wo.org.winter_olympics.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wo.org.winter_olympics.core.service.CompetitionRegistrationService;
import wo.org.winter_olympics.core.service.CompetitionService;
import wo.org.winter_olympics.dto.CompetitionViewDto;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class PublicCompetitionController {

    private final CompetitionService competitionService;
    private final CompetitionRegistrationService competitionRegistrationService;

    public PublicCompetitionController(
            CompetitionService competitionService,
            CompetitionRegistrationService competitionRegistrationService
    ) {
        this.competitionService = competitionService;
        this.competitionRegistrationService = competitionRegistrationService;
    }

    @GetMapping("/competitions")
    public String competitions(Model model, Principal principal) {
        List<CompetitionViewDto> competitions = competitionService.getAllCompetitions();

        if (principal != null) {
            Optional<Long> joinedCompetitionId = competitionRegistrationService.getJoinedCompetitionId(principal.getName());
            joinedCompetitionId.ifPresent(id -> competitions.forEach(competition ->
                    competition.setJoinedByCurrentUser(id.equals(competition.getId()))
            ));
        }

        model.addAttribute("competitions", competitions);
        return "competitions";
    }

    @PostMapping("/competitions/{id}/join")
    public String joinCompetition(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        competitionRegistrationService.joinCompetition(id, principal.getName());
        redirectAttributes.addFlashAttribute("competitionNotice", "You joined the competition successfully.");
        return "redirect:/competitions";
    }

    @PostMapping("/competitions/{id}/leave")
    public String leaveCompetition(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        competitionRegistrationService.leaveCompetition(id, principal.getName());
        redirectAttributes.addFlashAttribute("competitionNotice", "You left the competition successfully.");
        return "redirect:/competitions";
    }
}
