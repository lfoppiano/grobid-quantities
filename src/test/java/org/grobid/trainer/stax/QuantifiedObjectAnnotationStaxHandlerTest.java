package org.grobid.trainer.stax;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.exceptions.GrobidException;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class QuantifiedObjectAnnotationStaxHandlerTest {
    private QuantifiedObjectAnnotationStaxHandler target;

    private WstxInputFactory inputFactory = new WstxInputFactory();

    @Before
    public void setUp() {
        target = new QuantifiedObjectAnnotationStaxHandler();
    }

    @Test
    public void testHandler_simpleCase() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("trainingdata.sample.quantifiedObjects.1.xml");

        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(inputStream);

        StaxUtils.traverse(reader, target);

        List<Pair<String, String>> labeled = target.getLabeled();

//        labeled.stream().map(Pair::toString).forEach(System.out::println);

        assertThat(target.getLabeled(), hasSize(23));

        assertThat(target.getLabeled().get(1).getKey(), is("car"));
        assertThat(target.getLabeled().get(1).getValue(), is("I-<quantifiedObject_right>"));

        Stream<String> quantifiedObjects = labeled
                .stream()
                .map(Pair::getRight)
                .filter(v -> v.contains("quantifiedObject"));

        Stream<String> measures = labeled
                .stream()
                .map(Pair::getRight)
                .filter(v -> v.contains("measure"));


        assertThat(quantifiedObjects.count(), is(3L));
        assertThat(measures.count(), is(6L));
    }

    @Test
    public void testHandler_realCase_small() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("trainingdata.sample.quantifiedObjects.2.xml");

        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);

        List<Pair<String, String>> labeled = target.getLabeled();

//        labeled.stream().map(Pair::toString).forEach(System.out::println);

        Stream<String> quantifiedObjects = labeled
                .stream()
                .map(Pair::getRight)
                .filter(v -> v.contains("quantifiedObject"));

        Stream<String> measures = labeled
                .stream()
                .map(Pair::getRight)
                .filter(v -> v.contains("measure"));


        assertThat(quantifiedObjects.count(), is(4L));
        assertThat(measures.count(), is(6L));

    }

    @Test
    public void testHandler_realCase_long() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("trainingdata.sample.quantifiedObjects.3.xml");

        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);

        List<Pair<String, String>> labeled = target.getLabeled();

//        labeled.stream().map(Pair::toString).forEach(System.out::println);

        Stream<String> quantifiedObjects = labeled
                .stream()
                .map(Pair::getRight)
                .filter(v -> v.contains("I-<quantifiedObject"));

        Stream<String> measures = labeled
                .stream()
                .map(Pair::getRight)
                .filter(v -> v.contains("I-<measure"));


        assertThat(quantifiedObjects.count(), is(60L));
        assertThat(measures.count(), is(102L));

    }

    @Test
    public void testHandler_incompleteLinks() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("trainingdata.sample.quantifiedObjects.4.xml");

        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);

        List<Pair<String, String>> labeled = target.getLabeled();

//        labeled.stream().map(Pair::toString).forEach(System.out::println);

        Stream<String> quantifiedObjects = labeled
                .stream()
                .map(Pair::getRight)
                .filter(v -> v.contains("I-<quantifiedObject"));

        Stream<String> measures = labeled
                .stream()
                .map(Pair::getRight)
                .filter(v -> v.contains("I-<measure"));


        assertThat(quantifiedObjects.count(), is(2L));
        assertThat(measures.count(), is(3L));

    }

    @Test(expected = GrobidException.class)
    public void testHandler_incompleteLinks2() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("trainingdata.sample.quantifiedObjects.5.xml");

        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);

        StaxUtils.traverse(reader, target);

        target.getLabeled();
    }
}