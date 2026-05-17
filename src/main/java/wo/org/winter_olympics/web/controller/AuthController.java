package wo.org.winter_olympics.web.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import wo.org.winter_olympics.core.service.UserRegistrationService;
import wo.org.winter_olympics.data.entity.enums.Gender;
import wo.org.winter_olympics.dto.UserRegisterDto;

@Controller
public class AuthController {

    private final UserRegistrationService userRegistrationService;

    public AuthController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("userRegisterDto")) {
            model.addAttribute("userRegisterDto", new UserRegisterDto());
        }

        model.addAttribute("genders", Gender.values());

        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("userRegisterDto") UserRegisterDto userRegisterDto,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request
    ) throws ServletException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("genders", Gender.values());
            return "register";
        }

        userRegistrationService.register(userRegisterDto);
        request.login(userRegisterDto.getUsername(), userRegisterDto.getPassword());

        return "redirect:/";
    }
}
