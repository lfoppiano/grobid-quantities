package org.grobid.core.engines;

import org.apache.commons.io.IOUtils;
import org.grobid.core.data.Measurement;
import org.grobid.core.main.LibraryLoader;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by lfoppiano on 04.03.16.
 */
@Ignore
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
    public void testCreateTrainingDataFromText() throws Exception {
        QuantityParser parser = QuantityParser.getInstance();
        parser.createTraining("./src/test/resources/test1.txt",
                "./src/test/resources/test1.training.tei.xml", 0);
    }

}
