package wo.org.winter_olympics.dto;

import java.math.BigDecimal;

public class FirstRunResultInputDto {

    private Long registrationId;
    private BigDecimal firstRunTime;
    private boolean didNotFinish;

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public BigDecimal getFirstRunTime() {
        return firstRunTime;
    }

    public void setFirstRunTime(BigDecimal firstRunTime) {
        this.firstRunTime = firstRunTime;
    }

    public boolean isDidNotFinish() {
        return didNotFinish;
    }

    public void setDidNotFinish(boolean didNotFinish) {
        this.didNotFinish = didNotFinish;
    }
}
