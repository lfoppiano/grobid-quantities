package org.grobid.core.lexicon;

import org.grobid.core.data.RegexValueHolder;
import org.grobid.core.utilities.OffsetPosition;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrice Lopez
 */
public class QuantityLexiconIntegrationTest {

    private QuantityLexicon target = null;

    @Before
    public void setUp() {
        target = QuantityLexicon.getInstance();
    }

    @Test
    public void testInUnitDictionary_meter() throws Exception {
        String input = "meter";
        boolean test = target.inUnitDictionary(input);
        assertThat(test, is(true));

        input = "m";
        test = target.inUnitDictionary(input);
        assertThat(test, is(true));

        input = "m";
        test = target.inUnitDictionary(input);
        assertThat(test, is(true));
    }

    @Test
    public void testInUnitDictionary_weight() throws Exception {
        String input = "kg";
        boolean test = target.inUnitDictionary(input);
        assertThat(test, is(true));
    }

    @Test
    public void testInUnitDictionary_pressure() throws Exception {
        String input = "pa";
        boolean test = target.inUnitDictionaryCaseInsensitive(input);
        assertThat(test, is(true));
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
        assertThat(output.get(0), is("m/s"));
        assertThat(output.get(1), is("m/zs"));
    }

    @Test
    public void testDerivationalMorphologyExpansion_simpleNotation_noNotation_ChecknoDuplicates() throws Exception {

    }

    @Test
    public void testDerivationalMorphologyExpansion_simpleName_noNotation_ChecknoDuplicates() throws Exception {
        List<String> output = target.derivationalMorphologyExpansion("meter", false);

        Set<String> set = new HashSet<>(output);
        assertThat(set.size(), is(output.size()));

        assertThat(output.get(0), is("meter"));
        assertThat(output.get(1), is("zeptometer"));
        assertThat(output.get(2), is("zettameter"));
    }

    @Test
    @Ignore("Obsolete")
    public void testInflectionMorphologyExpansion_simpleNotation_noNotation_ChecknoDuplicates() throws Exception {
        List<String> output = target.getInflectionsByTerm("meter");

        Set<String> set = new HashSet<>(output);
        assertThat(set.size(), is(output.size()));
        assertThat(set.size(), is(2));

        assertThat(output.get(0), is("meter"));
        assertThat(output.get(1), is("meters"));
    }

    @Test
    public void testLookupNameByInflection_simpleUnit_shouldWork() throws Exception {
        String output = target.getNameByInflection("meter");

        assertThat(output, is("m"));
    }

    @Test
    public void testLookupNameByInflection_complexUnit_shouldWork() throws Exception {
        String output = target.getNameByInflection("kilometer");

        assertThat(output, is("km"));
    }

    @Test
    public void testDerivationalMorphologyExpansion_simpleNotation() throws Exception {
        List<String> output = target.derivationalMorphologyExpansion("m", true);

        assertThat(output.size(), is(21));
        assertThat(output.get(0), is("m"));
        assertThat(output.get(1), is("zm"));
        assertThat(output.get(2), is("Zm"));
        assertThat(output.get(3), is("dm"));
    }

    @Test
    public void testDerivationalMorphologyExpansion_noNotation() throws Exception {
        List<String> output = target.derivationalMorphologyExpansion("metre", false);

        assertThat(output.size(), is(21));
        assertThat(output.get(0), is("metre"));
        assertThat(output.get(1), is("zeptometre"));
        assertThat(output.get(2), is("zettametre"));
        assertThat(output.get(3), is("decimetre"));
    }

    @Test
    public void testInPrefixDictionary_G() throws Exception {
        assertThat(target.inPrefixDictionary("G"), is(true));
    }

    @Test
    public void testDecomposeComplexUnitWithDelimiter() throws Exception {
        List<RegexValueHolder> out = target.decomposeComplexUnit("m/s2");

        assertThat(out.size(), is(2));

        assertThat(out.get(0).getValue(), is("m"));
        assertThat(out.get(0).getStart(), is(0));
        assertThat(out.get(0).getEnd(), is(1));
        assertThat(out.get(1).getValue(), is("s2"));
        assertThat(out.get(1).getStart(), is(2));
        assertThat(out.get(1).getEnd(), is(4));
    }

    @Test
    public void testDecomposeComplexUnitWithDelimiter2() throws Exception {
        List<RegexValueHolder> out = target.decomposeComplexUnit("mol/m^3");

        assertThat(out.size(), is(2));

        assertThat(out.get(0).getValue(), is("mol"));
        assertThat(out.get(0).getStart(), is(0));
        assertThat(out.get(0).getEnd(), is(3));
        assertThat(out.get(1).getValue(), is("m^3"));
        assertThat(out.get(1).getStart(), is(4));
        assertThat(out.get(1).getEnd(), is(7));
    }

    @Test
    public void testDecomposeComplexUnitWithDelimiter3() throws Exception {
        List<RegexValueHolder> out = target.decomposeComplexUnit("cm⁻¹");

        assertThat(out.size(), is(1));

        assertThat(out.get(0).getValue(), is("cm⁻¹"));
        assertThat(out.get(0).getStart(), is(0));
        assertThat(out.get(0).getEnd(), is(4));
    }
}