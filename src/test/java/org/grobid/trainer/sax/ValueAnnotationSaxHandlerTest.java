package org.grobid.trainer.sax;

import org.apache.commons.io.IOUtils;
import org.grobid.trainer.ValueLabeled;
import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class ValueAnnotationSaxHandlerTest {

    private ValueAnnotationSaxHandler target;
    SAXParserFactory spf = SAXParserFactory.newInstance();

    @Before
    public void setUp() {
        target = new ValueAnnotationSaxHandler();
    }

    @Test
    public void testParsing_1_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("trainingdata.sample.values.1.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<ValueLabeled> labeled = target.getLabeledResult();

        assertThat(labeled, hasSize(10));

        final ValueLabeled valueLabeled = labeled.get(0);
        assertThat(valueLabeled.getLabels(), hasSize(5));
        assertThat(valueLabeled.getLabels().get(0).getLeft(), is("2"));
        assertThat(valueLabeled.getLabels().get(0).getRight(), is("I-<number>"));
        assertThat(valueLabeled.getLabels().get(1).getLeft(), is("x"));
        assertThat(valueLabeled.getLabels().get(1).getRight(), is("<other>"));
        assertThat(valueLabeled.getLabels().get(2).getLeft(), is("1"));
        assertThat(valueLabeled.getLabels().get(2).getRight(), is("I-<base>"));
        assertThat(valueLabeled.getLabels().get(3).getLeft(), is("0"));
        assertThat(valueLabeled.getLabels().get(3).getRight(), is("<base>"));
        assertThat(valueLabeled.getLabels().get(4).getLeft(), is("3"));
        assertThat(valueLabeled.getLabels().get(4).getRight(), is("I-<pow>"));
    }

    @Test
    public void testParsing_2_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("trainingdata.sample.values.1.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<ValueLabeled> labeled = target.getLabeledResult();

        final ValueLabeled valueLabeled = labeled.get(1);
        assertThat(valueLabeled.getLabels(), hasSize(6));
        assertThat(valueLabeled.getLabels().get(0).getLeft(), is("0"));
        assertThat(valueLabeled.getLabels().get(0).getRight(), is("I-<number>"));
        assertThat(valueLabeled.getLabels().get(1).getLeft(), is("."));
        assertThat(valueLabeled.getLabels().get(1).getRight(), is("<number>"));
        assertThat(valueLabeled.getLabels().get(2).getLeft(), is("2"));
        assertThat(valueLabeled.getLabels().get(2).getRight(), is("<number>"));
        assertThat(valueLabeled.getLabels().get(3).getLeft(), is("E"));
        assertThat(valueLabeled.getLabels().get(3).getRight(), is("<other>"));
        assertThat(valueLabeled.getLabels().get(4).getLeft(), is("-"));
        assertThat(valueLabeled.getLabels().get(4).getRight(), is("I-<exp>"));
        assertThat(valueLabeled.getLabels().get(5).getLeft(), is("3"));
        assertThat(valueLabeled.getLabels().get(5).getRight(), is("<exp>"));
    }

    @Test
    public void testParsing_3_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("trainingdata.sample.values.1.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<ValueLabeled> labeled = target.getLabeledResult();

        final ValueLabeled valueLabeled = labeled.get(5);
        assertThat(valueLabeled.getLabels(), hasSize(9));
        assertThat(valueLabeled.getLabels().get(0).getLeft(), is("t"));
        assertThat(valueLabeled.getLabels().get(0).getRight(), is("I-<alpha>"));
        assertThat(valueLabeled.getLabels().get(1).getLeft(), is("w"));
        assertThat(valueLabeled.getLabels().get(1).getRight(), is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(2).getLeft(), is("e"));
        assertThat(valueLabeled.getLabels().get(2).getRight(), is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(3).getLeft(), is("n"));
        assertThat(valueLabeled.getLabels().get(3).getRight(), is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(4).getLeft(), is("t"));
        assertThat(valueLabeled.getLabels().get(4).getRight(), is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(5).getLeft(), is("y"));
        assertThat(valueLabeled.getLabels().get(5).getRight(), is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(6).getLeft(), is("t"));
        assertThat(valueLabeled.getLabels().get(6).getRight(), is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(7).getLeft(), is("w"));
        assertThat(valueLabeled.getLabels().get(7).getRight(), is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(8).getLeft(), is("o"));
        assertThat(valueLabeled.getLabels().get(8).getRight(), is("<alpha>"));
    }

    @Test
    public void testParsing_character_in_the_middle() throws Exception {
        String input = "<value><number>2</number> · <base>10</base> <pow>10</pow></value>";
        InputStream is = IOUtils.toInputStream(input, UTF_8);

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<ValueLabeled> labeled = target.getLabeledResult();

        assertThat(labeled, hasSize(1));

        final ValueLabeled valueLabeled = labeled.get(0);
        assertThat(valueLabeled.getLabels(), hasSize(6));
        assertThat(valueLabeled.getLabels().get(0).getLeft(), is("2"));
        assertThat(valueLabeled.getLabels().get(0).getRight(), is("I-<number>"));
        assertThat(valueLabeled.getLabels().get(1).getLeft(), is("·"));
        assertThat(valueLabeled.getLabels().get(1).getRight(), is("<other>"));
        assertThat(valueLabeled.getLabels().get(2).getLeft(), is("1"));
        assertThat(valueLabeled.getLabels().get(2).getRight(), is("I-<base>"));
        assertThat(valueLabeled.getLabels().get(3).getLeft(), is("0"));
        assertThat(valueLabeled.getLabels().get(3).getRight(), is("<base>"));
        assertThat(valueLabeled.getLabels().get(4).getLeft(), is("1"));
        assertThat(valueLabeled.getLabels().get(4).getRight(), is("I-<pow>"));
        assertThat(valueLabeled.getLabels().get(5).getLeft(), is("0"));
        assertThat(valueLabeled.getLabels().get(5).getRight(), is("<pow>"));
    }

}