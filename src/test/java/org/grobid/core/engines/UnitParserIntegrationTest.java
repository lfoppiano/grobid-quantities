package org.grobid.core.engines;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import org.grobid.core.data.UnitBlock;
import org.grobid.core.data.normalization.UnitNormalizer;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by lfoppiano on 22.02.16.
 */
@Ignore("Only for manual checks since it depends on the model")
public class UnitParserIntegrationTest {

    UnitParser target;

    @Before
    public void setUp() throws Exception {
        initEngineForTests();
        target = UnitParser.getInstance();
    }

    public static void initEngineForTests() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // https://stackoverflow.com/questions/14853324/can-not-find-deserialize-for-non-concrete-collection-type
        mapper.registerModule(new GuavaModule());
        GrobidQuantitiesConfiguration configuration = mapper.readValue(UnitNormalizer.class.getResourceAsStream("/config-test.yml"), GrobidQuantitiesConfiguration.class);
        configuration.getModels().stream().forEach(GrobidProperties::addModel);
        LibraryLoader.load();
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
    public void testTagUnit13() throws Exception {

        String input = "mol dm–3";
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
}