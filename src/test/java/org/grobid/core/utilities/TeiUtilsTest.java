package org.grobid.core.utilities;

import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.UnitDefinition;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

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
}
