package org.grobid.core.engines;

import nu.xom.Element;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.UnitUtilities;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class QuantityParserTest {
    @BeforeClass
    public static void init() {
        LibraryLoader.load();
    }

    QuantityParser target;
    @Before
    public void setUp() throws Exception {
        target = QuantityParser.getInstance();
    }

    @Test
    public void testTrainingData() throws Exception {
        List<Measurement> measurementList = new ArrayList<>();
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.VALUE);
        final Unit unit = new Unit("grams");
        unit.setOffsetStart(11);
        unit.setOffsetEnd(16);

        final Quantity quantity = new Quantity("10", unit);
        quantity.setOffsetStart(8);
        quantity.setOffsetEnd(10);
        measurement.setAtomicQuantity(quantity);

        String text = "We need 10 grams of potatoes";

        measurementList.add(measurement);

        Element out = target.trainingExtraction(measurementList, text);
        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need <measure type=\"value\"><num>10</num> <measure type=\"?\" unit=\"grams\">grams</measure></measure> of potatoes</p>"));
    }

}