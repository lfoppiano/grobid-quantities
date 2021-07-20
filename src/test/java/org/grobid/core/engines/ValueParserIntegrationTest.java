package org.grobid.core.engines;

import org.grobid.core.data.Value;
import org.grobid.core.data.ValueBlock;
import org.junit.Before;
import org.junit.Test;

import static org.grobid.core.engines.UnitParserIntegrationTest.initEngineForTests;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ValueParserIntegrationTest {
    ValueParser target;

    @Before
    public void setUp() throws Exception {
        initEngineForTests();
        target = new ValueParser();
    }

    @Test
    public void testTagValue_exponential_1() throws Exception {
        String input = "0 . 3 x 10 - 7";
        ValueBlock output = target.tagValue(input);

        System.out.println(input + " -> " + output);
        System.out.println(output.getRawTaggedValue());

        assertThat(output.getNumber().toString(), is("0 . 3"));
        assertThat(output.getBase().toString(), is("10"));
        assertThat(output.getPow().toString(), is("- 7"));
    }

    @Test
    public void testTagValue_exponential_3() throws Exception {
        String input = "10 - 7";
        ValueBlock output = target.tagValue(input);

        System.out.println(input + " -> " + output);
        System.out.println(output.getRawTaggedValue());

        assertThat(output.getBase().toString(), is("10"));
        assertThat(output.getPow().toString(), is("- 7"));
    }

    @Test
    public void testTagValue_exponential_2() throws Exception {
        String input = "10 e -1";
        ValueBlock output = target.tagValue(input);
        System.out.println(input + " -> " + output);
        System.out.println(output.getRawTaggedValue());

        assertThat(output.getNumber().toString(), is("10"));
        assertThat(output.getExp().toString(), is("-1"));
    }

    @Test
    public void testParseValue_esponential_1() throws Exception {
        String input = "10 e -1";
        Value output = target.parseValue(input);
        System.out.println(input + " -> " + output);

        assertThat(output.getStructure().getNumber().toString(), is("10"));
        assertThat(output.getStructure().getExp().toString(), is("-1"));
    }

    @Test
    public void testParseValue_esponential_2() throws Exception {
        String input = "10 -30";
        Value output = target.parseValue(input);
        System.out.println(input + " -> " + output);

        assertThat(output.getStructure().getNumberAsString(), is(""));
        assertThat(output.getStructure().getBaseAsString(), is("10"));
        assertThat(output.getStructure().getPowAsString(), is("-30"));
    }

    @Test
    public void testParseValue_esponential_3() throws Exception {
        String input = "10 -33";
        Value output = target.parseValue(input);
        System.out.println(input + " -> " + output);

        assertThat(output.getStructure().getNumberAsString(), is(""));
        assertThat(output.getStructure().getBaseAsString(), is("10"));
        assertThat(output.getStructure().getPowAsString(), is("-33"));
    }
}