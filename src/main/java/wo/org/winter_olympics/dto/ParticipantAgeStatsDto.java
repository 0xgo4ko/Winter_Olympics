package wo.org.winter_olympics.dto;

import java.math.BigDecimal;

public class ParticipantAgeStatsDto {

    private final int participantsCount;
    private final BigDecimal averageAge;

    public ParticipantAgeStatsDto(int participantsCount, BigDecimal averageAge) {
        this.participantsCount = participantsCount;
        this.averageAge = averageAge;
    }

    public int getParticipantsCount() {
        return participantsCount;
    }

    public BigDecimal getAverageAge() {
        return averageAge;
    }
}
