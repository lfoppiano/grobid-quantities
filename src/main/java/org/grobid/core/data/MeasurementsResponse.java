package org.grobid.core.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.grobid.core.layout.Page;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@JsonInclude(Include.NON_EMPTY)
public class MeasurementsResponse {

    private long runtime;
    private List<Measurement> measurements;

    public MeasurementsResponse() {
        measurements = new ArrayList<>();
    }

    public MeasurementsResponse(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public MeasurementsResponse(List<Measurement> measurements, List<Page> pages) {
        this.measurements = measurements;
        this.pages = pages;
    }

    public MeasurementsResponse extendEntities(MeasurementsResponse other) {
        this.measurements.addAll(other.getMeasurements());

        return this;
    }

    private List<Page> pages;

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public long getRuntime() {
        return runtime;
    }

    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public String toJson() {
        StringBuilder jsonBuilder = new StringBuilder();

        jsonBuilder.append("{ ");
        jsonBuilder.append("\"runtime\" : " + runtime);
        boolean first = true;
        if (isNotEmpty(getPages())) {
            // page height and width
            jsonBuilder.append(", \"pages\":[");
            List<Page> pages = getPages();
            for (Page page : pages) {
                if (first)
                    first = false;
                else
                    jsonBuilder.append(", ");
                jsonBuilder.append("{\"page_height\":" + page.getHeight());
                jsonBuilder.append(", \"page_width\":" + page.getWidth() + "}");
            }
            jsonBuilder.append("]");
        }

        if (isNotEmpty(getMeasurements())) {
            jsonBuilder.append(", \"measurements\": [");
            first = true;
            for (Measurement temperature : getMeasurements()) {
                if (!first)
                    jsonBuilder.append(", ");
                else
                    first = false;
                jsonBuilder.append(temperature.toJson());
            }
            jsonBuilder.append("]");
        }

        jsonBuilder.append("}");

        return jsonBuilder.toString();
    }
}
