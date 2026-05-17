package wo.org.winter_olympics.web.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wo.org.winter_olympics.core.service.UserProfileService;
import wo.org.winter_olympics.data.entity.enums.Gender;
import wo.org.winter_olympics.dto.UserProfileDto;

import java.security.Principal;

@Controller
public class ProfileController {

    private final UserProfileService userProfileService;

    public ProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        if (!model.containsAttribute("userProfileDto")) {
            model.addAttribute("userProfileDto", userProfileService.getProfile(principal.getName()));
        }

        model.addAttribute("genders", Gender.values());

        return "profile";
    }

    @GetMapping("/athlete/profile")
    public String legacyAthleteProfile() {
        return "redirect:/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            Principal principal,
            @Valid @ModelAttribute("userProfileDto") UserProfileDto userProfileDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            UserProfileDto currentProfile = userProfileService.getProfile(principal.getName());
            userProfileDto.setUsername(currentProfile.getUsername());
            userProfileDto.setDateOfBirth(currentProfile.getDateOfBirth());
            model.addAttribute("genders", Gender.values());
            return "profile";
        }

        userProfileService.updateProfile(principal.getName(), userProfileDto);
        redirectAttributes.addFlashAttribute("profileSuccess", "Profile updated successfully.");

        return "redirect:/profile";
    }

    @GetMapping("/profile/delete")
    public String deleteProfileConfirmation(Principal principal, Model model) {
        model.addAttribute("userProfileDto", userProfileService.getProfile(principal.getName()));
        return "profile-delete";
    }

    @PostMapping("/profile/delete")
    public String deleteProfile(Principal principal, HttpServletRequest request) throws ServletException {
        userProfileService.deleteProfile(principal.getName());
        request.logout();

        return "redirect:/";
    }
}
