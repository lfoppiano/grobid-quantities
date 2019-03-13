package org.grobid.core.engines.training;

import nu.xom.Element;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.UnitDefinition;
import org.grobid.core.engines.QuantityParser;
import org.grobid.core.engines.training.QuantityTrainingFormatter;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.UnitUtilities;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.grobid.core.utilities.UnitUtilities.Unit_Type.*;
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
    public void testTrainingData_intervals_onlyLeast() throws Exception {
        String text = "We need max  12 grams of potatoes";

        List<Measurement> measurementList = new ArrayList<>();
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        final Unit unit = new Unit("grams");
        unit.setOffsetStart(16);
        unit.setOffsetEnd(21);

        final Quantity quantityRange = new Quantity("12", unit);
        quantityRange.setOffsetStart(13);
        quantityRange.setOffsetEnd(15);
        measurement.setQuantityMost(quantityRange);

        measurementList.add(measurement);

        Element out = target.trainingExtraction(measurementList, text);
        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need max  <measure type=\"interval\"><num atMost=\"12\">12</num> <measure type=\"?\" unit=\"grams\">grams</measure></measure> of potatoes</p>"));
    }

    @Test
    public void testTrainingData_intervals_onlyMost() throws Exception {
        String text = "We need min  12 grams of potatoes";

        List<Measurement> measurementList = new ArrayList<>();
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        measurement.setQuantityLeast(new Quantity("12", new Unit("grams", 16, 21), 13, 15));

        measurementList.add(measurement);

        Element out = target.trainingExtraction(measurementList, text);
        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need min  <measure type=\"interval\"><num atLeast=\"12\">12</num> <measure type=\"?\" unit=\"grams\">grams</measure></measure> of potatoes</p>"));
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


    @Test
    public void testConsistency() throws Exception {
        String text = "The H-overdoped samples of LaFeAsO 1−x H x (x =0.53, 0.58 and 0.625) were prepared for the present measurements. The H doping level was analyzed by thermal desorption spectroscopy. The doping level observed by thermal desorption spectroscopy was almost the same with the nominal one for x ≤ 0.30, whereas the deviation between them (∆x) became large with increasing x. For x ≥ 0.50, ∆x reached ∼ 0.1. The powder samples were analyzed by x-ray diffraction. No crystallographical anomalies were observed for each doping level and the lattice constants changed monotonously with increasing x. The SC volume fractions of the 53%, 58% and 62.5% doped samples were less than 12%, 3%, and 16%, respectively.";

        List<Measurement> measurementList = new ArrayList<>();

        Measurement measurementValue1 = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurementValue1.setAtomicQuantity(new Quantity("0.53", null, 47, 51));
        measurementList.add(measurementValue1);

        Measurement measurementList2 = new Measurement(UnitUtilities.Measurement_Type.CONJUNCTION);
        measurementList2.setQuantityList(Arrays.asList(new Quantity("0.58", null, 53, 57)));
        measurementList.add(measurementList2);

        Measurement measurementValue3 = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurementValue3.setAtomicQuantity(new Quantity("0.625", null, 62, 67));
        measurementList.add(measurementValue3);

        Measurement measurementValue4 = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurementValue4.setAtomicQuantity(new Quantity("0.30", null, 291, 295));
        measurementList.add(measurementValue4);

        Measurement measurementInterval5 = new Measurement(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        measurementInterval5.setQuantityLeast(new Quantity("0.50", null, 377, 381));
        measurementList.add(measurementInterval5);

        Measurement measurementValue6 = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurementValue6.setAtomicQuantity(new Quantity("0.1", null, 396, 399));
        measurementList.add(measurementValue6);

        Measurement measurementValue7 = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurementValue7.setAtomicQuantity(new Quantity("53", new Unit("%", 623, 624, new UnitDefinition(FRACTION, null)), 621, 623));
        measurementList.add(measurementValue7);

        Measurement measurementList8 = new Measurement(UnitUtilities.Measurement_Type.CONJUNCTION);
        measurementList8.setQuantityList(Arrays.asList(
                new Quantity("58", new Unit("%", 628, 629, new UnitDefinition(FRACTION, null)), 626, 628),
                new Quantity("62.5", null, 634, 638))
        );
        measurementList.add(measurementList8);

        Measurement measurementInterval9 = new Measurement(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        measurementInterval9.setQuantityMost(new Quantity("12", new Unit("%", 671, 672, new UnitDefinition(FRACTION, null)), 669, 671));
        measurementList.add(measurementInterval9);

        Measurement measurementList10 = new Measurement(UnitUtilities.Measurement_Type.CONJUNCTION);
        measurementList10.setQuantityList(Arrays.asList(
                new Quantity("3", new Unit("%", 675, 676, new UnitDefinition(FRACTION, null)), 674, 675),
                new Quantity("16", null, 682, 684))
        );
        measurementList.add(measurementList10);

        Element out = target.trainingExtraction(measurementList, text);

        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">The H-overdoped samples of LaFeAsO 1−x H x (x =<measure type=\"value\"><num>0.53</num></measure>, <measure type=\"list\"><num>0.58</num></measure> and <measure type=\"value\"><num>0.625</num></measure>) were prepared for the present measurements. The H doping level was analyzed by thermal desorption spectroscopy. The doping level observed by thermal desorption spectroscopy was almost the same with the nominal one for x ≤ <measure type=\"value\"><num>0.30</num></measure>, whereas the deviation between them (∆x) became large with increasing x. For x ≥ <measure type=\"interval\"><num atLeast=\"0.50\">0.50</num></measure>, ∆x reached ∼ <measure type=\"value\"><num>0.1</num></measure>. The powder samples were analyzed by x-ray diffraction. No crystallographical anomalies were observed for each doping level and the lattice constants changed monotonously with increasing x. The SC volume fractions of the <measure type=\"value\"><num>53</num><measure type=\"FRACTION\" unit=\"%\">%</measure></measure>, <measure type=\"list\"><num>58</num><measure type=\"FRACTION\" unit=\"%\">%</measure> and <num>62.5</num></measure>% doped samples were less than <measure type=\"interval\"><num atMost=\"12\">12</num><measure type=\"FRACTION\" unit=\"%\">%</measure></measure>, <measure type=\"list\"><num>3</num><measure type=\"FRACTION\" unit=\"%\">%</measure>, and <num>16</num></measure>%, respectively.</p>"));

    }
}