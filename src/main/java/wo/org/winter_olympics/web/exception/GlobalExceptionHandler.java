package wo.org.winter_olympics.web.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wo.org.winter_olympics.exception.CompetitionJoinException;
import wo.org.winter_olympics.exception.CompetitionNameAlreadyExistsException;
import wo.org.winter_olympics.exception.CompetitionNotFoundException;
import wo.org.winter_olympics.exception.CompetitionStartException;
import wo.org.winter_olympics.exception.PasswordMismatchException;
import wo.org.winter_olympics.exception.RoleNotFoundException;
import wo.org.winter_olympics.exception.UsernameAlreadyExistsException;
import wo.org.winter_olympics.exception.UserNotFoundException;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    @ExceptionHandler({UsernameAlreadyExistsException.class, PasswordMismatchException.class})
    public String handleRegistrationException(RuntimeException exception, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("registrationError", exception.getMessage());
        return "redirect:/register";
    }

    @ExceptionHandler(CompetitionNameAlreadyExistsException.class)
    public String handleCompetitionCreateException(
            CompetitionNameAlreadyExistsException exception,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        redirectAttributes.addFlashAttribute("competitionError", exception.getMessage());

        String requestUri = request.getRequestURI();
        if (requestUri.contains("/edit")) {
            return "redirect:" + requestUri;
        }

        return "redirect:/admin/competitions/create";
    }

    @ExceptionHandler(CompetitionJoinException.class)
    public String handleCompetitionJoinException(
            CompetitionJoinException exception,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("competitionNotice", exception.getMessage());
        return "redirect:/competitions";
    }

    @ExceptionHandler(CompetitionStartException.class)
    public String handleCompetitionStartException(
            CompetitionStartException exception,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("competitionNotice", exception.getMessage());
        return "redirect:/competitions/" + exception.getCompetitionId();
    }

    @ExceptionHandler({
            CompetitionNotFoundException.class,
            RoleNotFoundException.class,
            ServletException.class,
            UserNotFoundException.class
    })
    public String handleApplicationException(
            Exception exception,
            Model model,
            HttpServletResponse response
    ) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorMessage", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpectedException(
            Exception exception,
            Model model,
            HttpServletResponse response
    ) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorMessage", exception.getMessage());
        return "error";
    }
}
