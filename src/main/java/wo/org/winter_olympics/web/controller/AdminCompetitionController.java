package wo.org.winter_olympics.web.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wo.org.winter_olympics.core.service.CompetitionService;
import wo.org.winter_olympics.data.entity.enums.CompetitionType;
import wo.org.winter_olympics.data.entity.enums.Gender;
import wo.org.winter_olympics.dto.CompetitionCreateDto;

@Controller
public class AdminCompetitionController {

    private final CompetitionService competitionService;

    public AdminCompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @GetMapping("/admin/competitions")
    public String manageCompetitions(Model model) {
        model.addAttribute("competitions", competitionService.getAllCompetitions());
        return "admin/competitions";
    }

    @GetMapping("/admin/competitions/create")
    public String createCompetition(Model model) {
        addCompetitionFormModel(model);
        return "admin/competition-create";
    }

    @PostMapping("/admin/competitions/create")
    public String createCompetition(
            @Valid @ModelAttribute("competitionCreateDto") CompetitionCreateDto competitionCreateDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addCompetitionFormOptions(model);
            return "admin/competition-create";
        }

        competitionService.createCompetition(competitionCreateDto);
        redirectAttributes.addFlashAttribute("competitionSuccess", "Competition created successfully.");

        return "redirect:/admin/competitions";
    }

    private void addCompetitionFormModel(Model model) {
        if (!model.containsAttribute("competitionCreateDto")) {
            model.addAttribute("competitionCreateDto", new CompetitionCreateDto());
        }

        addCompetitionFormOptions(model);
    }

    private void addCompetitionFormOptions(Model model) {
        model.addAttribute("competitionTypes", CompetitionType.values());
        model.addAttribute("genders", Gender.values());
    }
}
