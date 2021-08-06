package org.grobid.core.lexicon;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by lfoppiano on 12/04/16.
 */
public class LexiconLoaderTest {

    LexiconLoader target;

    @Before
    public void setUp() {
        target = new LexiconLoader();
    }

    @Test
    public void testLoadJsonFile() throws Exception {

        List<String> outputs = new ArrayList<>();

        target.readJsonFile(this.getClass().getResourceAsStream("/sample.unit.json"), "units",
                input -> outputs.add(input.toString()));

        assertThat(outputs, hasSize(2));
    }

    @Test
    public void testLoadCsvFile() throws Exception {

        List<String> outputs = new ArrayList<>();

        target.readCsvFile(this.getClass().getResourceAsStream("/sample.unit.csv"),
                input -> outputs.add(input.toString()));

        assertThat(outputs, hasSize(3));
    }


}