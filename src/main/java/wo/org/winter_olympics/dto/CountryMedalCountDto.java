package wo.org.winter_olympics.dto;

public class CountryMedalCountDto {

    private final String country;
    private int contestantsCount;
    private int medalsCount;

    public CountryMedalCountDto(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public int getContestantsCount() {
        return contestantsCount;
    }

    public void incrementContestantsCount() {
        this.contestantsCount++;
    }

    public int getMedalsCount() {
        return medalsCount;
    }

    public void incrementMedalsCount() {
        this.medalsCount++;
    }
}
