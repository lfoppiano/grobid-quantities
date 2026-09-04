package org.grobid.core.utilities;

import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.UnitDefinition;
import org.grobid.core.engines.QuantityParser;
import org.grobid.trainer.sax.MeasureAnnotationSaxHandler;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

/**
 * See https://github.com/lfoppiano/grobid-quantities/issues/20
 */
public class TeiUtilsTest {

    @Test
    public void testToTei_atomicValue_shouldAnnotateInline() {
        String text = "I've lost two minutes.";

        Unit unit = new Unit("minutes", 14, 21);
        unit.setUnitDefinition(new UnitDefinition(UnitUtilities.Unit_Type.TIME, UnitUtilities.System_Type.NON_SI));
        Quantity quantity = new Quantity("two", unit, 10, 13);

        Measurement measurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurement.setAtomicQuantity(quantity);

        String tei = TeiUtils.toTei(Collections.singletonList(measurement), text);

        assertThat(tei, containsString("<measure type=\"value\">"));
        assertThat(tei, containsString("<num>two</num>"));
        assertThat(tei, containsString("<measure type=\"TIME\" unit=\"minutes\">minutes</measure>"));
    }

    @Test
    public void testToTei_interval_shouldAnnotateBothExtremities() {
        String text = "between 3 and 5 kg of it";

        Quantity least = new Quantity("3", null, 8, 9);
        Unit unit = new Unit("kg", 16, 18);
        Quantity most = new Quantity("5", unit, 14, 15);

        Measurement measurement = new Measurement(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        measurement.setQuantityLeast(least);
        measurement.setQuantityMost(most);

        String tei = TeiUtils.toTei(Collections.singletonList(measurement), text);

        assertThat(tei, containsString("<measure type=\"interval\">"));
        assertThat(tei, containsString("atLeast=\"3\""));
        assertThat(tei, containsString("atMost=\"5\""));
    }

    @Test
    public void testToTei_noMeasurement_shouldKeepTheText() {
        String tei = TeiUtils.toTei(Arrays.asList(), "nothing to see here");

        assertThat(tei, containsString("<teiHeader>"));
        assertThat(tei, containsString("<p>nothing to see here</p>"));
    }

    @Test
    public void testToTei_atomicValue_shouldKeepTheSurroundingText() {
        String text = "I've lost two minutes.";

        Unit unit = new Unit("minutes", 14, 21);
        Quantity quantity = new Quantity("two", unit, 10, 13);
        Measurement measurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurement.setAtomicQuantity(quantity);

        String tei = TeiUtils.toTei(Collections.singletonList(measurement), text);

        assertThat(tei, containsString("<p>I've lost <measure"));
        assertThat(tei, containsString("</measure>.</p>"));
    }

    @Test
    public void testToTei_blankText_shouldReturnEmptyTeiDocument() {
        String preprocessed = QuantityParser.preprocess("");
        List<Measurement> measurements = null;

        String tei = TeiUtils.toTei(measurements, preprocessed);

        assertThat(tei, containsString("<teiHeader>"));
        assertThat(tei, containsString("<text xml:lang=\"en\"><p"));
    }

    @Test
    public void testToTei_nullText_shouldReturnEmptyTeiDocument() {
        String preprocessed = QuantityParser.preprocess(null);

        assertThat(preprocessed, is(""));

        String tei = TeiUtils.toTei(null, preprocessed);

        assertThat(tei, containsString("<teiHeader>"));
        assertThat(tei, containsString("<text xml:lang=\"en\"><p"));
    }

    /**
     * What makes the TEI output worth having is that it is the *same* notation as the annotated
     * corpus, so the service output can go straight back in as training data. Rather than assert
     * that in prose, feed it to the trainer's own SAX handler and check it comes out labelled.
     */
    @Test
    public void testToTei_shouldBeReadableByTheTrainingSaxHandler() throws Exception {
        String text = "I've lost two minutes.";

        Unit unit = new Unit("minutes", 14, 21);
        unit.setUnitDefinition(new UnitDefinition(UnitUtilities.Unit_Type.TIME, UnitUtilities.System_Type.NON_SI));
        Quantity quantity = new Quantity("two", unit, 10, 13);
        Measurement measurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurement.setAtomicQuantity(quantity);

        String tei = TeiUtils.toTei(Collections.singletonList(measurement), text);

        List<String> labels = labelsOf(tei);

        assertThat(labels, hasItem("two/I-<valueAtomic>"));
        assertThat(labels, hasItem("minutes/I-<unitLeft>"));
        assertThat(labels, hasItem("lost/<other>"));
    }

    @Test
    public void testToTei_baseRangeInterval_shouldBeReadableByTheTrainingSaxHandler() throws Exception {
        String text = "K A = 5.54 ± 0.25 km";

        Quantity base = new Quantity("5.54", null, 6, 10);
        Unit unit = new Unit("km", 18, 20);
        Quantity range = new Quantity("0.25", unit, 13, 17);

        Measurement measurement = new Measurement(UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE);
        measurement.setQuantityBase(base);
        measurement.setQuantityRange(range);

        String tei = TeiUtils.toTei(Collections.singletonList(measurement), text);

        assertThat(tei, containsString("<num type=\"base\">5.54</num>"));
        assertThat(tei, containsString("<num type=\"range\">0.25</num>"));

        List<String> labels = labelsOf(tei);

        assertThat(labels, hasItem("5/I-<valueBase>"));
        // note the missing I- on the two below: the round trip is faithful, the handler that
        // reads it back is the one dropping the entity starts. Fixed separately, see issue #177
        assertThat(labels, hasItem("0/<valueRange>"));
        assertThat(labels, hasItem("km/<unitLeft>"));
    }

    private List<String> labelsOf(String tei) throws Exception {
        MeasureAnnotationSaxHandler handler = new MeasureAnnotationSaxHandler();
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        try (InputStream is = org.apache.commons.io.IOUtils.toInputStream(tei, UTF_8)) {
            parser.parse(is, handler);
        }

        List<String> labels = new ArrayList<>();
        for (org.grobid.core.utilities.Pair<String, String> pair : handler.getLabeledResult()) {
            if (pair.getB() != null) {
                labels.add(pair.getA() + "/" + pair.getB());
            }
        }
        return labels;
    }
}
