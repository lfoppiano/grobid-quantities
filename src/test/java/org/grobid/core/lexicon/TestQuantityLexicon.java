package org.grobid.core.lexicon;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.grobid.core.utilities.OffsetPosition;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.*;

/**
 * @author Patrice Lopez
 */
public class TestQuantityLexicon {

    private QuantityLexicon target = null;

    @Before
    public void setUp() {
        target = QuantityLexicon.getInstance();
    }

    @Test
    public void testInUnitDictionary() throws Exception {
        String input = "meter";
        boolean test = target.inUnitDictionary(input);
        assertEquals("Problem with inUnitDictionary", test, true);

        input = "m";
        test = target.inUnitDictionary(input);
        assertEquals("Problem with inUnitDictionary", test, true);
    }

    @Test
    public void testInUnitNamesSimple_singular() throws Exception {
        String input = "1 meter";
        List<OffsetPosition> unitPositions = target.inUnitNames(input);
        assertNotNull("Problem with unit matcher", unitPositions);
        assertThat("Problem with unit matching position.", unitPositions.size(), greaterThan(0));
    }

    @Test
    public void testInUnitNamesSimple_plural() throws Exception {
        String input = "10 meters";
        List<OffsetPosition> unitPositions = target.inUnitNames(input);
        assertNotNull("Problem with unit matcher", unitPositions);
        assertThat("Problem with unit matching position.", unitPositions.size(), greaterThan(0));
    }

    @Test
    public void testinUnitNamesComplex_simpleName() throws Exception {
        String input = "kilometer";
        List<OffsetPosition> unitPositions = target.inUnitNames(input);
        assertNotNull(unitPositions);
        assertNotNull("Problem with unit matcher", unitPositions);
        assertThat("Problem with unit matching position.", unitPositions.size(), greaterThan(0));
    }

    @Test
    public void testinUnitNamesComplex_speedName() throws Exception {
        String input = "kilometer per second";
        List<OffsetPosition> unitPositions = target.inUnitNames(input);
        assertNotNull(unitPositions);
        assertNotNull("Problem with unit matcher", unitPositions);
        assertThat("Problem with unit matching position.", unitPositions.size(), greaterThan(0));
    }

    @Test
    public void testinUnitNamesComplex_speedNotation() throws Exception {
        String input = "10 kg / s";
        List<OffsetPosition> unitPositions = target.inUnitNames(input);
        assertNotNull(unitPositions);
        assertNotNull("Problem with unit matcher", unitPositions);
        assertThat("Problem with unit matching position.", unitPositions.size(), greaterThan(0));
    }

    @Test
    public void testDerivationalMorphologyExpansion_complesNotation_ChecknoDuplicates() throws Exception {
        List<String> output = target.derivationalMorphologyExpansion("m/s", true);

        Set<String> set = new HashSet<>(output);
        assertThat(set.size(), is(output.size()));
    }

    @Test
    public void testDerivationalMorphologyExpansion_simpleNotation() throws Exception {
        List<String> output = target.derivationalMorphologyExpansion("m", true);

        assertThat(output.size(), is(21));
    }
}