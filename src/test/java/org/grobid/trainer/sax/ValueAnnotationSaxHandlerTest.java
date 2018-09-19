package org.grobid.trainer.sax;

import org.grobid.trainer.ValueLabeled;
import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created by lfoppiano
 */
public class ValueAnnotationSaxHandlerTest {

    private ValueAnnotationSaxHandler target;
    SAXParserFactory spf = SAXParserFactory.newInstance();

    @Before
    public void setUp() {
        target = new ValueAnnotationSaxHandler();
    }

    @Test
    public void testParsing_1_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/xml/trainingdata3.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<ValueLabeled> labeled = target.getLabeledResult();

        assertThat(labeled, hasSize(10));

        final ValueLabeled valueLabeled = labeled.get(0);
        assertThat(valueLabeled.getLabels(), hasSize(5));
        assertThat(valueLabeled.getLabels().get(0).a, is("2"));
        assertThat(valueLabeled.getLabels().get(0).b, is("I-<number>"));
        assertThat(valueLabeled.getLabels().get(1).a, is("x"));
        assertThat(valueLabeled.getLabels().get(1).b, is("<other>"));
        assertThat(valueLabeled.getLabels().get(2).a, is("1"));
        assertThat(valueLabeled.getLabels().get(2).b, is("I-<base>"));
        assertThat(valueLabeled.getLabels().get(3).a, is("0"));
        assertThat(valueLabeled.getLabels().get(3).b, is("<base>"));
        assertThat(valueLabeled.getLabels().get(4).a, is("3"));
        assertThat(valueLabeled.getLabels().get(4).b, is("I-<pow>"));
    }

    @Test
    public void testParsing_2_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/xml/trainingdata3.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<ValueLabeled> labeled = target.getLabeledResult();

        final ValueLabeled valueLabeled = labeled.get(1);
        assertThat(valueLabeled.getLabels(), hasSize(6));
        assertThat(valueLabeled.getLabels().get(0).a, is("0"));
        assertThat(valueLabeled.getLabels().get(0).b, is("I-<number>"));
        assertThat(valueLabeled.getLabels().get(1).a, is("."));
        assertThat(valueLabeled.getLabels().get(1).b, is("<number>"));
        assertThat(valueLabeled.getLabels().get(2).a, is("2"));
        assertThat(valueLabeled.getLabels().get(2).b, is("<number>"));
        assertThat(valueLabeled.getLabels().get(3).a, is("E"));
        assertThat(valueLabeled.getLabels().get(3).b, is("<other>"));
        assertThat(valueLabeled.getLabels().get(4).a, is("-"));
        assertThat(valueLabeled.getLabels().get(4).b, is("I-<exp>"));
        assertThat(valueLabeled.getLabels().get(5).a, is("3"));
        assertThat(valueLabeled.getLabels().get(5).b, is("<exp>"));
    }

    @Test
    public void testParsing_3_shouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/xml/trainingdata3.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<ValueLabeled> labeled = target.getLabeledResult();

        final ValueLabeled valueLabeled = labeled.get(5);
        assertThat(valueLabeled.getLabels(), hasSize(9));
        assertThat(valueLabeled.getLabels().get(0).a, is("t"));
        assertThat(valueLabeled.getLabels().get(0).b, is("I-<alpha>"));
        assertThat(valueLabeled.getLabels().get(1).a, is("w"));
        assertThat(valueLabeled.getLabels().get(1).b, is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(2).a, is("e"));
        assertThat(valueLabeled.getLabels().get(2).b, is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(3).a, is("n"));
        assertThat(valueLabeled.getLabels().get(3).b, is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(4).a, is("t"));
        assertThat(valueLabeled.getLabels().get(4).b, is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(5).a, is("y"));
        assertThat(valueLabeled.getLabels().get(5).b, is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(6).a, is("t"));
        assertThat(valueLabeled.getLabels().get(6).b, is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(7).a, is("w"));
        assertThat(valueLabeled.getLabels().get(7).b, is("<alpha>"));
        assertThat(valueLabeled.getLabels().get(8).a, is("o"));
        assertThat(valueLabeled.getLabels().get(8).b, is("<alpha>"));
    }

}