package wo.org.winter_olympics.dto;

import java.math.BigDecimal;

public class BiathlonResultInputDto {

    private Long registrationId;
    private BigDecimal biathlonTime;
    private Integer missedTargets;
    private boolean didNotFinish;

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public BigDecimal getBiathlonTime() {
        return biathlonTime;
    }

    public void setBiathlonTime(BigDecimal biathlonTime) {
        this.biathlonTime = biathlonTime;
    }

    public Integer getMissedTargets() {
        return missedTargets;
    }

    public void setMissedTargets(Integer missedTargets) {
        this.missedTargets = missedTargets;
    }

    public boolean isDidNotFinish() {
        return didNotFinish;
    }

    public void setDidNotFinish(boolean didNotFinish) {
        this.didNotFinish = didNotFinish;
    }
}
