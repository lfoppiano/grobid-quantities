package org.grobid.core.lexicon;

import java.util.List;

import org.grobid.core.utilities.OffsetPosition;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
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

    private QuantityLexicon lexicon = null;

    @Before
    public void setUp() {
        lexicon = QuantityLexicon.getInstance();
    }

    @Test
    public void testInUnitDictionary() throws Exception {
        String input = "meter";
        boolean test = lexicon.inUnitDictionary(input);
        assertEquals("Problem with inUnitDictionary", test, true);

        input = "m";
        test = lexicon.inUnitDictionary(input);
        assertEquals("Problem with inUnitDictionary", test, true);
    }

    @Test
    public void testInUnitNamesSimple_singular() throws Exception {
        String input = "1 meter";
        List<OffsetPosition> unitPositions = lexicon.inUnitNames(input);
        assertNotNull("Problem with unit matcher", unitPositions);
        assertThat("Problem with unit matching position.", unitPositions.size(), greaterThan(0));
    }

    @Test
    public void testInUnitNamesSimple_plural() throws Exception {
        String input = "10 meters";
        List<OffsetPosition> unitPositions = lexicon.inUnitNames(input);
        assertNotNull("Problem with unit matcher", unitPositions);
        assertThat("Problem with unit matching position.", unitPositions.size(), greaterThan(0));
    }

    @Test
    public void testinUnitNamesComplex_simpleName() throws Exception {
        String input = "kilometer";
        List<OffsetPosition> unitPositions = lexicon.inUnitNames(input);
        assertNotNull(unitPositions);
        assertNotNull("Problem with unit matcher", unitPositions);
        assertThat("Problem with unit matching position.", unitPositions.size(), greaterThan(0));
    }

    @Test
    public void testinUnitNamesComplex_speedName() throws Exception {
        String input = "kilometer per second";
        List<OffsetPosition> unitPositions = lexicon.inUnitNames(input);
        assertNotNull(unitPositions);
        assertNotNull("Problem with unit matcher", unitPositions);
        assertThat("Problem with unit matching position.", unitPositions.size(), greaterThan(0));
    }

    @Test
    public void testinUnitNamesComplex_speedNotation() throws Exception {
        String input = "10 kg / s";
        List<OffsetPosition> unitPositions = lexicon.inUnitNames(input);
        assertNotNull(unitPositions);
        assertNotNull("Problem with unit matcher", unitPositions);
        assertThat("Problem with unit matching position.", unitPositions.size(), greaterThan(0));
    }
}