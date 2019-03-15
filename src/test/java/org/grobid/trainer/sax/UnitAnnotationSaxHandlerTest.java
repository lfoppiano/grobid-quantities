package org.grobid.trainer.sax;

import org.apache.commons.io.IOUtils;
import org.grobid.trainer.UnitLabeled;
import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class UnitAnnotationSaxHandlerTest {

    private UnitAnnotationSaxHandler target;
    SAXParserFactory spf = SAXParserFactory.newInstance();

    @Before
    public void setUp() {
        target = new UnitAnnotationSaxHandler();
    }

    @Test
    public void testHandler1() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("trainingdata.sample.units.1.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<UnitLabeled> labeled = target.getLabeledResult();

        assertThat(labeled.size(), is(4));

        assertThat(labeled.get(0).isUnitLeft(), is(true));
        assertThat(labeled.get(0).getLabels().size(), is(5));
    }

    @Test
    public void testHandler2() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("trainingdata.sample.units.2.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<UnitLabeled> labeled = target.getLabeledResult();

        assertThat(labeled.size(), is(2));
        assertThat(labeled.get(0).isUnitLeft(), is(false));
        assertThat(labeled.get(0).getLabels().size(), is(5));
    }

    @Test
    public void testParser_doubleBaseName() throws Exception {
        String input = "<units><unit left=\"true\"><base>Hz</base></unit></units>";
        InputStream is = IOUtils.toInputStream(input, UTF_8);

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<UnitLabeled> labeled = target.getLabeledResult();

        assertThat(labeled.size(), is(1));
        assertThat(labeled.get(0).isUnitLeft(), is(true));
        assertThat(labeled.get(0).getLabels().size(), is(2));
        assertThat(labeled.get(0).getLabels().get(0).getA(), is("H"));
        assertThat(labeled.get(0).getLabels().get(0).getB(), is("I-<base>"));
        assertThat(labeled.get(0).getLabels().get(1).getA(), is("z"));
        assertThat(labeled.get(0).getLabels().get(1).getB(), is("<base>"));
    }
}