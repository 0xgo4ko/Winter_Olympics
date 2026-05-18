package wo.org.winter_olympics.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import wo.org.winter_olympics.data.entity.enums.CompetitionType;
import wo.org.winter_olympics.data.entity.enums.Gender;

import java.time.LocalDate;

public class CompetitionCreateDto {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    private CompetitionType type;

    @NotNull
    private Gender gender;

    @Min(1)
    @Max(100)
    private int minimumAge;

    @NotNull
    @FutureOrPresent
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationDeadline;

    @Min(1)
    @Max(100)
    private Integer secondRunQualifierCount;

    @Min(1)
    @Max(3600)
    private Integer penaltySecondsPerMiss;

    @Min(1)
    @Max(100)
    private Integer numberOfLaps;

    @Min(1)
    @Max(100)
    private Integer numberOfTargets;

    @AssertTrue(message = "Ski slalom competitions require a second-run qualifier count")
    public boolean isSkiSlalomConfigurationValid() {
        return type != CompetitionType.SKI_SLALOM || secondRunQualifierCount != null;
    }

    @AssertTrue(message = "Biathlon competitions require penalty seconds per miss, number of laps, and number of targets")
    public boolean isBiathlonConfigurationValid() {
        return type != CompetitionType.BIATHLON
                || (penaltySecondsPerMiss != null && numberOfLaps != null && numberOfTargets != null);
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
