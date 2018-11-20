package org.grobid.core.engines.training;

import nu.xom.Element;
import org.grobid.core.data.*;
import org.grobid.core.engines.training.ValueTrainingFormatter;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.UnitUtilities;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.grobid.core.document.xml.XmlBuilderUtils.teiElement;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class ValueTrainingFormatterTest {

    ValueTrainingFormatter target;

    @Before
    public void setUp() throws Exception {
        target = new ValueTrainingFormatter();
    }

    @Test
    public void trainingExtraction_noParsing_shouldLeaveOriginalValue() {
        String text = "We need 10 grams of potatoes and 1x 10 -5 gr / h of oxygen corresponding to ~20.";

        List<Measurement> measurementList = new ArrayList<>();

        Measurement measurement1 = new Measurement();
        measurement1.setType(UnitUtilities.Measurement_Type.VALUE);
        final Unit unit = new Unit("grams");
        unit.setOffsetStart(11);
        unit.setOffsetEnd(16);

        final Quantity quantity = new Quantity("10", unit);
        quantity.setOffsetStart(8);
        quantity.setOffsetEnd(10);

        final Value parsedValue = new Value();
        parsedValue.setRawValue(quantity.getRawValue());
        parsedValue.setOffsetStart(0);
        parsedValue.setOffsetEnd(2);
        quantity.setParsedValue(parsedValue);
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

        Measurement measurement3 = new Measurement();
        measurement3.setType(UnitUtilities.Measurement_Type.VALUE);

        final Quantity quantity3 = new Quantity("~20", null);
        quantity3.setOffsetStart(35);
        quantity3.setOffsetEnd(43);
        measurement3.setAtomicQuantity(quantity3);

        measurementList.add(measurement3);

        final List<Element> elements = target.trainingExtraction(measurementList);

        assertThat(elements, hasSize(3));
        assertThat(elements.get(0).toXML(), is("<value xmlns=\"http://www.tei-c.org/ns/1.0\"><number>10</number></value>"));
        assertThat(elements.get(1).toXML(), is("<value xmlns=\"http://www.tei-c.org/ns/1.0\">1x 10 -5</value>"));
        assertThat(elements.get(2).toXML(), is("<value xmlns=\"http://www.tei-c.org/ns/1.0\">~20</value>"));
    }

    @Test
    public void trainingExtraction_parsing() {
        String text = "We need 1x 10 -5 gr / h of oxygen.";

        List<Measurement> measurementList = new ArrayList<>();

        Measurement measurement1 = new Measurement();
        measurement1.setType(UnitUtilities.Measurement_Type.VALUE);
        final Unit unit = new Unit("gr / h");
        unit.setOffsetStart(17);
        unit.setOffsetEnd(22);

        final Quantity quantity = new Quantity("1x 10 -5", unit);
        quantity.setOffsetStart(7);
        quantity.setOffsetEnd(15);

        final Value parsedValue = new Value();
        parsedValue.setRawValue(quantity.getRawValue());
        parsedValue.setOffsetStart(0);
        parsedValue.setOffsetEnd(8);
        final ValueBlock structure = new ValueBlock(
                new Block("1", new OffsetPosition(0, 1)),
                new Block("10", new OffsetPosition(3, 5)),
                new Block("-5", new OffsetPosition(6, 8))
        );
        structure.setRawTaggedValue("<number>1</number>x<base>10</base> <pow>-5</pow>");
        parsedValue.setStructure(structure);
        quantity.setParsedValue(parsedValue);
        measurement1.setAtomicQuantity(quantity);

        measurementList.add(measurement1);

        final List<Element> elements = target.trainingExtraction(measurementList);

        assertThat(elements, hasSize(1));
        teiElement("bao").appendChild(elements.get(0));
        assertThat(elements.get(0).toXML(), is("<value><number>1</number>x<base>10</base> <pow>-5</pow></value>"));
    }
}