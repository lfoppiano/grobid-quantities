package org.grobid.trainer.sax;


import org.grobid.core.utilities.Pair;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * SAX handler for TEI-style annotations. should work for patent PDM and our usual scientific paper encoding.
 * Measures are inline quantities annotations.
 * The training data for the CRF models are generated during the XML parsing.
 *
 * @author Patrice Lopez
 */
public class UnitAnnotationSaxHandler extends DefaultHandler {

    StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text
    private String currentTag = null;

    private List<Pair<String, String>> labeled = new ArrayList<>(); // store line by line the labeled data

    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
    }

    public String getText() {
        return accumulator.toString().trim();
    }

    public List<Pair<String, String>> getLabeledResult() {
        return labeled;
    }

    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
        if (isRelevantTag(qName)) {
            writeData(qName);
        } else if ("unit".equals(qName)) {
            labeled.add(new Pair("\n", null));
        }

    }

    private void writeData(String qName) {
        if (currentTag == null)
            currentTag = "<other>";
        if (isRelevantTag(qName)) {
            currentTag = "<" + qName + ">";

            String text = getText();
            accumulator = new StringBuffer();

            labeled.add(new Pair(text, currentTag));
        }
    }

    private boolean isRelevantTag(String qName) {
        if ("pow".equals(qName) || "base".equals(qName)
                || "prefix".equals(qName) || "other".equals(qName)) {
            return true;
        }
        return false;
    }

}
