package org.grobid.trainer.sax;


import org.grobid.core.utilities.Pair;
import org.grobid.trainer.UnitLabeled;
import org.grobid.trainer.ValueLabeled;
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
            List<String> tokens = tokenize(text);

            boolean begin = true;
            for (String token : tokens) {
                token = token.trim();

                if (token.length() == 0)
                    continue;

                if (token.equals("+L+")) {
                    currentValue.addLabel(new Pair<>("@newline", null));
                } else if (token.equals("+PAGE+")) {
                    currentValue.addLabel(new Pair<>("@newpage", null));
                } else {
                    String content = token;
                    int i = 0;
                    if (content.length() > 0) {
                        if (begin && !currentTag.equals("<other>")) {
                            currentValue.addLabel(new Pair<>(content, "I-" + currentTag));
                            begin = false;
                        } else {
                            currentValue.addLabel(new Pair<>(content, currentTag));
                        }
                    }
                }
                begin = false;
            }
            accumulator.setLength(0);

            //labeled.add(new Pair(text, currentTag));
        }
    }

    private List<String> tokenize(String text) {
        char[] tokenizationByCharacter = text.toCharArray();
        List<String> tokenizations = new ArrayList<>();
        for (char characterToken : tokenizationByCharacter) {
            tokenizations.add("" + characterToken);
        }
        return tokenizations;
    }

    private boolean isRelevantTag(String qName) {
        if ("pow".equals(qName)
                || "base".equals(qName)
                || "val".equals(qName)
                || "operation".equals(qName)
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
