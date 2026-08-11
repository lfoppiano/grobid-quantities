package org.grobid.core.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The `text` field carries the text the offsets refer to, for the callers that do not have it -
 * the XML endpoint extracts the text from the markup itself.
 * See https://github.com/lfoppiano/grobid-quantities/issues/3
 */
class MeasurementsResponseTest {

    private JsonNode json(MeasurementsResponse response) throws Exception {
        return new ObjectMapper().readTree(response.toJson());
    }

    @Test
    void withoutText_shouldNotEmitTheField() throws Exception {
        MeasurementsResponse response = new MeasurementsResponse(Collections.emptyList());
        response.setRuntime(12);

        JsonNode node = json(response);

        assertFalse(node.has("text"), "the text endpoint's response must be unchanged");
        assertEquals(12, node.get("runtime").asLong());
    }

    @Test
    void aBlankText_shouldNotEmitTheFieldEither() throws Exception {
        MeasurementsResponse response = new MeasurementsResponse(Collections.emptyList());
        response.setText("   ");

        assertFalse(json(response).has("text"));
    }

    @Test
    void withText_shouldEmitItVerbatim() throws Exception {
        MeasurementsResponse response = new MeasurementsResponse(Collections.emptyList());
        response.setText("We lost 10 kg.\nThen 20 more.");

        assertEquals("We lost 10 kg.\nThen 20 more.", json(response).get("text").asText());
    }

    /**
     * The JSON is built by hand, so anything the extracted text may contain - quotes, newlines,
     * backslashes, non-ASCII - has to survive the round trip.
     */
    @Test
    void aTextNeedingEscaping_shouldStayValidJson() throws Exception {
        String awkward = "a \"quoted\" value,\na back\\slash, a tab\there, and 10 µm ± 2 µm";

        MeasurementsResponse response = new MeasurementsResponse(Collections.emptyList());
        response.setText(awkward);

        assertEquals(awkward, json(response).get("text").asText());
    }
}
