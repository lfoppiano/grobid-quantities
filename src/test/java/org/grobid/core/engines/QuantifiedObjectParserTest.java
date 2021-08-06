package org.grobid.core.engines;

import org.apache.commons.io.IOUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.QuantifiedObject;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.GrobidConfig;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.UnitUtilities;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

public class QuantifiedObjectParserTest {

    private QuantifiedObjectParser target;

    @BeforeClass
    public static void before() throws Exception {
        GrobidConfig.ModelParameters modelParameters = new GrobidConfig.ModelParameters();
        modelParameters.name = "bao";
        GrobidProperties.addModel(modelParameters);
    }

    @Before
    public void setUp() throws Exception {
        GrobidConfig.ModelParameters modelParameters = new GrobidConfig.ModelParameters();
        modelParameters.name = "bao";
        GrobidProperties.addModel(modelParameters);
        target = new QuantifiedObjectParser(GrobidModels.DUMMY);
    }

    @Ignore("Not a unit test ;-) ")
    @Test
    public void testParser() throws Exception {
//        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken("A 20kg ingot is made in a high frequency induction melting furnace and forged to 30mm in thickness and 90mm in width at 850 to 1,150°C.");
        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken("In all three cases, the superconducting transition temp- erature (TC) is found to be around T c ∼ 23 K. For x = 12.3 we have a y = 23.");

        List<Measurement> measurements = QuantityParser.getInstance(true).process(tokens);

        List<Measurement> measurementList = target.process(tokens, measurements);

//        measurementList.stream().forEach(System.out::println);


        measurementList.stream().filter(m -> m.getQuantifiedObject() != null).forEach(System.out::println);
    }


    @Test
    public void testAttachQuantifiedObjects() throws Exception {
        List<Measurement> measurementList = new ArrayList<>();

        Measurement measurement1 = new Measurement();
        measurement1.setType(UnitUtilities.Measurement_Type.VALUE);
        measurement1.setAtomicQuantity(new Quantity("20", new Unit("kg", 4, 6), 2, 4));
        measurementList.add(measurement1);

        Measurement measurement2 = new Measurement();
        measurement2.setType(UnitUtilities.Measurement_Type.VALUE);
        measurement2.setAtomicQuantity(new Quantity("30", new Unit("mm", 83, 85), 81, 83));
        measurementList.add(measurement2);

        Measurement measurement3 = new Measurement();
        measurement3.setType(UnitUtilities.Measurement_Type.VALUE);
        measurement3.setAtomicQuantity(new Quantity("90", new Unit("mm", 105, 107), 103, 105));
        measurementList.add(measurement3);

        Measurement measurement4 = new Measurement();
        measurement4.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        measurement4.setQuantityLeast(new Quantity("850", new Unit("°C", 132, 134), 120, 123));
        measurement4.setQuantityMost(new Quantity("1,150", new Unit("°C", 132, 134), 127, 132));
        measurementList.add(measurement4);

        List<QuantifiedObject> quantifiedObjects = new ArrayList<>();
        quantifiedObjects.add(new QuantifiedObject("ingot", null, 7,13, QuantifiedObject.Attachment.LEFT));
        quantifiedObjects.add(new QuantifiedObject("thickness", null, 89,99, QuantifiedObject.Attachment.LEFT));
        quantifiedObjects.add(new QuantifiedObject("width", null, 111,117, QuantifiedObject.Attachment.LEFT));

        List<Measurement> outputMeasurements = target.attachQuantifiedObjects(quantifiedObjects, measurementList);

        assertThat(outputMeasurements, hasSize(measurementList.size()));
    }


    @Test
    public void testExtractOutput() throws Exception {

        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken("A 20kg ingot is made in a high frequency induction melting furnace and forged to 30mm in thickness and 90mm in width at 850 to 1,150°C.");
        String result = IOUtils.toString(this.getClass().getResourceAsStream("result.sample.txt"), UTF_8);

        List<Measurement> measurementList = new ArrayList<>();
        Measurement measurement1 = new Measurement();
        measurement1.setType(UnitUtilities.Measurement_Type.VALUE);
        measurement1.setAtomicQuantity(new Quantity("20", new Unit("kg", 4, 6), 2, 4));
        measurementList.add(measurement1);

        Measurement measurement2 = new Measurement();
        measurement2.setType(UnitUtilities.Measurement_Type.VALUE);
        measurement2.setAtomicQuantity(new Quantity("30", new Unit("mm", 83, 85), 81, 83));
        measurementList.add(measurement2);

        Measurement measurement3 = new Measurement();
        measurement3.setType(UnitUtilities.Measurement_Type.VALUE);
        measurement3.setAtomicQuantity(new Quantity("90", new Unit("mm", 105, 107), 103, 105));
        measurementList.add(measurement3);

        Measurement measurement4 = new Measurement();
        measurement4.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        measurement4.setQuantityLeast(new Quantity("850", new Unit("°C", 132, 134), 120, 123));
        measurement4.setQuantityMost(new Quantity("1,150", new Unit("°C", 132, 134), 127, 132));
        measurementList.add(measurement4);

        List<QuantifiedObject> quantifiedObjects = target.extractOutput(result, tokens);

        assertThat(quantifiedObjects, hasSize(3));

        assertThat(quantifiedObjects.get(0).getRawName(), is("ingot"));
        assertThat(quantifiedObjects.get(1).getRawName(), is("thickness"));
        assertThat(quantifiedObjects.get(2).getRawName(), is("width"));

    }

}