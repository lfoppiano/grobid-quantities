package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.UnitBlock;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.GrobidConfig;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UnitParserTest {
    private UnitParser target;

    @Before
    public void setUp() throws Exception {
        GrobidConfig.ModelParameters modelParameters = new GrobidConfig.ModelParameters();
        modelParameters.name = "bao";
        GrobidProperties.addModel(modelParameters);
        target = new UnitParser(GrobidModels.DUMMY, null);
    }

    @Test
    public void resultExtraction_kgmm2_liters() throws Exception {
        String result = "k\t0\t0\t1\t1\tNOPUNCT\t0\tI-<prefix>\n" +
            "g\t0\t0\t0\t0\tNOPUNCT\t0\tI-<base>\n" +
            "/\t0\t0\t1\t0\tNOPUNCT\t0\tI-<pow>\n" +
            "m\t1\t0\t0\t0\tSLASH\t0\tI-<prefix>\n" +
            "m\t0\t0\t1\t0\tNOPUNCT\t0\tI-<base>\n" +
            "2\t0\t0\t1\t0\tNOPUNCT\t0\tI-<pow>";

        List<UnitBlock> blocks = target.resultExtraction(result, generateTokenisation("kg/mm2"));
        assertThat(blocks.size(), is(2));
        assertThat(blocks.get(0).getPrefix(), is("k"));
        assertThat(blocks.get(0).getBase(), is("g"));
        assertThat(blocks.get(1).getPrefix(), is("m"));
        assertThat(blocks.get(1).getBase(), is("m"));
        assertThat(blocks.get(1).getPow(), is("-2"));
    }

    @Test
    public void resultExtraction_mol_divided_liters() throws Exception {
        String result = "m\t0\t0\t1\t1\tNOPUNCT\t0\tI-<base>\n" +
            "o\t0\t0\t0\t0\tNOPUNCT\t0\t<base>\n" +
            "l\t0\t0\t1\t0\tNOPUNCT\t0\t<base>\n" +
            "/\t1\t0\t0\t0\tSLASH\t0\tI-<pow>\n" +
            "l\t0\t0\t1\t0\tNOPUNCT\t0\tI-<base>";

        List<UnitBlock> blocks = target.resultExtraction(result, generateTokenisation("mol/l"));
        assertThat(blocks.size(), is(2));
        assertThat(blocks.get(0).getBase(), is("mol"));
        assertThat(blocks.get(1).getBase(), is("l"));
        assertThat(blocks.get(1).getPow(), is("-1"));
    }

    @Test
    public void resultExtraction_C_divided_hours() throws Exception {
        String result = "°\t0\t0\t1\t1\tNOPUNCT\t0\tI-<base>\n" +
            "C\t0\t0\t0\t0\tNOPUNCT\t0\t<base>\n" +
            "/\t0\t0\t1\t0\tNOPUNCT\t0\tI-<pow>\n" +
            "h\t1\t0\t0\t0\tSLASH\t0\tI-<base>";

        List<UnitBlock> blocks = target.resultExtraction(result, generateTokenisation("°C /h"));
        assertThat(blocks.size(), is(2));
        assertThat(blocks.get(0).getBase(), is("°C"));
        assertThat(blocks.get(1).getBase(), is("h"));
        assertThat(blocks.get(1).getPow(), is("-1"));
        assertThat(blocks.get(0).getRawTaggedValue(), is("<base>°C</base> <pow>/</pow><base>h</base>"));
        assertThat(blocks.get(1).getRawTaggedValue(), is("<base>°C</base> <pow>/</pow><base>h</base>"));
    }

    public static List<LayoutToken> generateTokenisation(String input) {
        List<LayoutToken> tokenisation = new ArrayList<>();

        final char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            tokenisation.add(new LayoutToken(String.valueOf(chars[i])));
        }

        return tokenisation;
    }

    @Test
    public void resultExtraction_speed() throws Exception {
        String result = "k\t0\t0\t1\t1\tNOPUNCT\t0\tI-<prefix>\n" +
            "m\t0\t0\t1\t1\tNOPUNCT\t0\tI-<base>\n" +
            "/\t1\t0\t0\t0\tSLASH\t0\tI-<pow>\n" +
            "s\t0\t0\t1\t0\tNOPUNCT\t0\tI-<base>";

        List<UnitBlock> blocks = target.resultExtraction(result, generateTokenisation("km / s"));
        assertThat(blocks.size(), is(2));
        assertThat(blocks.get(0).getPrefix(), is("k"));
        assertThat(blocks.get(0).getBase(), is("m"));
        assertThat(blocks.get(1).getBase(), is("s"));
        assertThat(blocks.get(1).getPow(), is("-1"));
        assertThat(blocks.get(0).getRawTaggedValue(), is("<prefix>k</prefix><base>m</base> <pow>/</pow> <base>s</base>"));
        assertThat(blocks.get(1).getRawTaggedValue(), is("<prefix>k</prefix><base>m</base> <pow>/</pow> <base>s</base>"));
    }


    @Test
    public void resultExtraction_wrong() throws Exception {
        String result = "a\t0\t0\t1\t1\tNOPUNCT\t0\tI-<base>\n" +
            "u\t0\t0\t0\t0\tNOPUNCT\t0\t<base>\n" +
            "/\t1\t0\t0\t0\tSLASH\t0\tI-<pow>\n" +
            "d\t0\t0\t1\t1\tNOPUNCT\t0\tI-<base>\n" +
            "2\t1\t1\t0\t0\tNOPUNCT\t0\tI-<pow>";

        List<UnitBlock> blocks = target.resultExtraction(result, generateTokenisation("au/d 2"));
        assertThat(blocks.size(), is(2));
        assertThat(blocks.get(0).getPrefix(), is(""));
        assertThat(blocks.get(0).getBase(), is("au"));
        assertThat(blocks.get(1).getBase(), is("d"));
        assertThat(blocks.get(1).getPow(), is("-2"));
        assertThat(blocks.get(0).getRawTaggedValue(), is("<base>au</base><pow>/</pow><base>d</base> <pow>2</pow>"));
        assertThat(blocks.get(1).getRawTaggedValue(), is("<base>au</base><pow>/</pow><base>d</base> <pow>2</pow>"));
    }


    @Test
    public void resultExtraction_wrong2() throws Exception {
        String result = "g\t0\t0\t1\t0\tNOPUNCT\t0\tI-<base>\n" +
            "·\t1\t0\t0\t0\tDOT\t0\t<other>\n" +
            "k\t0\t0\t1\t1\tNOPUNCT\t0\tI-<prefix>\n" +
            "g\t0\t0\t1\t0\tNOPUNCT\t0\tI-<base>\n" +
            "−\t1\t0\t0\t0\tHYPHEN\t0\tI-<pow>\n" +
            "1\t1\t1\t0\t0\tNOPUNCT\t0\t<pow>\n" +
            "·\t1\t0\t0\t0\tDOT\t0\t<other>\n" +
            "d\t0\t0\t1\t1\tNOPUNCT\t0\tI-<base>\n" +
            "a\t0\t0\t1\t1\tNOPUNCT\t0\t<base>\n" +
            "y\t0\t0\t1\t1\tNOPUNCT\t0\t<base>\n" +
            "−\t1\t0\t0\t0\tHYPHEN\t0\tI-<pow>\n" +
            "1\t1\t1\t0\t0\tNOPUNCT\t0\t<pow>";

        List<UnitBlock> blocks = target.resultExtraction(result, generateTokenisation("g · kg −1 · day −1"));
        assertThat(blocks.size(), is(3));
        assertThat(blocks.get(0).getPrefix(), is(""));
        assertThat(blocks.get(0).getBase(), is("g"));
        assertThat(blocks.get(1).getPrefix(), is("k"));
        assertThat(blocks.get(1).getBase(), is("g"));
        assertThat(blocks.get(1).getPow(), is("−1"));
        assertThat(blocks.get(2).getBase(), is("day"));
        assertThat(blocks.get(2).getPow(), is("−1"));
        assertThat(blocks.get(0).getRawTaggedValue(), is("<base>g</base> · <prefix>k</prefix><base>g</base> <pow>−1</pow> · <base>day</base> <pow>−1</pow>"));
        assertThat(blocks.get(1).getRawTaggedValue(), is("<base>g</base> · <prefix>k</prefix><base>g</base> <pow>−1</pow> · <base>day</base> <pow>−1</pow>"));
        assertThat(blocks.get(2).getRawTaggedValue(), is("<base>g</base> · <prefix>k</prefix><base>g</base> <pow>−1</pow> · <base>day</base> <pow>−1</pow>"));
    }
}