package org.grobid.core.features;

import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Created by lfoppiano on 20.02.16.
 */
public class FeaturesVectorUnitTest {

    @Before
    public void setUp() throws Exception {
        LibraryLoader.load();
    }

    @Test
    public void testPrintVector_sample1() throws Exception {
        FeaturesVectorUnit output = FeaturesVectorUnit.addFeaturesUnit("c", "LABEL", false, true, false);
        String outputString = output.printVector();

        assertThat(outputString, is("c 0 0 0 1 NOPUNCT 0 LABEL"));
    }

    @Test
    public void testPrintVector_sample2() throws Exception {
        FeaturesVectorUnit output = FeaturesVectorUnit.addFeaturesUnit("2", "LABEL", false, true, true);
        String outputString = output.printVector();

        assertThat(outputString, is("2 1 1 0 1 NOPUNCT 1 LABEL"));
    }

    @Test
    public void testAddFeaturesUnit() throws Exception {
        FeaturesVectorUnit output = FeaturesVectorUnit.addFeaturesUnit("c", null, false, true, false);

        assertNotNull(output.isDigit);
        assertNotNull(output.isKnownUnitToken);
        assertNotNull(output.isUpperCase);
        assertNotNull(output.punctType);
        assertNotNull(output.hasRightAttachment);
        assertNull(output.label);
    }

    @Test
    public void testAddFeaturesUnit_prefix1() throws Exception {
        FeaturesVectorUnit output = FeaturesVectorUnit.addFeaturesUnit("G", null, false, true, true);

        assertNotNull(output.isDigit);
        assertNotNull(output.isKnownUnitToken);
        assertNotNull(output.isUpperCase);
        assertNotNull(output.punctType);
        assertTrue(output.hasRightAttachment);
        assertNull(output.label);
    }
}