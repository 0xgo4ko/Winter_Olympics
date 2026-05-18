package wo.org.winter_olympics.dto;

import java.math.BigDecimal;

public class SecondRunResultInputDto {

    private Long registrationId;
    private BigDecimal secondRunTime;
    private boolean secondRunDidNotFinish;

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public BigDecimal getSecondRunTime() {
        return secondRunTime;
    }

    public void setSecondRunTime(BigDecimal secondRunTime) {
        this.secondRunTime = secondRunTime;
    }

    public boolean isSecondRunDidNotFinish() {
        return secondRunDidNotFinish;
    }

    public void setSecondRunDidNotFinish(boolean secondRunDidNotFinish) {
        this.secondRunDidNotFinish = secondRunDidNotFinish;
    }
}
