package org.grobid.core.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 10/03/16.
 */
public class UnitTrainingDataGeneratorTest {

    private UnitTrainingDataGenerator target;

    @Before
    public void setUp() throws Exception {
        target = new UnitTrainingDataGenerator();
    }

    @Test
    public void testProcessLine_notComposed() throws Exception {

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("k", "kilo");
        prefixes.put("m", "milli");

        Map<String, List<String>> inflections = new HashMap<>();
        List<String> meterInflections = new ArrayList<>();
        meterInflections.add("meters");
        inflections.put("meter", meterInflections);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        target.processLine("m\tLENGTH\tSI_BASE\tmetre,meter", writer, prefixes, inflections);

        assertThat(stringWriter.toString(), is("<unit><base>m</base></unit>\n" +
                "<unit><prefix>k</prefix><base>m</base></unit>\n" +
                "<unit><prefix>m</prefix><base>m</base></unit>\n"));

    }

    @Test
    public void testProcessLine_Composed1() throws Exception {

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("k", "kilo");
        prefixes.put("m", "milli");

        Map<String, List<String>> inflections = new HashMap<>();
        List<String> meterInflections = new ArrayList<>();
        meterInflections.add("meters");
        inflections.put("meter", meterInflections);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        target.processLine("mol/m^3,mol/m³\tCONCENTRATION\tSI_BASE\tmolarity", writer, prefixes, inflections);

        assertThat(stringWriter.toString(), startsWith("<unit><base>mol</base><pow>/</pow><base>m</base><pow>^3</pow></unit>"));

    }

    @Test
    public void testProcessLine_Composed2() throws Exception {

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("k", "kilo");
        prefixes.put("m", "milli");

        Map<String, List<String>> inflections = new HashMap<>();
        List<String> meterInflections = new ArrayList<>();
        meterInflections.add("meters");
        inflections.put("meter", meterInflections);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        target.processLine("V/m\tELECTRIC_FIELD\tSI_DERIVED\tvolt per metre,volt per meter", writer, prefixes, inflections);

        assertThat(stringWriter.toString(), startsWith("<unit><base>V</base><pow>/</pow><base>m</base></unit>\n"));

    }

    @Test
    public void testProcessNode_notComposed() throws Exception {

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("k", "kilo");
        prefixes.put("m", "milli");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        String json = "{\n" +
                "      \"notations\": [\n" +
                "        {\n" +
                "          \"raw\": \"m\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"type\": \"LENGTH\",\n" +
                "      \"system\": \"SI_BASE\",\n" +
                "      \"names\": [\n" +
                "        {\n" +
                "          \"lemma\": \"metre\",\n" +
                "          \"inflections\": [\n" +
                "            \"metres\"\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"lemma\": \"meter\",\n" +
                "          \"inflections\": [\n" +
                "            \"meters\"\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }";


        JsonNode jsonNode = new ObjectMapper().readTree(json);
        target.processNode(jsonNode, writer, prefixes);

        assertThat(stringWriter.toString(), is("<unit><base>m</base></unit>\n" +
                "<unit><prefix>k</prefix><base>m</base></unit>\n" +
                "<unit><prefix>m</prefix><base>m</base></unit>\n"));

    }

    @Test
    public void testProcessNode_Composed1() throws Exception {

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("k", "kilo");
        prefixes.put("m", "milli");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        String json = "{\n" +
                "      \"notations\": [\n" +
                "        {\n" +
                "          \"raw\": \"M\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"raw\": \"mol/m^3\",\n" +
                "          \"decomposition\": [\n" +
                "            {\n" +
                "              \"base\": \"mol\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"op\": \"/\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"base\": \"m\",\n" +
                "              \"pow\": \"3\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"product\": [\n" +
                "            {\n" +
                "              \"base\": \"mol\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"base\": \"m\",\n" +
                "              \"pow\": \"-3\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"raw\": \"mol/L\",\n" +
                "          \"decomposition\": [\n" +
                "            {\n" +
                "              \"base\": \"mol\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"op\": \"/\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"pow\": \"L\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"product\": [\n" +
                "            {\n" +
                "              \"base\": \"mol\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"base\": \"L\",\n" +
                "              \"pow\": \"-1\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"raw\": \"mol/m³\",\n" +
                "          \"decomposition\": [\n" +
                "            {\n" +
                "              \"base\": \"mol\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"op\": \"/\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"base\": \"m\",\n" +
                "              \"pow\": \"³\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"product\": [\n" +
                "            {\n" +
                "              \"base\": \"mol\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"base\": \"m\",\n" +
                "              \"pow\": \"-3\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"type\": \"CONCENTRATION\",\n" +
                "      \"system\": \"NON_SI\",\n" +
                "      \"names\": [\n" +
                "        {\n" +
                "          \"lemma\": \"molarity\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"lemma\": \"amount concentration\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"lemma\": \"substance concentration\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }";

        JsonNode jsonNode = new ObjectMapper().readTree(json);
        target.processNode(jsonNode, writer, prefixes);

        assertThat(stringWriter.toString(), startsWith("<unit><base>M</base></unit>\n<unit><base>mol</base><pow>/</pow><base>m</base><pow>^3</pow></unit>"));

    }

    @Test
    public void testProcessNode_Composed2() throws Exception {

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("k", "kilo");
        prefixes.put("m", "milli");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        String json = "{\"notations\": [{\"raw\": \"V/m\"}],\"type\": \"ELECTRIC_FIELD\",\"system\": \"SI_DERIVED\",\"names\": [ {\"lemma\": \"volt per meter\",\"inflections\": [\"volts per meter\"]}, {\"lemma\": \"volt per metre\",\"inflections\": [\"volts per metre\"]}]}";

        JsonNode jsonNode = new ObjectMapper().readTree(json);
        target.processNode(jsonNode, writer, prefixes);

        assertThat(stringWriter.toString(), startsWith("<unit><base>V</base><pow>/</pow><base>m</base></unit>\n"));

    }

    @Test
    public void testAppendUnit_onlyBase1_simple() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        target.appendUnit(writer, null, "m", null, null, null);

        assertThat(stringWriter.toString(), is("<unit><base>m</base></unit>\n"));
    }

    @Test
    public void testAppendUnit_onlyBase1_composed() throws Exception {
        StringWriter stringWriter = new StringWriter();

        PrintWriter writer = new PrintWriter(stringWriter);


        target.appendUnit(writer, null, "m^2", null, null, null);

        assertThat(stringWriter.toString(), is("<unit><base>m</base><pow>^2</pow></unit>\n"));
    }

    @Test
    public void testAppendUnit_composed() throws Exception {
        StringWriter stringWriter = new StringWriter();

        PrintWriter writer = new PrintWriter(stringWriter);


        target.appendUnit(writer, "k", "m^2", "/", "m", "s2");

        assertThat(stringWriter.toString(), is("<unit><prefix>k</prefix><base>m</base><pow>^2</pow><pow>/</pow><prefix>m</prefix><base>s</base><pow>2</pow></unit>\n"));
    }

    @Test
    public void testSeparateBaseAndPow_notPow() throws Exception {

        Pair<String, String> result = target.separateBaseAndPow("m");

        assertThat(result.getA(), is("m"));
        assertThat(result.getB(), is(""));
    }

    @Test
    public void testSeparateBaseAndPow_pow1() throws Exception {

        Pair<String, String> result = target.separateBaseAndPow("m^2");

        assertThat(result.getA(), is("m"));
        assertThat(result.getB(), is("^2"));
    }

    @Test
    public void testSeparateBaseAndPow_pow2() throws Exception {

        Pair<String, String> result = target.separateBaseAndPow("m³");

        assertThat(result.getA(), is("m"));
        assertThat(result.getB(), is("³"));
    }

    @Test
    public void testSeparateBaseAndPow_pow3() throws Exception {

        Pair<String, String> result = target.separateBaseAndPow("m⁻¹");

        assertThat(result.getA(), is("m"));
        assertThat(result.getB(), is("⁻¹"));
    }

    @Test
    public void testSeparateBaseAndPow_pow4() throws Exception {

        Pair<String, String> result = target.separateBaseAndPow("cm⁻¹");

        assertThat(result.getA(), is("cm"));
        assertThat(result.getB(), is("⁻¹"));
    }
}
