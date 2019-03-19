package org.grobid.core.engines.training;

import nu.xom.Element;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.QuantifiedObject;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.UnitUtilities;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.powermock.api.easymock.PowerMock.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(QuantifiedObjectTrainingFormatter.class)
public class QuantifiedObjectTrainingFormatterTest {
    QuantifiedObjectTrainingFormatter target;

    @Before
    public void setUp() throws Exception {
        target = new QuantifiedObjectTrainingFormatter();
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

        final QuantifiedObject potatoes = new QuantifiedObject("potatoes");
        potatoes.setOffsetStart(20);
        potatoes.setOffsetEnd(28);
        measurement.setQuantifiedObject(potatoes);

        UUID expectedID = UUID.randomUUID();
        mockStatic(UUID.class);
        expect(UUID.randomUUID()).andReturn(expectedID);
        replay(UUID.class);

        String text = "We need 10 grams of potatoes";

        measurementList.add(measurement);

        Element out = target.trainingExtraction(measurementList, text);

        verify(UUID.class);

        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need <measure type=\"value\" ptr=\"#" + expectedID.toString() + "\">10 grams</measure> of <quantifiedObject id=\"" + expectedID.toString() + "\">potatoes</quantifiedObject></p>"));
    }

    @Test
    public void testTrainingData_intervals() throws Exception {
        String text = "We need 10 to 2 grams of potatoes";

        List<Measurement> measurementList = new ArrayList<>();
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        final Unit unit = new Unit("grams");
        unit.setOffsetStart(16);
        unit.setOffsetEnd(21);

        final Quantity quantityBase = new Quantity("10", unit, 8, 10);
        measurement.setQuantityLeast(quantityBase);

        final Quantity quantityRange = new Quantity("2", unit, 14, 15);
        measurement.setQuantityMost(quantityRange);

        final QuantifiedObject potatoes = new QuantifiedObject("potatoes");
        potatoes.setOffsetStart(25);
        potatoes.setOffsetEnd(33);
        measurement.setQuantifiedObject(potatoes);

        measurementList.add(measurement);

        UUID expectedID = UUID.randomUUID();
        mockStatic(UUID.class);
        expect(UUID.randomUUID()).andReturn(expectedID);
        replay(UUID.class);

        Element out = target.trainingExtraction(measurementList, text);

        verify(UUID.class);
        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need <measure type=\"interval\" ptr=\"#" + expectedID + "\">10 to 2 grams</measure> of <quantifiedObject id=\"" + expectedID + "\">potatoes</quantifiedObject></p>"));
    }

    @Test
    public void testTrainingData_intervals_onlyAtMost() throws Exception {
        String text = "We need < 2 grams of potatoes";

        List<Measurement> measurementList = new ArrayList<>();
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        final Unit unit = new Unit("grams");
        unit.setOffsetStart(12);
        unit.setOffsetEnd(17);

        final Quantity quantityMost = new Quantity("2", unit);
        quantityMost.setOffsetStart(8);
        quantityMost.setOffsetEnd(11);
        measurement.setQuantityMost(quantityMost);

        final QuantifiedObject potatoes = new QuantifiedObject("potatoes");
        potatoes.setOffsetStart(21);
        potatoes.setOffsetEnd(29);
        measurement.setQuantifiedObject(potatoes);

        measurementList.add(measurement);

        UUID expectedID = UUID.randomUUID();
        mockStatic(UUID.class);
        expect(UUID.randomUUID()).andReturn(expectedID);
        replay(UUID.class);

        Element out = target.trainingExtraction(measurementList, text);

        verify(UUID.class);

        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need <measure type=\"interval\" ptr=\"#" + expectedID + "\">&lt; 2 grams</measure> of <quantifiedObject id=\"" + expectedID + "\">potatoes</quantifiedObject></p>"));
    }

    @Test
    public void testTrainingData_intervals_onlyAtLeast() throws Exception {
        String text = "We need > 2 grams of potatoes";

        List<Measurement> measurementList = new ArrayList<>();
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        final Unit unit = new Unit("grams");
        unit.setOffsetStart(12);
        unit.setOffsetEnd(17);

        final Quantity quantityLeast = new Quantity("2", unit);
        quantityLeast.setOffsetStart(8);
        quantityLeast.setOffsetEnd(11);
        measurement.setQuantityLeast(quantityLeast);

        final QuantifiedObject potatoes = new QuantifiedObject("potatoes");
        potatoes.setOffsetStart(21);
        potatoes.setOffsetEnd(29);
        measurement.setQuantifiedObject(potatoes);

        measurementList.add(measurement);

        UUID expectedID = UUID.randomUUID();
        mockStatic(UUID.class);
        expect(UUID.randomUUID()).andReturn(expectedID);
        replay(UUID.class);

        Element out = target.trainingExtraction(measurementList, text);

        verify(UUID.class);
        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need <measure type=\"interval\" ptr=\"#" + expectedID + "\">&gt; 2 grams</measure> of <quantifiedObject id=\"" + expectedID + "\">potatoes</quantifiedObject></p>"));
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

        final QuantifiedObject potatoes = new QuantifiedObject("potatoes");
        potatoes.setOffsetStart(25);
        potatoes.setOffsetEnd(33);
        measurement.setQuantifiedObject(potatoes);

        measurementList.add(measurement);

        UUID expectedID = UUID.randomUUID();
        mockStatic(UUID.class);
        expect(UUID.randomUUID()).andReturn(expectedID);
        replay(UUID.class);

        Element out = target.trainingExtraction(measurementList, text);

        verify(UUID.class);
        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need <measure type=\"interval\">10 +- 2 grams</measure> of <quantifiedObject id=\"" + expectedID + "\">potatoes</quantifiedObject></p>"));
    }

    @Test
    public void testTrainingData_range_onlyRange() throws Exception {
        String text = "We need +- 2 grams of potatoes";

        List<Measurement> measurementList = new ArrayList<>();
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE);
        final Unit unit = new Unit("grams");
        unit.setOffsetStart(13);
        unit.setOffsetEnd(18);

        final Quantity quantityRange = new Quantity("2", unit);
        quantityRange.setOffsetStart(8);
        quantityRange.setOffsetEnd(12);
        measurement.setQuantityRange(quantityRange);

        final QuantifiedObject potatoes = new QuantifiedObject("potatoes");
        potatoes.setOffsetStart(22);
        potatoes.setOffsetEnd(30);
        measurement.setQuantifiedObject(potatoes);

        measurementList.add(measurement);

        UUID expectedID = UUID.randomUUID();
        mockStatic(UUID.class);
        expect(UUID.randomUUID()).andReturn(expectedID);
        replay(UUID.class);

        Element out = target.trainingExtraction(measurementList, text);

        verify(UUID.class);
        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need <measure type=\"interval\" ptr=\"#" + expectedID + "\">+- 2 grams</measure> of <quantifiedObject id=\"" + expectedID + "\">potatoes</quantifiedObject></p>"));
    }

    @Test
    public void testTrainingData_list() throws Exception {
        String text = "We need 1, 5 and 12 grams of potatoes";

        List<Measurement> measurementList = new ArrayList<>();
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.CONJUNCTION);
        final Unit unit = new Unit("grams", 20, 25);

        final Quantity quantity1 = new Quantity("1", unit, 8, 9);
        final Quantity quantity2 = new Quantity("5", unit, 11, 12);
        final Quantity quantity3 = new Quantity("12", unit, 17, 19);

        List<Quantity> quantityList = new ArrayList<>();
        quantityList.add(quantity1);
        quantityList.add(quantity2);
        quantityList.add(quantity3);

        final QuantifiedObject potatoes = new QuantifiedObject("potatoes");
        potatoes.setOffsetStart(29);
        potatoes.setOffsetEnd(37);
        measurement.setQuantifiedObject(potatoes);

        measurement.setQuantityList(quantityList);

        measurementList.add(measurement);

        UUID expectedID = UUID.randomUUID();
        mockStatic(UUID.class);
        expect(UUID.randomUUID()).andReturn(expectedID);
        replay(UUID.class);

        Element out = target.trainingExtraction(measurementList, text);

        verify(UUID.class);
        assertThat(out.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">We need <measure type=\"list\" ptr=\"#" + expectedID + "\">1, 5 and 12 grams</measure> of <quantifiedObject id=\"" + expectedID + "\">potatoes</quantifiedObject></p>"));
    }


    @Test
    public void testTrainingExtraction() throws Exception {
        String text = "In this letter we present the discovery of a very light planetary companion to the star µ Ara (HD 160691). " +
                "The planet orbits its host once every 9.5 days, and induces a sinusoidal radial velocity signal with a semi-amplitude of 4.1 m s −1 , " +
                "the smallest Doppler amplitude detected so far. These values imply a mass of m2 sin i=14 M⊕ (earth-masses). " +
                "This detection represents the discovery of a planet with a mass slightly smaller than that of Uranus, the smallest \"ice giant\" in our Solar System. " +
                "Whether this planet can be considered an ice giant or a super-earth planet is discussed in the context of the core-accretion and migration models.";

        //Measurement 1
        Measurement measurement1 = new Measurement();
        measurement1.setType(UnitUtilities.Measurement_Type.VALUE);
        final Unit rawUnitOfMeasurement1 = new Unit("days", new OffsetPosition(149, 153));
        final Quantity quantityOfMeasurement1 = new Quantity("9.5", rawUnitOfMeasurement1);

        quantityOfMeasurement1.setOffsetStart(145);
        quantityOfMeasurement1.setOffsetEnd(148);
        measurement1.setAtomicQuantity(quantityOfMeasurement1);

        //Measurement 2
        Measurement measurement2 = new Measurement();
        measurement2.setType(UnitUtilities.Measurement_Type.VALUE);
        final Unit rawUnitOfMeasurement2 = new Unit("m s -1", new OffsetPosition(232, 238));
        final Quantity quantityOfMeasurement2 = new Quantity("4.1", rawUnitOfMeasurement2);

        quantityOfMeasurement2.setOffsetStart(228);
        quantityOfMeasurement2.setOffsetEnd(231);

        measurement2.setAtomicQuantity(quantityOfMeasurement2);
        final QuantifiedObject quantifiedObjectOfMeasurement2 = new QuantifiedObject();
        quantifiedObjectOfMeasurement2.setRawName("a semi-amplitude of");
        quantifiedObjectOfMeasurement2.setNormalizedName("semi-amplitude");
        quantifiedObjectOfMeasurement2.setOffsetStart(208);
        quantifiedObjectOfMeasurement2.setOffsetEnd(227);
        measurement2.setQuantifiedObject(quantifiedObjectOfMeasurement2);

        //Measurement 3
        Measurement measurement3 = new Measurement();
        measurement3.setType(UnitUtilities.Measurement_Type.VALUE);

        final Unit rawUnitOfMeasurement3 = new Unit("M⊕", new OffsetPosition(330, 332));
        final Quantity quantityOfMeasurement3 = new Quantity("14", rawUnitOfMeasurement3);
        quantityOfMeasurement3.setOffsetStart(327);
        quantityOfMeasurement3.setOffsetEnd(329);

//        final Unit parsedUnit = new Unit("M⊕", new OffsetPosition(330, 332));
//        final UnitBlock parsedUnitBlock = new UnitBlock("M", "⊕", "");
//        parsedUnitBlock.setRawTaggedValue("<prefix>M</prefix><base>⊕</base>");
//        parsedUnit.setProductBlocks(Arrays.asList(parsedUnitBlock));
//        quantityOfMeasurement3.setParsedUnit(parsedUnit);
//
//        final Value parsedValue = new Value(new BigDecimal(14));
//        final ValueBlock structure = new ValueBlock();
//        structure.setNumber("14");
//        structure.setRawTaggedValue("<number>14</number>");

//        parsedValue.setStructure(structure);
//        quantityOfMeasurement3.setParsedValue(parsedValue);

        measurement3.setAtomicQuantity(quantityOfMeasurement3);

        UUID expectedID = UUID.randomUUID();
        mockStatic(UUID.class);
        expect(UUID.randomUUID()).andReturn(expectedID);
        replay(UUID.class);

        final Element element = target.trainingExtraction(Arrays.asList(measurement1, measurement2, measurement3), text);

        verify(UUID.class);
        assertThat(element.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">In this letter we present the discovery of a very light planetary companion to the star µ Ara (HD 160691). " +
                "The planet orbits its host once every <measure type=\"value\">9.5 days</measure>, and induces a sinusoidal radial velocity signal " +
                "with <quantifiedObject id=\"" + expectedID + "\">a semi-amplitude of</quantifiedObject> <measure type=\"value\" ptr=\"#" + expectedID + "\">4.1 m s −1</measure> , " +
                "the smallest Doppler amplitude detected so far. These values imply a mass of m2 sin i=<measure type=\"value\">14 M⊕</measure> (earth-masses). " +
                "This detection represents the discovery of a planet with a mass slightly smaller than that of Uranus, the smallest \"ice giant\" in our Solar System. " +
                "Whether this planet can be considered an ice giant or a super-earth planet is discussed in the context of the core-accretion and migration models.</p>"));
        System.out.println(element.toXML());
    }

}