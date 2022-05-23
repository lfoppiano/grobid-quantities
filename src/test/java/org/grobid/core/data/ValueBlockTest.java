package org.grobid.core.data;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ValueBlockTest {

    private ValueBlock target;

    @Before
    public void setUp() throws Exception {
        target = new ValueBlock();
    }

    @Test
    public void testGetType_exponent() throws Exception {
        target = new ValueBlock(new Block("10"), new Block("-3"));

        assertThat(target.getType(), is(ValueBlock.Type.EXPONENT));
    }

    @Test
    public void testGetType_number_2() throws Exception {
        target = new ValueBlock();
        target.setNumber("10");

        assertThat(target.getType(), is(ValueBlock.Type.NUMBER));
    }

    @Test
    public void testGetType_number() throws Exception {
        target = new ValueBlock(new Block("3"), new Block("10"), new Block("-3"));

        assertThat(target.getType(), is(ValueBlock.Type.NUMBER));
    }

    @Test
    public void testGetType_number_onlyBaseAndPow() throws Exception {
        target = new ValueBlock(null, new Block("10"), new Block("-3"));

        assertThat(target.getType(), is(ValueBlock.Type.NUMBER));
    }

    @Test
    public void toJson() {
        target = new ValueBlock(new Block("1"), new Block("10"), new Block("20"));

        assertThat(target.toJson(), is("{ \"type\" : \"NUMBER\", \"formatted\" : \"1 x 10^20\" }"));
    }

    @Test
    public void toJson2() {
        target = new ValueBlock();
        target.setNumber("1");
        target.setExp("-5");
        assertThat(target.toJson(), is("{ \"type\" : \"EXPONENT\", \"formatted\" : \"1\" }"));
    }
}