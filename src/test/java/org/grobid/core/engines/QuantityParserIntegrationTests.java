package org.grobid.core.engines;

import org.apache.commons.io.IOUtils;
import org.grobid.core.data.Measurement;
import org.grobid.core.main.LibraryLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 04.03.16.
 */
public class QuantityParserIntegrationTests {

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
    }

    @Test
    public void testNotNormalizedQuantity() throws Exception {
        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process("10 meters");


        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
                System.out.println(measurement.toJson());
            }
        } else {
            System.out.println("No measurement found.");
        }

    }

    @Test
    public void testNormalizeableQuantity() throws Exception {
        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process("10 km");


        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
                System.out.println(measurement.toJson());
            }
        } else {
            System.out.println("No measurement found.");
        }

    }

    @Test
    public void testNormalizeableWordsQuantity() throws Exception {
        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process("ten km");


        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
                System.out.println(measurement.toJson());
            }
        } else {
            System.out.println("No measurement found.");
        }

    }

    @Test
    public void testNormalizeableQuantity2() throws Exception {
        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process("10 m^1*s^-1");


        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
                System.out.println(measurement.toJson());
            }
        } else {
            System.out.println("No measurement found.");
        }

    }

    @Test
    public void testQuantityParser1() throws Exception {

        String text = IOUtils.toString(
                this.getClass().getResourceAsStream("/test1.txt"), UTF_8);
        System.out.println("\ntest1.txt\n" + text + "\n");

        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process(text);
        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
            }
        } else {
            System.out.println("No measurement found.");
        }
    }

    @Test
    public void testQuantityParser2() throws Exception {

        String text = IOUtils.toString(
                this.getClass().getResourceAsStream("/test2.txt"), UTF_8);
        System.out.println("\ntest2.txt\n" + text + "\n");

        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process(text);
        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
            }
        } else {
            System.out.println("No measurement found.");
        }
    }

    @Test
    public void testQuantityParser3() throws Exception {

        String text = IOUtils.toString(
                this.getClass().getResourceAsStream("/test0.training.txt"), UTF_8);

        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process(text);
        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
            }
        } else {
            System.out.println("No measurement found.");
        }
    }


    @Test
    public void testQuantityParser4() throws Exception {
        QuantityParser parser = QuantityParser.getInstance(true);

//        String text = "\n\nFirst, it was heated to 840°C to form austenite structure and cooled at the speed of 100°C/hour to harden.";
        String text = "A 20kg ingot is made in a high frequency induction melting furnace and forged to 30mm in thickness and 90mm in width at 850 to 1,150°C. Specimens No.2 to 4, 6 and 15 are materials embodying the invention. Others are for comparison. No.1 is a material equivalent to ASTM standard A469-88 class 8 for generator rotor shaft material. No. 5 is a material containing relatively high Al content. \n" +
                "\n" +
                "These specimens underwent heat treatment by simulating the conditions for the large size rotor shaft centre of a large capacity generator. First, it was heated to 840°C to form austenite structure and cooled at the speed of 100°C/hour to harden. Then, the specimen was heated and held at 575 to 590°C for 32 hours and cooled at a speed of 15°C/hour. Tempering was done at such a temperature to secure tensile strength in the range of 100 to 105kg/mm2 for each specimen.";
        List<Measurement> measurements = parser.process(text);

        assertThat(measurements, hasSize(10));
        assertThat(measurements.get(4).getQuantityAtomic().getRawValue(), is("840"));
        assertThat(measurements.get(5).getQuantityAtomic().getRawValue(), is("100"));

        int offsetStart0 = measurements.get(4).getQuantityAtomic().getOffsetStart();
        int offsetEnd0 = measurements.get(4).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart0, offsetEnd0), is("840"));

        int offsetStart1 = measurements.get(5).getQuantityAtomic().getOffsetStart();
        int offsetEnd1 = measurements.get(5).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart1, offsetEnd1), is("100"));
    }

    @Test
    public void testQuantityParser5() throws Exception {
        QuantityParser parser = QuantityParser.getInstance(true);

        String text = "\n\nFirst, it was heated to 840°C to form austenite structure and cooled at the speed of 100°C/hour to harden.";

        List<Measurement> measurements = parser.process(text);

        assertThat(measurements, hasSize(2));
        assertThat(measurements.get(0).getQuantityAtomic().getRawValue(), is("100"));
        assertThat(measurements.get(1).getQuantityAtomic().getRawValue(), is("840"));

        int offsetStart0 = measurements.get(0).getQuantityAtomic().getOffsetStart();
        int offsetEnd0 = measurements.get(0).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart0, offsetEnd0), is("100"));

        int offsetStart1 = measurements.get(1).getQuantityAtomic().getOffsetStart();
        int offsetEnd1 = measurements.get(1).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart1, offsetEnd1), is("840"));
    }





}
