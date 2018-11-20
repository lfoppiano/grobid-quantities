package org.grobid.core.engines.training;

import nu.xom.Element;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.engines.training.QuantityTrainingFormatter;
import org.grobid.core.utilities.UnitUtilities;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class QuantityTrainingFormatterTest {

    QuantityTrainingFormatter target;

    @Before
    public void setUp() throws Exception {
        target = new QuantityTrainingFormatter();
    }

    @Test
    public void testTrainingData_value() throws Exception {
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

    @Test
    public void testTrainingData_range() throws Exception {
        String text = "We need 10 +- 2 grams of potatoes";

        List<Measurement> measurementList = new ArrayList<>();
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE);
        final Unit unit = new Unit("grams");
        unit.setOffsetStart(16);
        unit.setOffsetEnd(21);

        final Quantity quantityBase = new Quantity("10", unit);
        quantityBase.setOffsetStart(8);
        quantityBase.setOffsetEnd(10);
        measurement.setQuantityBase(quantityBase);

        final Quantity quantityRange = new Quantity("2", unit);
        quantityRange.setOffsetStart(14);
        quantityRange.setOffsetEnd(15);
        measurement.setQuantityRange(quantityRange);

        measurementList.add(measurement);

        Element out = target.trainingExtraction(measurementList, text);
        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need <measure type=\"interval\"><num type=\"base\">10</num> +- <num type=\"range\">2</num> <measure type=\"?\" unit=\"grams\">grams</measure></measure> of potatoes</p>"));
    }

    @Test
    public void testTrainingData_intervals() throws Exception {
        String text = "We need 1 to 12 grams of potatoes";

        List<Measurement> measurementList = new ArrayList<>();
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        final Unit unit = new Unit("grams");
        unit.setOffsetStart(16);
        unit.setOffsetEnd(21);

        final Quantity quantityBase = new Quantity("1", unit);
        quantityBase.setOffsetStart(8);
        quantityBase.setOffsetEnd(9);
        measurement.setQuantityLeast(quantityBase);

        final Quantity quantityRange = new Quantity("12", unit);
        quantityRange.setOffsetStart(13);
        quantityRange.setOffsetEnd(15);
        measurement.setQuantityMost(quantityRange);

        measurementList.add(measurement);

        Element out = target.trainingExtraction(measurementList, text);
        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need <measure type=\"interval\"><num atLeast=\"1\">1</num> to <num atMost=\"12\">12</num> <measure type=\"?\" unit=\"grams\">grams</measure></measure> of potatoes</p>"));
    }

    @Test
    public void testTrainingData_list() throws Exception {
        String text = "We need 1, 5 and 12 grams of potatoes";

        List<Measurement> measurementList = new ArrayList<>();
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.CONJUNCTION);
        final Unit unit = new Unit("grams");
        unit.setOffsetStart(20);
        unit.setOffsetEnd(25);

        final Quantity quantity1 = new Quantity("1", unit);
        quantity1.setOffsetStart(8);
        quantity1.setOffsetEnd(9);

        final Quantity quantity2 = new Quantity("5", unit);
        quantity2.setOffsetStart(11);
        quantity2.setOffsetEnd(12);

        final Quantity quantity3 = new Quantity("12", unit);
        quantity3.setOffsetStart(17);
        quantity3.setOffsetEnd(19);

        List<Quantity> quantityList = new ArrayList<>();
        quantityList.add(quantity1);
        quantityList.add(quantity2);
        quantityList.add(quantity3);
        
        measurement.setQuantityList(quantityList);

        measurementList.add(measurement);

        Element out = target.trainingExtraction(measurementList, text);
        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need <measure type=\"list\"><num>1</num>, <num>5</num> and <num>12</num> <measure type=\"?\" unit=\"grams\">grams</measure></measure> of potatoes</p>"));
    }

}