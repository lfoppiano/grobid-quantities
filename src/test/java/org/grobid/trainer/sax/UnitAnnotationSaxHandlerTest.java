package org.grobid.trainer.sax;

import org.grobid.core.utilities.Pair;
import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created by lfoppiano on 21.02.16.
 */
public class UnitAnnotationSaxHandlerTest {

    private UnitAnnotationSaxHandler target;
    SAXParserFactory spf = SAXParserFactory.newInstance();

    @Before
    public void setUp() {
        target = new UnitAnnotationSaxHandler();
    }

    @Test
    public void testHandler1() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/xml/trainingdata1.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<Pair<String, String>> labeled = target.getLabeledResult();

        assertThat(labeled.size(), is(28));


    }

    @Test
    public void testHandler2() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/xml/trainingdata2.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<Pair<String, String>> labeled = target.getLabeledResult();

        assertThat(labeled.size(), is(15));


    }
}