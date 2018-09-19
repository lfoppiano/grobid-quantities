package org.grobid.core.data;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class ValueTest {

    @Test
    public void testToJson_numericValue() {
        final Value value = new Value();
        value.setNumeric(new BigDecimal(123));
        final ValueBlock structure = new ValueBlock();
        structure.setNumber("123");
        value.setStructure(structure);

        System.out.println(value.toJson());
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

        System.out.println(value.toJson());
    }

}