package org.grobid.core.data.normalization;

import org.grobid.core.data.UnitBlock;
import org.grobid.core.engines.UnitParser;
import org.grobid.core.lexicon.QuantityLexicon;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.grobid.core.engines.UnitParserIntegrationTest.initEngineForTests;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

public class UnitNormalizerIntegrationTest {

    private UnitNormalizer target;
    private UnitParser mockUnitParser;
    private QuantityLexicon mockQuantityLexicon;

    @BeforeClass
    public static void setUpClass() throws Exception {
        initEngineForTests();
    }
    
    @Before
    public void setUp() {
        target = new UnitNormalizer();
        mockUnitParser = createMock(UnitParser.class);
        target.setUnitParser(mockUnitParser);

        mockQuantityLexicon = createMock(QuantityLexicon.class);
        target.setQuantityLexicon(mockQuantityLexicon);
    }

    /*@Test
    public void testParseAndReformat_baseUnit_noPow() throws Exception {
        String unitSymbol = "m";

        expect(mockQuantityLexicon.getNameByInflection("m")).andReturn(null);
        final UnitBlock block = new UnitBlock("m");
        expect(mockUnitParser.tagUnit("m", false)).andReturn(Arrays.asList(new UnitBlock[]{block}));

        replay(mockQuantityLexicon, mockUnitParser);
        String reformatted = target.parseAndReformat(unitSymbol, false);
        verify(mockQuantityLexicon, mockUnitParser);

        assertThat(reformatted, is("m"));
    }

    @Test
    public void testParseAndReformat_baseUnit_noPow2() throws Exception {
        String unitSymbol = "s";

        expect(mockQuantityLexicon.getNameByInflection(unitSymbol)).andReturn(null);
        final UnitBlock block = new UnitBlock("s");
        expect(mockUnitParser.tagUnit(unitSymbol, false)).andReturn(Arrays.asList(new UnitBlock[]{block}));

        replay(mockQuantityLexicon, mockUnitParser);
        String reformatted = target.parseAndReformat(unitSymbol, false);
        verify(mockQuantityLexicon, mockUnitParser);

        assertThat(reformatted, is("s"));
    }

    @Test
    public void testParseAndReformat_baseUnit_noPow3() throws Exception {
        String unitSymbol = "ml";

        expect(mockQuantityLexicon.getNameByInflection("ml")).andReturn(null);
        final UnitBlock block = new UnitBlock("ml");
        expect(mockUnitParser.tagUnit("ml", false)).andReturn(Arrays.asList(new UnitBlock[]{block}));

        replay(mockQuantityLexicon, mockUnitParser);
        String reformatted = target.parseAndReformat(unitSymbol, false);
        verify(mockQuantityLexicon, mockUnitParser);

        assertThat(reformatted, is("ml"));
    }

    @Test
    public void testParseAndReformat_baseUnit_fullName_noPow() throws Exception {
        String unitSymbol = "meter";

        expect(mockQuantityLexicon.getNameByInflection(unitSymbol)).andReturn("m");
        replay(mockQuantityLexicon);
        String reformatted = target.parseAndReformat(unitSymbol, false);
        verify(mockQuantityLexicon);

        assertThat(reformatted, is("m"));
    }

    @Test
    public void testParseAndReformat_composedUnit_noPow() throws Exception {
        String unitSymbol = "hm";
        expect(mockQuantityLexicon.getNameByInflection(unitSymbol)).andReturn(null);

        final UnitBlock block = new UnitBlock("h", "m", null);
        expect(mockUnitParser.tagUnit(unitSymbol, false)).andReturn(Arrays.asList(new UnitBlock[]{block}));

        replay(mockQuantityLexicon, mockUnitParser);
        String normalized = target.parseAndReformat(unitSymbol, false);
        verify(mockQuantityLexicon, mockUnitParser);

        assertThat(normalized, is("hm"));
    }

    @Test
    public void testParseAndReformat_baseUnit_pow2() throws Exception {
        String unitSymbol = "m^2";
        expect(mockQuantityLexicon.getNameByInflection(unitSymbol)).andReturn(null);
        final UnitBlock block = new UnitBlock(null, "m", "2");
        expect(mockUnitParser.tagUnit(unitSymbol, false)).andReturn(Arrays.asList(new UnitBlock[]{block}));

        replay(mockQuantityLexicon, mockUnitParser);
        String normalized = target.parseAndReformat(unitSymbol, false);
        verify(mockQuantityLexicon, mockUnitParser);
        String normalizedUnit = "m²";

        assertThat(normalized, is(normalizedUnit));

    }


    @Test
    public void testParseAndReformat_transformedUnit_pow2() throws Exception {
        String unitSymbol = "km^2";
        expect(mockQuantityLexicon.getNameByInflection(unitSymbol)).andReturn(null);
        final UnitBlock block = new UnitBlock("k", "m", "2");
        expect(mockUnitParser.tagUnit(unitSymbol, false)).andReturn(Arrays.asList(new UnitBlock[]{block}));

        replay(mockUnitParser, mockQuantityLexicon);
        String reformatted = target.parseAndReformat(unitSymbol, false);
        verify(mockUnitParser, mockQuantityLexicon);
        String expected = "km²";

        assertThat(reformatted, is(expected));
    }


    @Test
    public void testParseAndReformat_productUnit_pow2() throws Exception {
        String unitSymbol = "m/s^2";
        expect(mockQuantityLexicon.getNameByInflection(unitSymbol)).andReturn(null);
        final UnitBlock block1 = new UnitBlock("m");
        final UnitBlock block2 = new UnitBlock(null, "s", "-2");
        expect(mockUnitParser.tagUnit(unitSymbol, false)).andReturn(Arrays.asList(new UnitBlock[]{block1, block2}));

        replay(mockUnitParser, mockQuantityLexicon);
        String reformatted = target.parseAndReformat(unitSymbol, false);
        verify(mockUnitParser, mockQuantityLexicon);
        String expected = "m/s²";

        assertThat(reformatted, is(expected));
    }*/

    @Test
    public void testDecompositionOfBlocks_shouldReturnTheNotation() throws Exception {
        List<UnitBlock> blocks = new ArrayList<>();

        blocks.add(new UnitBlock("k", "m", null));
        blocks.add(new UnitBlock(null, "hours", "-1"));
        
        expect(mockQuantityLexicon.getNameByInflection("hours")).andReturn("h");

        replay(mockQuantityLexicon);

        final List<UnitBlock> result = target.decomposeBlocks(blocks);
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getBase(), is("m"));
        assertThat(result.get(0).getPrefix(), is("k"));
        assertThat(result.get(1).getBase(), is("h"));

        verify(mockQuantityLexicon);
    }


}