package wo.org.winter_olympics.exception;

public class CompetitionNotFoundException extends RuntimeException {

    public CompetitionNotFoundException(Long id) {
        super("Competition was not found: " + id);
    }
}
