package wo.org.winter_olympics.exception;

public class CompetitionResultException extends RuntimeException {

    private final Long competitionId;

    public CompetitionResultException(Long competitionId, String message) {
        super(message);
        this.competitionId = competitionId;
    }

    public Long getCompetitionId() {
        return competitionId;
    }
}
