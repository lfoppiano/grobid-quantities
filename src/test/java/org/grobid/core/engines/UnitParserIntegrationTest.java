package org.grobid.core.engines;

import org.grobid.core.data.UnitBlock;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by lfoppiano on 22.02.16.
 */
@Ignore("Only for manual checks since it depends on the model")
public class UnitParserIntegrationTest {

    UnitParser target;

    @Before
    public void setUp() throws Exception {
        LibraryLoader.load();
        target = UnitParser.getInstance();
    }

    @Test
    public void testTagUnit1() throws Exception {

        String input = "fm/g2";
        List<UnitBlock> output = target.tagUnit(input, false);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(2));
        assertThat(output.get(0).getPrefix(), is("f"));
        assertThat(output.get(0).getBase(), is("m"));
        assertThat(output.get(0).getPow(), is(""));
        assertThat(output.get(1).getPrefix(), is(""));
        assertThat(output.get(1).getBase(), is("g"));
        assertThat(output.get(1).getPow(), is("-2"));
    }

    @Test
    public void testTagUnit2() throws Exception {

        String input = "K";
        List<UnitBlock> output = target.tagUnit(input, false);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(1));
        assertThat(output.get(0).getBase(), is("K"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is(""));
    }

    @Test
    public void testTagUnit3() throws Exception {

        String input = "m2g2";
        List<UnitBlock> output = target.tagUnit(input, false);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(2));
        assertThat(output.get(0).getBase(), is("m"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is("2"));
        assertThat(output.get(1).getBase(), is("g"));
        assertThat(output.get(1).getPrefix(), is(""));
        assertThat(output.get(1).getPow(), is("2"));
    }

    @Test
    public void testTagUnit4() throws Exception {

        String input = "m2/g2";
        List<UnitBlock> output = target.tagUnit(input, false);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(2));
        assertThat(output.get(0).getBase(), is("m"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is("2"));
        assertThat(output.get(1).getBase(), is("g"));
        assertThat(output.get(1).getPrefix(), is(""));
        assertThat(output.get(1).getPow(), is("-2"));
    }

    @Test
    public void testTagUnit5() throws Exception {

        String input = "m22";
        List<UnitBlock> output = target.tagUnit(input, false);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(1));
        assertThat(output.get(0).getBase(), is("m"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is("22"));
    }

    @Test
    public void testTagUnit6() throws Exception {

        String input = "Hz/s";
        List<UnitBlock> output = target.tagUnit(input, false);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(2));
        assertThat(output.get(0).getBase(), is("Hz"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is(""));
        assertThat(output.get(1).getBase(), is("s"));
        assertThat(output.get(1).getPrefix(), is(""));
        assertThat(output.get(1).getPow(), is("-1"));
    }

    @Test
    public void testTagUnit7() throws Exception {

        String input = "Hzs";
        List<UnitBlock> output = target.tagUnit(input, false);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(2));
        assertThat(output.get(0).getBase(), is("Hz"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is(""));
        assertThat(output.get(1).getBase(), is("s"));
        assertThat(output.get(1).getPrefix(), is(""));
        assertThat(output.get(1).getPow(), is(""));
    }

    @Test
    public void testTagUnit8() throws Exception {

        String input = "Db/s";
        List<UnitBlock> output = target.tagUnit(input, false);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(2));
        assertThat(output.get(0).getBase(), is("Db"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is(""));
        assertThat(output.get(1).getBase(), is("s"));
        assertThat(output.get(1).getPrefix(), is(""));
        assertThat(output.get(1).getPow(), is("-1"));
    }

    @Test
    public void testTagUnit9() throws Exception {

        String input = "Dbs";
        List<UnitBlock> output = target.tagUnit(input, false);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(2));
        assertThat(output.get(0).getBase(), is("Db"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is(""));
        assertThat(output.get(1).getBase(), is("s"));
        assertThat(output.get(1).getPrefix(), is(""));
        assertThat(output.get(1).getPow(), is(""));
    }

    @Test
    public void testTagUnit10() throws Exception {

        String input = "Db*s";
        List<UnitBlock> output = target.tagUnit(input, false);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(2));
        assertThat(output.get(0).getBase(), is("Db"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is(""));
        assertThat(output.get(1).getBase(), is("s"));
        assertThat(output.get(1).getPrefix(), is(""));
        assertThat(output.get(1).getPow(), is(""));
    }

    @Test
    public void testTagUnit11() throws Exception {

        String input = "km/h*kg";
        List<UnitBlock> output = target.tagUnit(input, false);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(3));
        assertThat(output.get(0).getBase(), is("m"));
        assertThat(output.get(0).getPrefix(), is("k"));
        assertThat(output.get(0).getPow(), is(""));
        assertThat(output.get(1).getBase(), is("h"));
        assertThat(output.get(1).getPrefix(), is(""));
        assertThat(output.get(1).getPow(), is("-1"));
        assertThat(output.get(2).getBase(), is("g"));
        assertThat(output.get(2).getPrefix(), is("k"));
        assertThat(output.get(2).getPow(), is("-1"));
    }

    @Test
    public void testTagUnit12() throws Exception {

        String input = "m*s^-1";
        List<UnitBlock> output = target.tagUnit(input, false);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(2));
        assertThat(output.get(0).getBase(), is("m"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is(""));
        assertThat(output.get(1).getBase(), is("s"));
        assertThat(output.get(1).getPrefix(), is(""));
        assertThat(output.get(1).getPow(), is("-1"));
    }

    @Test
    public void resultExtraction_kgmm2_liters() throws Exception {
        String result = "k\t0\t0\t1\t1\tNOPUNCT\t0\tI-<prefix>\n" +
                "g\t0\t0\t0\t0\tNOPUNCT\t0\tI-<base>\n" +
                "/\t0\t0\t1\t0\tNOPUNCT\t0\tI-<pow>\n" +
                "m\t1\t0\t0\t0\tSLASH\t0\tI-<prefix>\n" +
                "m\t0\t0\t1\t0\tNOPUNCT\t0\tI-<base>\n"+
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

        List<UnitBlock> blocks = target.resultExtraction(result, generateTokenisation("°C/h"));
        assertThat(blocks.size(), is(2));
        assertThat(blocks.get(0).getBase(), is("°C"));
        assertThat(blocks.get(1).getBase(), is("h"));
        assertThat(blocks.get(1).getPow(), is("-1"));
    }

    public static List<LayoutToken> generateTokenisation(String input) {
        List<LayoutToken> tokenisation = new ArrayList<>();

        final char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            tokenisation.add(new LayoutToken(String.valueOf(chars[i])));
        }

        return tokenisation;
    }
}