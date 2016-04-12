package org.grobid.core.lexicon;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.collections4.Closure;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
    public void test1() {
        target.readJsonFile(this.getClass().getResourceAsStream("/sample.unit.json"), "units", input -> {

            System.out.println(input.toString());

        });
    }


}