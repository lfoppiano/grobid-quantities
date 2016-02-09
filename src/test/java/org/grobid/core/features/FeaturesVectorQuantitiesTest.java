package org.grobid.core.features;

import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by lfoppiano on 10.02.16.
 */
public class FeaturesVectorQuantitiesTest {

    @Before
    public void setUp() throws Exception {
        LibraryLoader.load();
    }

    @Test
    public void testPrintVector1() throws Exception {
        String word = "Colorado";
        String label = "CITY";

        FeaturesVectorQuantities target = FeaturesVectorQuantities.addFeaturesQuantities(word, label, true, true);

        assertThat(target.printVector(), is("Colorado colorado C Co Col Colo o do ado rado INITCAP NODIGIT 0 NOPUNCT 8 Colorado Xxxx Xx 1 1 CITY"));
    }

    @Test
    public void testPrintVector2() throws Exception {
        String word = "The";
        String label = "OTHER";

        FeaturesVectorQuantities target = FeaturesVectorQuantities.addFeaturesQuantities(word, label, true, true);

        assertThat(target.printVector(), is("The the T Th The The e he The The INITCAP NODIGIT 0 NOPUNCT 3 The Xxx Xx 1 1 OTHER"));
    }

    @Test
    public void testPrintVector3() throws Exception {
        String word = "a";
        String label = "OTHER";

        FeaturesVectorQuantities target = FeaturesVectorQuantities.addFeaturesQuantities(word, label, true, true);

        assertThat(target.printVector(), is("a a a a a a a a a a NOCAPS NODIGIT 1 NOPUNCT 1 a x x 1 1 OTHER"));
    }
}