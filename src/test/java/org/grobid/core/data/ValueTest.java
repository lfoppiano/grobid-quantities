package org.grobid.core.data;

import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ValueTest {

    @Test
    public void testToJson_numericValue() {
        final Value value = new Value();
        value.setNumeric(new BigDecimal(123));
        final ValueBlock structure = new ValueBlock();
        structure.setNumber("123");
        value.setStructure(structure);

        assertThat(value.toJson(), is("{ \"numeric\" : 123, \"structure\" : { \"type\" : \"NUMBER\", \"formatted\" : \"123\" }, \"parsed\" : \"123\" }"));
    }

    @Test
    public void testToJson_complexNumericValue() {
        final Value value = new Value();
        value.setNumeric(new BigDecimal(123));
        final ValueBlock structure = new ValueBlock();
        structure.setNumber("123");
        structure.setBase("10");
        structure.setPow("-2");
        value.setStructure(structure);

        assertThat(value.toJson(), is("{ \"numeric\" : 123, \"structure\" : { \"type\" : \"NUMBER\", \"formatted\" : \"123 x 10^-2\" }, \"parsed\" : \"123 x 10^-2\" }"));
    }

}