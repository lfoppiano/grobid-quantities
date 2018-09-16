package org.grobid.core.engines;

import org.grobid.core.data.ValueBlock;
import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ValueParserIntegrationTest {
    ValueParser target;

    @Before
    public void setUp() throws Exception {
        LibraryLoader.load();
        target = new ValueParser();
    }

    @Test
    public void testTagUnit1() throws Exception {

        String input = "10 e -1";
        List<ValueBlock> output = target.tagValue(input);
        System.out.println(input + " -> " + output);

        input = "4560 10 100";
        output = target.tagValue(input);
        System.out.println(input + " -> " + output);
    }

}