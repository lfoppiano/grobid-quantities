package org.grobid.core.engines.training;

import org.grobid.core.engines.QuantityParser;
import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class QuantityTrainingDataTest {

    private QuantityTrainingData target;

    @Before
    public void setUp() throws Exception {
        LibraryLoader.load();
        target = new QuantityTrainingData(QuantityParser.getInstance());

    }

    @Ignore("nonsense")
    @Test
    public void test() throws Exception {
        target.createTraining("/Users/lfoppiano/development/inria/grobid-resources/grobid-quantities/in/hal-00082280.pdf","/Users/lfoppiano/development/inria/grobid-resources/grobid-quantities/out", 1);
    }



    @Ignore("Nonsense as well with the hardcoded path... ")
    @Test
    public void test2() throws Exception {
//        String input = IOUtils.(this.getClass().getResourceAsStream("text.example.txt"))
//        String input = "In this letter we present the discovery of a very light planetary companion to the star µ Ara (HD 160691). The planet orbits its host once every 9.5 days, and induces a sinusoidal radial velocity signal with a semi-amplitude of 4.1 m s −1 , the smallest Doppler amplitude detected so far. These values imply a mass of m2 sin i=14 M⊕ (earth-masses). This detection represents the discovery of a planet with a mass slightly smaller than that of Uranus, the smallest \"ice giant\" in our Solar System. Whether this planet can be considered an ice giant or a super-earth planet is discussed in the context of the core-accretion and migration models.";
        target.createTrainingText(new File("/Users/lfoppiano/development/inria/grobid/grobid-quantities/src/test/resources/org/grobid/engines/training/text.example.txt"), "/tmp", 1);
    }
}