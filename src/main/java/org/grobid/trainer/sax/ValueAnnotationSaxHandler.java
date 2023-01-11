package org.grobid.trainer.sax;


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.trainer.ValueLabeled;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * SAX handler for TEI-style annotations. should work for patent PDM and our usual scientific paper encoding.
 * The training data for the CRF models are generated during the XML parsing.
 */
public class ValueAnnotationSaxHandler extends DefaultHandler {

    StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text
    private String currentTag = null;

    private List<ValueLabeled> labeled = new ArrayList<>(); // store line by line the labeled data
    ValueLabeled currentValue = null;

    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
    }

    public String getText() {
        return accumulator.toString().trim();
    }

    public List<ValueLabeled> getLabeledResult() {
        return labeled;
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes) {

        if (isValueTag(qName)) {
            currentValue = new ValueLabeled();
        } else {
            // we have to write first what has been accumulated yet with the upper-level tag
            String text = getText();
            if (text != null) {
                if (isNotEmpty(text)) {
                    currentTag = "<other>";
                    writeData("other");
                }
            }
            accumulator.setLength(0);
        }

    }

    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
        if (isRelevantTag(qName)) {
            writeData(qName);
        } else if (isValueTag(qName)) {
            labeled.add(currentValue);
        }

    }

    /**
     * After the data is extracted from the tags, the data is tokenized at character level and
     * the label are calculated. The prefix 'I-' is attached to distinguish the beginning of a new
     * tag (for example if we have <base>Hz</base><base>m</base>).
     */
    private void writeData(String qName) {
        if (currentTag == null)
            currentTag = "<other>";

        if (isRelevantTag(qName)) {
            currentTag = "<" + qName + ">";

            String text = getText();

            // text segmentation
            QuantityAnalyzer analyzer = QuantityAnalyzer.getInstance();
            List<String> tokens = analyzer.tokenize(text);

            boolean begin = true;
            for (String token : tokens) {
                token = trim(token);

                if (isEmpty(token))
                    continue;

                if (begin && !currentTag.equals("<other>")) {
                    currentValue.addLabel(new ImmutablePair<>(token, "I-" + currentTag));
                    begin = false;
                } else {
                    currentValue.addLabel(new ImmutablePair<>(token, currentTag));
                }
            }
            accumulator.setLength(0);

            //labeled.add(new Pair(text, currentTag));
        }
    }

    private boolean isRelevantTag(String qName) {
        if ("pow".equals(qName)
            || "base".equals(qName)
            || "number".equals(qName)
            || "exp".equals(qName)
            || "alpha".equals(qName)
            || "time".equals(qName)
            || "other".equals(qName)) {
            return true;
        }
        return false;
    }

    private boolean isValueTag(String qName) {
        if ("value".equals(qName)) {
            return true;
        }
        return false;
    }

}
