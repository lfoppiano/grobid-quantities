package org.grobid.core.engines;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.main.LibraryLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * @author Patrice Lopez
 */
public class TestQuantityParser {

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
    }

    @Test
    public void testQuantityParser1() throws Exception {

        String text = IOUtils.toString(
                this.getClass().getResourceAsStream("/test1.txt"));
        System.out.println("\ntest1.txt\n" + text + "\n");

        QuantityParser parser = new QuantityParser();

        List<Measurement> measurements = parser.extractQuantities(text);
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
                this.getClass().getResourceAsStream("/test2.txt"));
        System.out.println("\ntest2.txt\n" + text + "\n");

        QuantityParser parser = new QuantityParser();

        List<Measurement> measurements = parser.extractQuantities(text);
        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
            }
        } else {
            System.out.println("No measurement found.");
        }
    }

    /*@Test
    public void testQuantityParser3() throws Exception {

        String text = IOUtils.toString(
                this.getClass().getResourceAsStream("/test3.txt"));

        QuantityParser parser = new QuantityParser();

        List<Measurement> measurements = parser.extractQuantities(text);
        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
            }
        } else {
            System.out.println("No measurement found.");
        }
    }*/
}