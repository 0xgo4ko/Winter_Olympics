package wo.org.winter_olympics.exception;

public class CompetitionNameAlreadyExistsException extends RuntimeException {

    public CompetitionNameAlreadyExistsException(String name) {
        super("Competition name is already taken: " + name);
    }
}
