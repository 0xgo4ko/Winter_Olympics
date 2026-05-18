package wo.org.winter_olympics.web.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wo.org.winter_olympics.core.service.CompetitionRegistrationService;
import wo.org.winter_olympics.core.service.CompetitionService;
import wo.org.winter_olympics.data.entity.enums.CompetitionType;
import wo.org.winter_olympics.data.entity.enums.Gender;
import wo.org.winter_olympics.dto.CompetitionCreateDto;
import wo.org.winter_olympics.dto.FirstRunResultsFormDto;
import wo.org.winter_olympics.exception.CompetitionResultException;

@Controller
public class AdminCompetitionController {

    private final CompetitionRegistrationService competitionRegistrationService;
    private final CompetitionService competitionService;

    public AdminCompetitionController(
            CompetitionRegistrationService competitionRegistrationService,
            CompetitionService competitionService
    ) {
        this.competitionRegistrationService = competitionRegistrationService;
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

    @GetMapping("/admin/competitions/{id}/edit")
    public String editCompetition(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("competitionCreateDto")) {
            model.addAttribute("competitionCreateDto", competitionService.getCompetitionForEdit(id));
        }

        model.addAttribute("competitionId", id);
        addCompetitionFormOptions(model);

        return "admin/competition-edit";
    }

    @PostMapping("/admin/competitions/{id}/edit")
    public String editCompetition(
            @PathVariable Long id,
            @Valid @ModelAttribute("competitionCreateDto") CompetitionCreateDto competitionCreateDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("competitionId", id);
            addCompetitionFormOptions(model);
            return "admin/competition-edit";
        }

        competitionService.updateCompetition(id, competitionCreateDto);
        redirectAttributes.addFlashAttribute("competitionSuccess", "Competition updated successfully.");

        return "redirect:/admin/competitions";
    }

    @GetMapping("/admin/competitions/{id}/delete")
    public String deleteCompetitionConfirmation(@PathVariable Long id, Model model) {
        model.addAttribute("competition", competitionService.getCompetitionById(id));
        return "admin/competition-delete";
    }

    @PostMapping("/admin/competitions/{id}/delete")
    public String deleteCompetition(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        competitionService.deleteCompetition(id);
        redirectAttributes.addFlashAttribute("competitionSuccess", "Competition deleted successfully.");

        return "redirect:/admin/competitions";
    }

    @PostMapping("/admin/competitions/{id}/start")
    public String startCompetition(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        competitionService.startCompetition(id);
        redirectAttributes.addFlashAttribute("competitionNotice", "Competition started successfully.");

        return "redirect:/competitions/" + id;
    }

    @PostMapping("/admin/competitions/{id}/second-run/start")
    public String startSecondRun(
            @PathVariable Long id,
            @ModelAttribute("firstRunResultsForm") FirstRunResultsFormDto firstRunResultsForm,
            RedirectAttributes redirectAttributes
    ) {
        try {
            competitionRegistrationService.startSecondRun(id, firstRunResultsForm.getResults());
        } catch (CompetitionResultException exception) {
            redirectAttributes.addFlashAttribute("competitionNotice", exception.getMessage());
            redirectAttributes.addFlashAttribute("firstRunResultsForm", firstRunResultsForm);
            return "redirect:/competitions/" + id;
        }

        redirectAttributes.addFlashAttribute("competitionNotice", "Second run started successfully.");

        return "redirect:/competitions/" + id;
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
