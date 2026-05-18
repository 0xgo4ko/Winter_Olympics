package wo.org.winter_olympics.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import wo.org.winter_olympics.data.common.BaseEntityModel;
import wo.org.winter_olympics.data.entity.enums.CompetitionStatus;
import wo.org.winter_olympics.data.entity.enums.CompetitionType;
import wo.org.winter_olympics.data.entity.enums.Gender;

import java.time.LocalDate;

@Entity
@Table(name = "competitions")
public class CompetitionEntity extends BaseEntityModel {

    @Column(name = "name", nullable = false, unique = true, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CompetitionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "minimum_age", nullable = false)
    private int minimumAge;

    @Column(name = "registration_deadline", nullable = false)
    private LocalDate registrationDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CompetitionStatus status = CompetitionStatus.STARTING_SOON;

    @Column(name = "second_run_qualifier_count")
    private Integer secondRunQualifierCount;

    @Column(name = "penalty_seconds_per_miss")
    private Integer penaltySecondsPerMiss;

    @Column(name = "number_of_laps")
    private Integer numberOfLaps;

    @Column(name = "number_of_targets")
    private Integer numberOfTargets;

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

    public Integer getNumberOfLaps() {
        return numberOfLaps;
    }

    public void setNumberOfLaps(Integer numberOfLaps) {
        this.numberOfLaps = numberOfLaps;
    }

    public Integer getNumberOfTargets() {
        return numberOfTargets;
    }

    public void setNumberOfTargets(Integer numberOfTargets) {
        this.numberOfTargets = numberOfTargets;
    }
}
