package org.grobid.core.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import org.grobid.core.layout.Page;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

    /**
     * The text the measurement offsets refer to, when the caller does not already have it.
     * Set by the XML endpoint, which extracts the text from the markup before processing it -
     * without it the offsets in the response cannot be resolved to anything. Left null by the
     * text endpoint, where the caller supplied the text in the first place.
     * <p>
     * See https://github.com/lfoppiano/grobid-quantities/issues/3
     */
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

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
        if (isNotBlank(getText())) {
            byte[] encodedText = JsonStringEncoder.getInstance().quoteAsUTF8(getText());
            jsonBuilder.append(", \"text\" : \"" + new String(encodedText, UTF_8) + "\"");
        }
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
