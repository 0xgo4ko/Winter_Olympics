package wo.org.winter_olympics.exception;

public class PasswordMismatchException extends RuntimeException {

    public PasswordMismatchException() {
        super("Passwords do not match");
    }
}
