package org.grobid.core.engines;

import org.grobid.core.data.Value;
import org.grobid.core.data.ValueBlock;
import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ValueParserIntegrationTest {
    ValueParser target;

    @Before
    public void setUp() throws Exception {
        LibraryLoader.load();
        target = new ValueParser();
    }

    @Test
    public void testTagUnit_exponential_1() throws Exception {
        String input = "0.3 x 10-7";
        ValueBlock output = target.tagValue(input);

        System.out.println(input + " -> " + output);
        System.out.println(output.getRawTaggedValue());
        
        assertThat(output.getNumber(), is("0.3"));
        assertThat(output.getBase(), is("10"));
        assertThat(output.getPow(), is("-7"));
    }

    @Test
    public void testTagUnit_exponential_2() throws Exception {
        String input = "10 e -1";
        ValueBlock output = target.tagValue(input);
        System.out.println(input + " -> " + output);
        System.out.println(output.getRawTaggedValue());

        assertThat(output.getNumber(), is("10"));
        assertThat(output.getExp(), is("-1"));
    }

    @Test
    @Ignore
    public void testParseValue_esponential_1() throws Exception {
        String input = "10 e -1";
        Value output = target.parseValue(input);
        System.out.println(input + " -> " + output);

        assertThat(output.getStructure().getNumber(), is("10"));
        assertThat(output.getStructure().getExp(), is("-1"));
    }

}