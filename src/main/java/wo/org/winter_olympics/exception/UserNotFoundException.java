package wo.org.winter_olympics.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String username) {
        super("User was not found: " + username);
    }
}
