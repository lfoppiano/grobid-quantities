package org.grobid.core.engines;

import nu.xom.Element;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.utilities.UnitUtilities;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class UnitTrainingFormatterTest {

    UnitTrainingFormatter target;

    @Before
    public void setUp() throws Exception {
        target = new UnitTrainingFormatter();
    }

    @Test
    public void trainingExtraction() {
        String text = "We need 10 grams of potatoes and 1x 10 -5 gr / h of oxygen.";

        List<Measurement> measurementList = new ArrayList<>();

        Measurement measurement1 = new Measurement();
        measurement1.setType(UnitUtilities.Measurement_Type.VALUE);
        final Unit unit = new Unit("grams");
        unit.setOffsetStart(11);
        unit.setOffsetEnd(16);

        final Quantity quantity = new Quantity("10", unit);
        quantity.setOffsetStart(8);
        quantity.setOffsetEnd(10);
        measurement1.setAtomicQuantity(quantity);

        measurementList.add(measurement1);

        Measurement measurement2 = new Measurement();
        measurement2.setType(UnitUtilities.Measurement_Type.VALUE);
        final Unit unit2 = new Unit("gr / h");
        unit2.setOffsetStart(45);
        unit2.setOffsetEnd(51);

        final Quantity quantity2 = new Quantity("1x 10 -5", unit2);
        quantity2.setOffsetStart(35);
        quantity2.setOffsetEnd(43);
        measurement2.setAtomicQuantity(quantity2);

        measurementList.add(measurement2);

        final List<Element> elements = target.trainingExtraction(measurementList);

        assertThat(elements, hasSize(2));
        assertThat(elements.get(0).toXML(), is("<unit xmlns=\"http://www.tei-c.org/ns/1.0\">grams</unit>"));
        assertThat(elements.get(1).toXML(), is("<unit xmlns=\"http://www.tei-c.org/ns/1.0\">gr / h</unit>"));
    }
}