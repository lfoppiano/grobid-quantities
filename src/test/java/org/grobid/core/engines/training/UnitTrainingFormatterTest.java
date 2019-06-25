package org.grobid.core.engines.training;

import nu.xom.Element;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.UnitBlock;
import org.grobid.core.engines.training.UnitTrainingFormatter;
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
        assertThat(elements.get(0).toXML(), is("<unit xmlns=\"http://www.tei-c.org/ns/1.0\"><base>grams</base></unit>"));
        assertThat(elements.get(1).toXML(), is("<unit xmlns=\"http://www.tei-c.org/ns/1.0\"><base>gr / h</base></unit>"));
    }

    @Test
    public void trainingExtraction_parsedUnit() {
        String text = "We need drive at 10 km/ s or we will get a fine!";

        List<Measurement> measurementList = new ArrayList<>();

        Measurement measurement1 = new Measurement();
        measurement1.setType(UnitUtilities.Measurement_Type.VALUE);
        final Unit unit = new Unit("km/ s");
        unit.setOffsetStart(20);
        unit.setOffsetEnd(25);

        final Quantity quantity = new Quantity("10", unit);
        quantity.setOffsetStart(17);
        quantity.setOffsetEnd(19);
        measurement1.setAtomicQuantity(quantity);

        Unit parsedUnit = new Unit("km/ s");
        parsedUnit.setOffsetStart(20);
        parsedUnit.setOffsetEnd(25);
        List<UnitBlock> productForm = new ArrayList<>();
        UnitBlock unitBlock1 = new UnitBlock("k", "m", null);
        unitBlock1.setRawTaggedValue("<prefix>k</prefix><base>m</base><pow>/</pow> <base>s</base>");
        UnitBlock unitBlock2 = new UnitBlock(null, "s", "-1");
        unitBlock2.setRawTaggedValue("<prefix>k</prefix><base>m</base><pow>/</pow> <base>s</base>");

        productForm.add(unitBlock1);
        productForm.add(unitBlock2);
        parsedUnit.setProductBlocks(productForm);
        quantity.setParsedUnit(parsedUnit);

        measurementList.add(measurement1);

        final List<Element> elements = target.trainingExtraction(measurementList);

        assertThat(elements, hasSize(1));
        assertThat(elements.get(0).toXML(), is("<unit xmlns=\"http://www.tei-c.org/ns/1.0\"><prefix>k</prefix><base>m</base><pow>/</pow> <base>s</base></unit>"));
    }
}