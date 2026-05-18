package wo.org.winter_olympics.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(
            @RequestParam(name = "adminOnly", defaultValue = "false") boolean adminOnly,
            Model model
    ) {
        model.addAttribute("pageTitle", "Winter Olympics");
        model.addAttribute("adminOnly", adminOnly);
        return "home";
    }
}
