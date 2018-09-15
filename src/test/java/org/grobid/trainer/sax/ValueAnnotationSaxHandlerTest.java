package org.grobid.trainer.sax;

import org.apache.commons.io.IOUtils;
import org.grobid.core.utilities.Pair;
import org.grobid.trainer.UnitLabeled;
import org.grobid.trainer.ValueLabeled;
import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
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
    public void testHandler1() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/xml/trainingdata3.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<ValueLabeled> labeled = target.getLabeledResult();

        assertThat(labeled.size(), is(8));

        assertThat(labeled.get(0).getLabels().size(), is(5));
    }
    
}