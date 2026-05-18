package wo.org.winter_olympics.dto;

import wo.org.winter_olympics.data.entity.enums.CompetitionStatus;
import wo.org.winter_olympics.data.entity.enums.CompetitionType;
import wo.org.winter_olympics.data.entity.enums.Gender;

import java.time.LocalDate;

public class CompetitionViewDto {

    private Long id;
    private String name;
    private CompetitionType type;
    private Gender gender;
    private int minimumAge;
    private LocalDate registrationDeadline;
    private CompetitionStatus status;
    private Integer secondRunQualifierCount;
    private Integer penaltySecondsPerMiss;
    private boolean joinedByCurrentUser;
    private boolean startingSoon;
    private boolean firstRun;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CompetitionType getType() {
        return type;
    }

    public void setType(CompetitionType type) {
        this.type = type;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getMinimumAge() {
        return minimumAge;
    }

    public void setMinimumAge(int minimumAge) {
        this.minimumAge = minimumAge;
    }

    public LocalDate getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDate registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public CompetitionStatus getStatus() {
        return status;
    }

    public void setStatus(CompetitionStatus status) {
        this.status = status;
    }

    public Integer getSecondRunQualifierCount() {
        return secondRunQualifierCount;
    }

    public void setSecondRunQualifierCount(Integer secondRunQualifierCount) {
        this.secondRunQualifierCount = secondRunQualifierCount;
    }

    public Integer getPenaltySecondsPerMiss() {
        return penaltySecondsPerMiss;
    }

    public void setPenaltySecondsPerMiss(Integer penaltySecondsPerMiss) {
        this.penaltySecondsPerMiss = penaltySecondsPerMiss;
    }

    public boolean isJoinedByCurrentUser() {
        return joinedByCurrentUser;
    }

    public void setJoinedByCurrentUser(boolean joinedByCurrentUser) {
        this.joinedByCurrentUser = joinedByCurrentUser;
    }

    public boolean isStartingSoon() {
        return startingSoon;
    }

    public void setStartingSoon(boolean startingSoon) {
        this.startingSoon = startingSoon;
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }
}
