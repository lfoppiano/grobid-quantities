package org.grobid.core.utilities;

import org.grobid.core.data.*;
import org.grobid.core.engines.QuantityParser;
import org.grobid.core.layout.BoundingBox;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class TeiUtilsTest {

    @Test
    public void testHeader_identShouldBeGrobidQuantities() {
        String tei = TeiUtils.toTei(Collections.emptyList(), "test");

        assertThat(tei, containsString("ident=\"grobid-quantities\""));
        assertThat(tei, containsString("target=\"https://github.com/kermitt2/grobid-quantities\""));
        assertThat(tei, not(containsString("ident=\"GROBID\"")));
    }

    @Test
    public void testToTei_atomicValue_shouldAnnotateInlineAndStandoff() {
        String text = "I've lost two minutes.";

        Unit unit = new Unit("minutes", 14, 21);
        unit.setUnitDefinition(new UnitDefinition(UnitUtilities.Unit_Type.TIME, UnitUtilities.System_Type.NON_SI));
        Quantity quantity = new Quantity("two", unit, 10, 13);
        quantity.setParsedValue(new Value(new BigDecimal("2")));

        Measurement measurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurement.setAtomicQuantity(quantity);
        measurement.setRawOffsets(new OffsetPosition(10, 21));
        measurement.setRawString("two minutes");

        String tei = TeiUtils.toTei(Collections.singletonList(measurement), text);

        // Inline verification
        assertThat(tei, containsString("<measure xml:id=\"m0\" type=\"value\" corresp=\"#ann-m0\">"));
        assertThat(tei, containsString("<num xml:id=\"m0-num\" value=\"2\">two</num>"));
        assertThat(tei, containsString("<unit xml:id=\"m0-unit\" type=\"time\">minutes</unit>"));
        assertThat(tei, containsString("<p>I've lost <measure"));
        assertThat(tei, containsString("</measure>.</p>"));

        // Standoff verification
        assertThat(tei, containsString("<standOff>"));
        assertThat(tei, containsString("<listAnnotation type=\"measurements\">"));
        assertThat(tei, containsString("<annotation xml:id=\"ann-m0\" corresp=\"#m0\">"));
        assertThat(tei, containsString("<fs type=\"measurement\">"));
        assertThat(tei, containsString("<f name=\"type\"><symbol value=\"value\" /></f>"));
        assertThat(tei, containsString("<f name=\"measurementRaw\"><string>two minutes</string></f>"));
        assertThat(tei, containsString("<fs type=\"offsets\">"));
        assertThat(tei, containsString("<f name=\"start\"><numeric value=\"10\" /></f>"));
        assertThat(tei, containsString("<f name=\"end\"><numeric value=\"21\" /></f>"));
        assertThat(tei, containsString("<fs type=\"quantity\">"));
        assertThat(tei, containsString("<f name=\"rawValue\"><string>two</string></f>"));
        assertThat(tei, containsString("<fs type=\"unit\">"));
        assertThat(tei, containsString("<f name=\"name\"><string>minutes</string></f>"));
        assertThat(tei, containsString("<f name=\"system\"><symbol value=\"non SI\" /></f>"));
    }

    @Test
    public void testToTei_intervalMinMax_shouldAnnotateBothExtremitiesAndStandoff() {
        String text = "between 3 and 5 kg of it";

        Quantity least = new Quantity("3", null, 8, 9);
        Unit unit = new Unit("kg", 16, 18);
        unit.setUnitDefinition(new UnitDefinition(UnitUtilities.Unit_Type.MASS, UnitUtilities.System_Type.SI_BASE));
        Quantity most = new Quantity("5", unit, 14, 15);

        Measurement measurement = new Measurement(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        measurement.setQuantityLeast(least);
        measurement.setQuantityMost(most);
        measurement.setRawOffsets(new OffsetPosition(8, 18));
        measurement.setRawString("3 and 5 kg");

        String tei = TeiUtils.toTei(Collections.singletonList(measurement), text);

        // Inline checks
        assertThat(tei, containsString("<measure xml:id=\"m0\" type=\"interval\" corresp=\"#ann-m0\">"));
        assertThat(tei, containsString("<num xml:id=\"m0-num-least\" atLeast=\"3\">3</num>"));
        assertThat(tei, containsString("<num xml:id=\"m0-num-most\" atMost=\"5\">5</num>"));
        assertThat(tei, containsString("<unit xml:id=\"m0-unit\" type=\"mass\">kg</unit>"));

        // Standoff checks
        assertThat(tei, containsString("<f name=\"quantityLeast\">"));
        assertThat(tei, containsString("<f name=\"quantityMost\">"));
        assertThat(tei, containsString("<f name=\"name\"><string>kg</string></f>"));
    }

    @Test
    public void testToTei_intervalBaseRange_shouldAnnotateInlineAndStandoff() {
        String text = "K A = 5.54 ± 0.25 km";

        Quantity base = new Quantity("5.54", null, 6, 10);
        base.setParsedValue(new Value(new BigDecimal("5.54")));
        Unit unit = new Unit("km", 18, 20);
        unit.setUnitDefinition(new UnitDefinition(UnitUtilities.Unit_Type.LENGTH, UnitUtilities.System_Type.SI_DERIVED));
        Quantity range = new Quantity("0.25", unit, 13, 17);
        range.setParsedValue(new Value(new BigDecimal("0.25")));

        Measurement measurement = new Measurement(UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE);
        measurement.setQuantityBase(base);
        measurement.setQuantityRange(range);
        measurement.setRawOffsets(new OffsetPosition(6, 20));
        measurement.setRawString("5.54 ± 0.25 km");

        String tei = TeiUtils.toTei(Collections.singletonList(measurement), text);

        assertThat(tei, containsString("<num xml:id=\"m0-num-base\" type=\"base\" value=\"5.54\">5.54</num>"));
        assertThat(tei, containsString("<num xml:id=\"m0-num-range\" type=\"range\" value=\"0.25\">0.25</num>"));
        assertThat(tei, containsString("<unit xml:id=\"m0-unit\" type=\"length\">km</unit>"));
        assertThat(tei, containsString("<f name=\"quantityBase\">"));
        assertThat(tei, containsString("<f name=\"quantityRange\">"));
    }

    @Test
    public void testToTei_withNormalizedQuantity_shouldIncludeNormalizedFeaturesInStandoff() {
        String text = "The weight is 5 kg.";

        Unit unit = new Unit("kg", 16, 18);
        unit.setUnitDefinition(new UnitDefinition(UnitUtilities.Unit_Type.MASS, UnitUtilities.System_Type.SI_BASE));
        Quantity quantity = new Quantity("5", unit, 14, 15);

        Quantity.Normalized norm = quantity.new Normalized();
        norm.setValue(new BigDecimal("5000"));
        Unit normUnit = new Unit("g");
        normUnit.setUnitDefinition(new UnitDefinition(UnitUtilities.Unit_Type.MASS, UnitUtilities.System_Type.SI_BASE));
        norm.setUnit(normUnit);
        quantity.setNormalizedQuantity(norm);

        Measurement measurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurement.setAtomicQuantity(quantity);
        measurement.setRawOffsets(new OffsetPosition(14, 18));

        String tei = TeiUtils.toTei(Collections.singletonList(measurement), text);

        assertThat(tei, containsString("<f name=\"normalizedQuantity\"><numeric value=\"5000\" /></f>"));
        assertThat(tei, containsString("<f name=\"normalizedUnit\">"));
        assertThat(tei, containsString("<f name=\"name\"><string>g</string></f>"));
    }

    @Test
    public void testToTei_withQuantifiedObject_shouldIncludeCommodityAndQuantifiedFs() {
        String text = "The dosage is 500 mg aspirin daily.";

        Unit unit = new Unit("mg", 18, 20);
        Quantity quantity = new Quantity("500", unit, 14, 17);

        Measurement measurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurement.setAtomicQuantity(quantity);
        measurement.setRawOffsets(new OffsetPosition(14, 20));

        QuantifiedObject qo = new QuantifiedObject("aspirin", "acetylsalicylic acid", 21, 28);
        measurement.setQuantifiedObject(qo);

        String tei = TeiUtils.toTei(Collections.singletonList(measurement), text);

        assertThat(tei, containsString("commodity=\"aspirin\""));
        assertThat(tei, containsString("<fs type=\"quantified\">"));
        assertThat(tei, containsString("<f name=\"rawName\"><string>aspirin</string></f>"));
        assertThat(tei, containsString("<f name=\"normalizedName\"><string>acetylsalicylic acid</string></f>"));
    }

    @Test
    public void testToTei_withBoundingBoxes_shouldIncludeBoundingBoxesFs() {
        String text = "10 m";
        Unit unit = new Unit("m", 3, 4);
        Quantity quantity = new Quantity("10", unit, 0, 2);

        Measurement measurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurement.setAtomicQuantity(quantity);
        measurement.setRawOffsets(new OffsetPosition(0, 4));

        BoundingBox box = BoundingBox.fromPointAndDimensions(1, 10.0, 20.0, 30.0, 40.0);
        measurement.setBoundingBoxes(Collections.singletonList(box));

        String tei = TeiUtils.toTei(Collections.singletonList(measurement), text);

        assertThat(tei, containsString("<f name=\"boundingBoxes\">"));
        assertThat(tei, containsString("<fs type=\"boundingBox\">"));
        assertThat(tei, containsString("<f name=\"page\"><numeric value=\"1\" /></f>"));
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
        measurement.setRawOffsets(new OffsetPosition(10, 21));

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
        assertThat(tei, containsString("<text xml:lang=\"en\">"));
        assertThat(tei, containsString("<p"));
    }

    @Test
    public void testToTei_nullText_shouldReturnEmptyTeiDocument() {
        String preprocessed = QuantityParser.preprocess(null);

        String tei = TeiUtils.toTei((List<Measurement>) null, preprocessed);

        assertThat(tei, containsString("<teiHeader>"));
        assertThat(tei, containsString("<text xml:lang=\"en\">"));
        assertThat(tei, containsString("<p"));
    }
}

