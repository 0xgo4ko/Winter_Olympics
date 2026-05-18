package wo.org.winter_olympics.dto;

import java.util.ArrayList;
import java.util.List;

public class SecondRunResultsFormDto {

    private List<SecondRunResultInputDto> results = new ArrayList<>();

    public List<SecondRunResultInputDto> getResults() {
        return results;
    }

    public void setResults(List<SecondRunResultInputDto> results) {
        this.results = results;
    }
}
