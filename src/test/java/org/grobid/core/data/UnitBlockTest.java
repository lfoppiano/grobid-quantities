package org.grobid.core.data;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class UnitBlockTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testAsProduct() {
        assertThat(UnitBlock.asProduct(Arrays.asList(new UnitBlock("m", "m", "/"))), is("mm"));
    }

    @Test
    public void testAsProduct_withDenominator() {
        final List<UnitBlock> unitBlockList = Arrays.asList(
            new UnitBlock("m", "m", "2/"),
            new UnitBlock("k", "m", "2")
        );

        assertThat(UnitBlock.asProduct(unitBlockList), is("mm^2·km^-2"));
    }

    @Test
    public void testAsString_withDenominator() {
        final List<UnitBlock> unitBlockList = Arrays.asList(
            new UnitBlock("m", "m", "2/"),
            new UnitBlock("k", "m", "2")
        );

        assertThat(UnitBlock.asString(unitBlockList), is("mm^2/km^2"));
    }

    @Test
    public void testAsStringWithoutDenominator() {
        final List<UnitBlock> unitBlockList = Arrays.asList(
            new UnitBlock("m", "m", "2"),
            new UnitBlock("k", "m", "2")
        );

        assertThat(UnitBlock.asString(unitBlockList), is("mm^2·km^2"));
    }

    @Test
    public void testAsProductWithoutDenominator() {
        final List<UnitBlock> unitBlockList = Arrays.asList(
            new UnitBlock("m", "m", "2"),
            new UnitBlock("k", "m", "2")
        );

        assertThat(UnitBlock.asProduct(unitBlockList), is("mm^2·km^2"));
    }

    @Test
    public void testAsStringWithDenominator_2() {
        final List<UnitBlock> unitBlockList = Arrays.asList(
            new UnitBlock("m", "m", "2"),
            new UnitBlock("k", "m", "-2")
        );

        assertThat(UnitBlock.asString(unitBlockList), is("mm^2/km^2"));
    }

    @Test
    public void testAsProductWithDenominator_2() {
        final List<UnitBlock> unitBlockList = Arrays.asList(
            new UnitBlock("m", "m", "2"),
            new UnitBlock("k", "m", "-2")
        );

        assertThat(UnitBlock.asProduct(unitBlockList), is("mm^2·km^-2"));
    }
}