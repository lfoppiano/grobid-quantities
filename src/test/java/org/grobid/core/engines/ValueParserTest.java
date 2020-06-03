package org.grobid.core.engines;

import junit.framework.TestCase;
import org.apache.commons.lang3.tuple.Triple;
import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.ValueBlock;
import org.grobid.core.features.FeaturesVectorValues;
import org.grobid.core.layout.LayoutToken;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class ValueParserTest {
    ValueParser target;

    @Before
    public void setUp() throws Exception {
        target = new ValueParser(GrobidModels.DUMMY);
    }

    @Test
    public void testTagValue_exponential_1() throws Exception {
        String input = "0.3 x 10-7";

        List<LayoutToken> layoutTokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutTokenByCharacter(input);

        List<Triple<String, Integer, Integer>> labels = Arrays.asList(
            Triple.of("<number>", 0, 3),
            Triple.of("<base>", 6, 8),
            Triple.of("<pow>", 8, 10)
        );
        String results = getWapitiResult(layoutTokens, labels);

        ValueBlock output = target.resultExtraction(results, layoutTokens);

//        System.out.println(input + " -> " + output);

        assertThat(output.getNumber().toString(), is("0.3"));
        assertThat(output.getBase().toString(), is("10"));
        assertThat(output.getPow().toString(), is("-7"));
        assertThat(output.getRawTaggedValue(), is("<number>0.3</number>x<base>10</base><pow>-7</pow>"));
    }

    /**
     *
     * Current wapiti result:
     * 1	1	1	NOPUNCT	I-<alpha>
     * 0	1	1	NOPUNCT	<number>
     * e	0	0	NOPUNCT	<base>
     * -	1	0	HYPHEN	<pow>
     * 1	1	1	NOPUNCT	<number>
     *
     * @throws Exception
     */
    @Test
    public void testTagValue_exponential_2() throws Exception {
        String input = "10 e -1";

        List<LayoutToken> layoutTokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutTokenByCharacter(input);

        List<Triple<String, Integer, Integer>> labels = Arrays.asList(
            Triple.of("<alpha>", 0, 1),
            Triple.of("<number>", 1, 2),
            Triple.of("<base>", 2, 3),
            Triple.of("<pow>", 3, 4),
            Triple.of("<exp>", 5, 7)
        );
        String results = getWapitiResult(layoutTokens, labels);

        ValueBlock output = target.resultExtraction(results, layoutTokens);

//        System.out.println(input + " -> " + output);

        assertThat(output.getNumber().toString(), is("10"));
        assertThat(output.getExp().toString(), is("-1"));
        assertThat(output.getRawTaggedValue(), is("<alpha>1</alpha><number>0</number><pow>e</pow><exp>-1</exp>"));
    }

    /**
     * Utility method to generate a hypotetical result from wapiti.
     * Useful for testing the extraction of the sequence labeling.
     *
     * @param layoutTokens layout tokens of the initial text
     * @param labels       label maps. A list of Triples, containing label (left), start_index (middle) and end_index exclusive (right)
     * @return a string containing the resulting features + labels returned by wapiti
     */
    public static String getWapitiResult(List<LayoutToken> layoutTokens, List<Triple<String, Integer, Integer>> labels) {

        List<String> features = layoutTokens.stream()
            .map(token -> FeaturesVectorValues.addFeatures(token.getText(), null).printVector())
            .collect(Collectors.toList());

        List<String> labeled = new ArrayList<>();
        int idx = 0;

        for (Triple<String, Integer, Integer> label : labels) {

            if (idx < label.getMiddle()) {
                for (int i = idx; i < label.getMiddle(); i++) {
                    labeled.add("<other>");
                    idx++;
                }
            }

            for (int i = label.getMiddle(); i < label.getRight(); i++) {
                labeled.add(label.getLeft());
                idx++;
            }
        }

        if (idx < features.size()) {
            for (int i = idx; i < features.size(); i++) {
                labeled.add("<other>");
                idx++;
            }
        }

        assertThat(features, hasSize(labeled.size()));

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < features.size(); i++) {
            if (features.get(i).startsWith(" ")) {
                continue;
            }
            sb.append(features.get(i)).append(" ").append(labeled.get(i)).append("\n");
        }

        return sb.toString();
    }

    @Test
    public void testParseValueBlock_simpleNumeric() throws Exception {
        ValueBlock block = new ValueBlock();
        block.setNumber("20");
        final BigDecimal bigDecimal = target.parseValueBlock(block, Locale.ENGLISH);

        assertThat(bigDecimal, is(not(nullValue())));
        assertThat(bigDecimal, is(new BigDecimal("20")));
    }

    @Test
    public void testParseValueBlock_simpleNumericWithBaseAndPow() throws Exception {
        ValueBlock block = new ValueBlock();
        block.setNumber("20");
        block.setPow("-1");
        block.setBase("10");
        final BigDecimal bigDecimal = target.parseValueBlock(block, Locale.ENGLISH);

        assertThat(bigDecimal, is(not(nullValue())));
        assertThat(bigDecimal.intValue(), is(2));
    }

    @Test
    public void testParseValueBlock_simpleNumericWithBase() throws Exception {
        ValueBlock block = new ValueBlock();
        block.setNumber("200");
        block.setBase("10");
        final BigDecimal bigDecimal = target.parseValueBlock(block, Locale.ENGLISH);

        assertThat(bigDecimal, is(not(nullValue())));
        assertThat(bigDecimal.intValue(), is(200));
    }
}