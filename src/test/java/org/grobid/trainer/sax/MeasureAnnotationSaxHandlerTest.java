package org.grobid.trainer.sax;

import org.apache.commons.io.IOUtils;
import org.grobid.core.utilities.Pair;
import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MeasureAnnotationSaxHandlerTest {

    private MeasureAnnotationSaxHandler target;
    SAXParserFactory spf = SAXParserFactory.newInstance();

    @Before
    public void setUp() {
        target = new MeasureAnnotationSaxHandler();
    }

    private List<Pair<String, String>> parse(String input) throws Exception {
        InputStream is = IOUtils.toInputStream(input, UTF_8);
        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        // drop the paragraph separators, they carry no label
        List<Pair<String, String>> result = new ArrayList<>();
        for (Pair<String, String> pair : target.getLabeledResult()) {
            if (!"\n".equals(pair.getA())) {
                result.add(pair);
            }
        }
        return result;
    }

    private void assertLabel(List<Pair<String, String>> labeled, int index, String token, String label) {
        assertThat(labeled.get(index).getA(), is(token));
        assertThat(labeled.get(index).getB(), is(label));
    }

    @Test
    public void testAtomicValue_shouldMarkTheBeginningOfEachEntity() throws Exception {
        List<Pair<String, String>> labeled = parse("<tei><text><p>" +
            "<measure type=\"value\"><num>10</num> <measure type=\"MASS\" unit=\"kg\">kg</measure></measure>" +
            "</p></text></tei>");

        assertLabel(labeled, 0, "10", "I-<valueAtomic>");
        assertLabel(labeled, 1, "kg", "I-<unitLeft>");
    }

    /**
     * See https://github.com/lfoppiano/grobid-quantities/issues/177: the first token of
     * <valueRange> was labelled as a continuation, so the entity had no beginning.
     */
    @Test
    public void testBaseRangeInterval_shouldMarkTheBeginningOfTheRange() throws Exception {
        List<Pair<String, String>> labeled = parse("<tei><text><p>" +
            "<measure type=\"interval\"><num type=\"base\">65.5</num> ± <num type=\"range\">0.8</num></measure>" +
            "</p></text></tei>");

        assertLabel(labeled, 0, "65", "I-<valueBase>");
        assertLabel(labeled, 1, ".", "<valueBase>");
        assertLabel(labeled, 2, "5", "<valueBase>");
        assertLabel(labeled, 3, "±", "<other>");
        assertLabel(labeled, 4, "0", "I-<valueRange>");
        assertLabel(labeled, 5, ".", "<valueRange>");
        assertLabel(labeled, 6, "8", "<valueRange>");
    }

    /**
     * See https://github.com/lfoppiano/grobid-quantities/issues/177: the <unitLeft> following
     * a base/range value was labelled as a continuation too.
     */
    @Test
    public void testBaseRangeInterval_shouldMarkTheBeginningOfEachUnit() throws Exception {
        List<Pair<String, String>> labeled = parse("<tei><text><p>" +
            "<measure type=\"interval\">" +
            "<num type=\"base\">135</num><measure type=\"LENGTH\" unit=\"km\">km</measure>" +
            " ± " +
            "<num type=\"range\">5</num><measure type=\"LENGTH\" unit=\"km\">km</measure>" +
            "</measure>" +
            "</p></text></tei>");

        assertLabel(labeled, 0, "135", "I-<valueBase>");
        assertLabel(labeled, 1, "km", "I-<unitLeft>");
        assertLabel(labeled, 2, "±", "<other>");
        assertLabel(labeled, 3, "5", "I-<valueRange>");
        assertLabel(labeled, 4, "km", "I-<unitLeft>");
    }

    @Test
    public void testTwoConsecutiveIntervals_shouldMarkTheBeginningOfEachEntity() throws Exception {
        List<Pair<String, String>> labeled = parse("<tei><text><p>" +
            "<measure type=\"interval\"><num type=\"base\">3</num> ± <num type=\"range\">1</num></measure>" +
            " and " +
            "<measure type=\"interval\"><num type=\"base\">4</num> ± <num type=\"range\">2</num></measure>" +
            "</p></text></tei>");

        assertLabel(labeled, 0, "3", "I-<valueBase>");
        assertLabel(labeled, 2, "1", "I-<valueRange>");
        assertLabel(labeled, 3, "and", "<other>");
        assertLabel(labeled, 4, "4", "I-<valueBase>");
        assertLabel(labeled, 6, "2", "I-<valueRange>");
    }
}
