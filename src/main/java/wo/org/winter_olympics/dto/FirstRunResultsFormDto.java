package wo.org.winter_olympics.dto;

import java.util.ArrayList;
import java.util.List;

public class FirstRunResultsFormDto {

    private List<FirstRunResultInputDto> results = new ArrayList<>();

    public List<FirstRunResultInputDto> getResults() {
        return results;
    }

    public void setResults(List<FirstRunResultInputDto> results) {
        this.results = results;
    }
}
