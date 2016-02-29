package org.grobid.core.engines;

import org.grobid.core.data.Unit;
import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.SchemaOutputResolver;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by lfoppiano on 22.02.16.
 */
@Ignore("Only for manual checks since it depends on the model")
public class UnitParserTest {

    UnitParser target;

    @Before
    public void setUp() throws Exception {
        LibraryLoader.load();
        target = UnitParser.getInstance();
    }

    @Test
    public void testTagUnit1() throws Exception {

        String input = "fm/g2";
        List<Unit.UnitBlock> output = target.tagUnit(input);
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
        List<Unit.UnitBlock> output = target.tagUnit(input);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(1));
        assertThat(output.get(0).getBase(), is("K"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is(""));
    }

    @Test
    public void testTagUnit3() throws Exception {

        String input = "m2g2";
        List<Unit.UnitBlock> output = target.tagUnit(input);
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
        List<Unit.UnitBlock> output = target.tagUnit(input);
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
        List<Unit.UnitBlock> output = target.tagUnit(input);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(1));
        assertThat(output.get(0).getBase(), is("m"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is("22"));
    }

    @Test
    public void testTagUnit6() throws Exception {

        String input = "hZ/s";
        List<Unit.UnitBlock> output = target.tagUnit(input);
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

        String input = "hZs";
        List<Unit.UnitBlock> output = target.tagUnit(input);
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
        List<Unit.UnitBlock> output = target.tagUnit(input);
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
        List<Unit.UnitBlock> output = target.tagUnit(input);
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
        List<Unit.UnitBlock> output = target.tagUnit(input);
        System.out.println(input + " -> " + output);

        assertThat(output.size(), is(2));
        assertThat(output.get(0).getBase(), is("Db"));
        assertThat(output.get(0).getPrefix(), is(""));
        assertThat(output.get(0).getPow(), is(""));
        assertThat(output.get(1).getBase(), is("s"));
        assertThat(output.get(1).getPrefix(), is(""));
        assertThat(output.get(1).getPow(), is(""));
    }


}