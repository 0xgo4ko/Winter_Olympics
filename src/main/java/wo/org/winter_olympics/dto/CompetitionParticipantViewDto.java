package wo.org.winter_olympics.dto;

import wo.org.winter_olympics.data.entity.enums.Gender;

import java.math.BigDecimal;

public class CompetitionParticipantViewDto {

    private Long registrationId;
    private String username;
    private String fullName;
    private String country;
    private Gender gender;
    private String resultStatus;
    private BigDecimal firstRunTime;
    private boolean didNotFinish;
    private boolean qualifiedForSecondRun;
    private BigDecimal secondRunTime;
    private boolean secondRunDidNotFinish;
    private BigDecimal biathlonTime;
    private Integer missedTargets;
    private BigDecimal totalTime;
    private Integer rank;
    private String medal;
    private String podiumCssClass;

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
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

    public boolean isQualifiedForSecondRun() {
        return qualifiedForSecondRun;
    }

    public void setQualifiedForSecondRun(boolean qualifiedForSecondRun) {
        this.qualifiedForSecondRun = qualifiedForSecondRun;
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

    public BigDecimal getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(BigDecimal totalTime) {
        this.totalTime = totalTime;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getMedal() {
        return medal;
    }

    public void setMedal(String medal) {
        this.medal = medal;
    }

    public String getPodiumCssClass() {
        return podiumCssClass;
    }

    public void setPodiumCssClass(String podiumCssClass) {
        this.podiumCssClass = podiumCssClass;
    }
}
