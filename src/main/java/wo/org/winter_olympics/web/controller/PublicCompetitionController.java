package wo.org.winter_olympics.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wo.org.winter_olympics.core.service.CompetitionService;
import wo.org.winter_olympics.dto.CompetitionParticipantViewDto;
import wo.org.winter_olympics.dto.CompetitionViewDto;
import wo.org.winter_olympics.dto.FirstRunResultInputDto;
import wo.org.winter_olympics.dto.FirstRunResultsFormDto;
import wo.org.winter_olympics.dto.SecondRunResultInputDto;
import wo.org.winter_olympics.dto.SecondRunResultsFormDto;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class PublicCompetitionController {

    private final CompetitionService competitionService;

    public PublicCompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @GetMapping("/competitions")
    public String competitions(Model model, Principal principal) {
        List<CompetitionViewDto> competitions = competitionService.getAllCompetitions();

        if (principal != null) {
            Optional<Long> joinedCompetitionId = competitionService.getJoinedCompetitionId(principal.getName());
            joinedCompetitionId.ifPresent(id -> competitions.forEach(competition ->
                    competition.setJoinedByCurrentUser(id.equals(competition.getId()))
            ));
        }

        model.addAttribute("competitions", competitions);
        return "competitions";
    }

    @GetMapping("/competitions/{id}")
    public String competitionDetails(@PathVariable Long id, Model model) {
        List<CompetitionParticipantViewDto> participants =
                competitionService.getParticipantsForCompetition(id);

        FirstRunResultsFormDto firstRunResultsForm = resolveFirstRunResultsForm(model, participants);
        SecondRunResultsFormDto secondRunResultsForm = resolveSecondRunResultsForm(model, participants);

        model.addAttribute("competition", competitionService.getCompetitionById(id));
        model.addAttribute("participants", participants);
        model.addAttribute("firstRunResultsForm", firstRunResultsForm);
        model.addAttribute("secondRunResultsForm", secondRunResultsForm);
        return "competition-details";
    }

    @PostMapping("/competitions/{id}/join")
    public String joinCompetition(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        competitionService.joinCompetition(id, principal.getName());
        redirectAttributes.addFlashAttribute("competitionNotice", "You joined the competition successfully.");
        return "redirect:/competitions";
    }

    @PostMapping("/competitions/{id}/leave")
    public String leaveCompetition(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        competitionService.leaveCompetition(id, principal.getName());
        redirectAttributes.addFlashAttribute("competitionNotice", "You left the competition successfully.");
        return "redirect:/competitions";
    }

    private FirstRunResultsFormDto resolveFirstRunResultsForm(
            Model model,
            List<CompetitionParticipantViewDto> participants
    ) {
        if (model.containsAttribute("firstRunResultsForm")) {
            FirstRunResultsFormDto submittedForm =
                    (FirstRunResultsFormDto) model.asMap().get("firstRunResultsForm");
            return alignSubmittedResultsWithParticipants(submittedForm, participants);
        }

        return createFirstRunResultsForm(participants);
    }

    private FirstRunResultsFormDto alignSubmittedResultsWithParticipants(
            FirstRunResultsFormDto submittedForm,
            List<CompetitionParticipantViewDto> participants
    ) {
        Map<Long, FirstRunResultInputDto> submittedResultsByRegistrationId =
                Optional.ofNullable(submittedForm.getResults())
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(result -> result.getRegistrationId() != null)
                        .collect(Collectors.toMap(
                                FirstRunResultInputDto::getRegistrationId,
                                Function.identity(),
                                (first, second) -> first
                        ));

        FirstRunResultsFormDto alignedForm = new FirstRunResultsFormDto();
        List<FirstRunResultInputDto> alignedResults = participants.stream()
                .map(participant -> createFirstRunResultInput(
                        participant,
                        submittedResultsByRegistrationId.get(participant.getRegistrationId())
                ))
                .toList();

        alignedForm.setResults(alignedResults);
        return alignedForm;
    }

    private FirstRunResultsFormDto createFirstRunResultsForm(List<CompetitionParticipantViewDto> participants) {
        FirstRunResultsFormDto firstRunResultsForm = new FirstRunResultsFormDto();
        List<FirstRunResultInputDto> results = participants.stream()
                .map(participant -> createFirstRunResultInput(participant, null))
                .toList();

        firstRunResultsForm.setResults(results);
        return firstRunResultsForm;
    }

    private FirstRunResultInputDto createFirstRunResultInput(
            CompetitionParticipantViewDto participant,
            FirstRunResultInputDto submittedResult
    ) {
        FirstRunResultInputDto input = new FirstRunResultInputDto();
        input.setRegistrationId(participant.getRegistrationId());

        if (submittedResult != null) {
            input.setFirstRunTime(submittedResult.getFirstRunTime());
            input.setDidNotFinish(submittedResult.isDidNotFinish());
            participant.setFirstRunTime(submittedResult.getFirstRunTime());
            participant.setDidNotFinish(submittedResult.isDidNotFinish());
        } else {
            input.setFirstRunTime(participant.getFirstRunTime());
            input.setDidNotFinish(participant.isDidNotFinish());
        }

        return input;
    }

    private SecondRunResultsFormDto resolveSecondRunResultsForm(
            Model model,
            List<CompetitionParticipantViewDto> participants
    ) {
        if (model.containsAttribute("secondRunResultsForm")) {
            SecondRunResultsFormDto submittedForm =
                    (SecondRunResultsFormDto) model.asMap().get("secondRunResultsForm");
            return alignSubmittedSecondRunResultsWithParticipants(submittedForm, participants);
        }

        return createSecondRunResultsForm(participants);
    }

    private SecondRunResultsFormDto alignSubmittedSecondRunResultsWithParticipants(
            SecondRunResultsFormDto submittedForm,
            List<CompetitionParticipantViewDto> participants
    ) {
        Map<Long, SecondRunResultInputDto> submittedResultsByRegistrationId =
                Optional.ofNullable(submittedForm.getResults())
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(result -> result.getRegistrationId() != null)
                        .collect(Collectors.toMap(
                                SecondRunResultInputDto::getRegistrationId,
                                Function.identity(),
                                (first, second) -> first
                        ));

        SecondRunResultsFormDto alignedForm = new SecondRunResultsFormDto();
        List<SecondRunResultInputDto> alignedResults = participants.stream()
                .map(participant -> createSecondRunResultInput(
                        participant,
                        submittedResultsByRegistrationId.get(participant.getRegistrationId())
                ))
                .toList();

        alignedForm.setResults(alignedResults);
        return alignedForm;
    }

    private SecondRunResultsFormDto createSecondRunResultsForm(List<CompetitionParticipantViewDto> participants) {
        SecondRunResultsFormDto secondRunResultsForm = new SecondRunResultsFormDto();
        List<SecondRunResultInputDto> results = participants.stream()
                .map(participant -> createSecondRunResultInput(participant, null))
                .toList();

        secondRunResultsForm.setResults(results);
        return secondRunResultsForm;
    }

    private SecondRunResultInputDto createSecondRunResultInput(
            CompetitionParticipantViewDto participant,
            SecondRunResultInputDto submittedResult
    ) {
        SecondRunResultInputDto input = new SecondRunResultInputDto();
        input.setRegistrationId(participant.getRegistrationId());

        if (submittedResult != null) {
            input.setSecondRunTime(submittedResult.getSecondRunTime());
            input.setSecondRunDidNotFinish(submittedResult.isSecondRunDidNotFinish());
            participant.setSecondRunTime(submittedResult.getSecondRunTime());
            participant.setSecondRunDidNotFinish(submittedResult.isSecondRunDidNotFinish());
        } else {
            input.setSecondRunTime(participant.getSecondRunTime());
            input.setSecondRunDidNotFinish(participant.isSecondRunDidNotFinish());
        }

        return input;
    }
}
