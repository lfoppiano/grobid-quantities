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

    @Ignore("Integration test")
    @Test
    public void test() throws Exception {
        target.createTraining(
                "/Users/lfoppiano/development/nims/projects/grobid/grobid-quantities/resources/dataset/original/pdf/test/halshs-01279855.pdf",
                "/tmp",
                1);
    }
}