package wo.org.winter_olympics.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicCompetitionController {

    @GetMapping("/competitions")
    public String competitions() {
        return "competitions";
    }
}
