package wo.org.winter_olympics.dto;

import java.util.ArrayList;
import java.util.List;

public class BiathlonResultsFormDto {

    private List<BiathlonResultInputDto> results = new ArrayList<>();

    public List<BiathlonResultInputDto> getResults() {
        return results;
    }

    public void setResults(List<BiathlonResultInputDto> results) {
        this.results = results;
    }
}
