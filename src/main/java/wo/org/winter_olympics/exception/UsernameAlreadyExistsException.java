package wo.org.winter_olympics.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Username is already taken: " + username);
    }
}
