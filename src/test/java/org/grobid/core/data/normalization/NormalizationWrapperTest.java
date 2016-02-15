package org.grobid.core.data.normalization;

import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.utilities.UnitUtilities;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by lfoppiano on 14.02.16.
 */
public class NormalizationWrapperTest {

    private NormalizationWrapper target;

    @Before
    public void setUp() throws Exception {
        target = new NormalizationWrapper();
    }

    @Test
    public void testNormalization_baseUnit_noPow() throws Exception {
        String unitSymbol = "m";
        Map<String, Integer> normalized = target.extractUnitProducts(unitSymbol);
        assertThat(normalized.size(), is(1));
        assertTrue(normalized.containsKey(unitSymbol));
        assertThat(normalized.get(unitSymbol), is(1));
    }

    @Test
    public void testNormalization_baseUnit_pow2() throws Exception {
        String unitSymbol = "m^2";
        Map<String, Integer> normalized = target.extractUnitProducts(unitSymbol);
        assertThat(normalized.size(), is(1));
        String decomposedUnit = "m";
        assertTrue(normalized.containsKey(decomposedUnit));
        assertThat(normalized.get(decomposedUnit), is(2));
    }

    @Test
    public void testNormalization3_productUnit_pow2() throws Exception {
        String unitSymbol = "km^2";
        Map<String, Integer> normalized = target.extractUnitProducts(unitSymbol);
        assertThat(normalized.size(), is(1));
        String decomposedUnit = "km";
        assertTrue(normalized.containsKey(decomposedUnit));
        assertThat(normalized.get(decomposedUnit), is(2));
    }

    @Test
    public void testNormalization4_productUnit_pow2() throws Exception {
        String unitSymbol = "m/km^2";
        Map<String, Integer> normalized = target.extractUnitProducts(unitSymbol);
        assertThat(normalized.size(), is(2));
        assertTrue(normalized.containsKey("m"));
        assertTrue(normalized.containsKey("km"));
        assertThat(normalized.get("m"), is(1));
        assertThat(normalized.get("km"), is(-2));
    }

    @Test
    public void testNormalization_productUnit_celsius() throws Exception {
        String unitSymbol = "m*°C";
        Map<String, Integer> normalized = target.extractUnitProducts(unitSymbol);

        assertThat(normalized.size(), is(2));

    }


    @Test(expected = NormalizationException.class)
    public void testNormalization_productUnit_unknown() throws Exception {
        String unitSymbol = "m*J*y";
        Map<String, Integer> normalized = target.extractUnitProducts(unitSymbol);

//        assertThat(normalized.size(), is(2));


    }

    @Test(expected = NormalizationException.class)
    public void testNormalization_productUnit_unknown2() throws Exception {
        String unitSymbol = "μ*m";

        Map<String, Integer> normalized = target.extractUnitProducts(unitSymbol);

//        assertThat(normalized.size(), is(2));
    }

    @Test
    public void testNormalizeQuantity_simpleUnitWithoutNormalization_meters() throws Exception {
        Quantity input = new Quantity();
        input.setType(UnitUtilities.Unit_Type.LENGTH);
        input.setRawString("2 m");
        input.setRawValue("2");
        Unit raw = new Unit();
        raw.setRawName("m");
        input.setRawUnit(raw);

        Quantity output = target.normalizeQuantity(input);
        assertThat(output.getNormalizedUnit().getRawName(), is("m"));
        assertThat(output.getNormalizedValue(), is(2.0));
    }

    @Test
    public void testNormalizeQuantity_simpleUnitWithNormalization_kmToMeters() throws Exception {
        Quantity input = new Quantity();
        input.setType(UnitUtilities.Unit_Type.LENGTH);
        input.setRawString("2 km");
        input.setRawValue("2");
        Unit raw = new Unit();
        raw.setRawName("km");
        input.setRawUnit(raw);

        Quantity output = target.normalizeQuantity(input);
        assertThat(output.getNormalizedUnit().getRawName(), is("m"));
        assertThat(output.getNormalizedValue(), is(2000.0));
    }

    @Test
    public void testNormalizeQuantity_simpleUnitWithNormalization_CelsiusToKelvin() throws Exception {
        Quantity input = new Quantity();
        input.setType(UnitUtilities.Unit_Type.LENGTH);
        input.setRawString("10°C");
        input.setRawValue("10");
        Unit raw = new Unit();
        raw.setRawName("°C");
        input.setRawUnit(raw);

        Quantity output = target.normalizeQuantity(input);
        assertThat(output.getNormalizedUnit().getRawName(), is("K"));
        assertThat(output.getNormalizedValue(), is(283.15));
    }

    @Test
    public void testNormalizeQuantity_kmHourToMetersSecond() throws Exception {
        Quantity input = new Quantity();
        input.setType(UnitUtilities.Unit_Type.LENGTH);
        input.setRawString("2 km/h");
        input.setRawValue("2");
        Unit raw = new Unit();
        raw.setRawName("km/s");
        input.setRawUnit(raw);

        Quantity output = target.normalizeQuantity(input);
        assertThat(output.getNormalizedUnit().getRawName(), is("m"));
        assertThat(output.getNormalizedValue(), is(2000.0));
    }

}