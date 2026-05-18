package wo.org.winter_olympics.exception;

public class CompetitionStartException extends RuntimeException {

    private final Long competitionId;

    public CompetitionStartException(Long competitionId, String message) {
        super(message);
        this.competitionId = competitionId;
    }

    public Long getCompetitionId() {
        return competitionId;
    }
}
