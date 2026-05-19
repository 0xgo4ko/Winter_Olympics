package wo.org.winter_olympics.dto;

public class MedalistAgeDto {

    private final String label;
    private final String fullName;
    private final String country;
    private final String competitionName;
    private final int age;

    public MedalistAgeDto(
            String label,
            String fullName,
            String country,
            String competitionName,
            int age
    ) {
        this.label = label;
        this.fullName = fullName;
        this.country = country;
        this.competitionName = competitionName;
        this.age = age;
    }

    public String getLabel() {
        return label;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCountry() {
        return country;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public int getAge() {
        return age;
    }
}
