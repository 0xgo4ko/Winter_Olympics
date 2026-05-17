package wo.org.winter_olympics.web.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wo.org.winter_olympics.exception.PasswordMismatchException;
import wo.org.winter_olympics.exception.RoleNotFoundException;
import wo.org.winter_olympics.exception.UsernameAlreadyExistsException;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    @ExceptionHandler({UsernameAlreadyExistsException.class, PasswordMismatchException.class})
    public String handleRegistrationException(RuntimeException exception, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("registrationError", exception.getMessage());
        return "redirect:/register";
    }

    @ExceptionHandler({RoleNotFoundException.class, ServletException.class})
    public String handleApplicationException(
            Exception exception,
            Model model,
            HttpServletResponse response
    ) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("errorMessage", exception.getMessage());
        return "error";
    }
}
