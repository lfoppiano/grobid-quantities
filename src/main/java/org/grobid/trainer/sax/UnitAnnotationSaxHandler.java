package org.grobid.trainer.sax;


import org.apache.commons.collections4.CollectionUtils;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.Quantity;
import org.grobid.core.utilities.Pair;
import org.grobid.trainer.UnitLabeled;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * SAX handler for TEI-style annotations. should work for patent PDM and our usual scientific paper encoding.
 * The training data for the CRF models are generated during the XML parsing.
 */
public class UnitAnnotationSaxHandler extends DefaultHandler {

    StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text
    private String currentTag = null;

    private List<UnitLabeled> labeled = new ArrayList<>(); // store line by line the labeled data
    UnitLabeled currentUnit = new UnitLabeled();

    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
    }

    public String getText() {
        return accumulator.toString().trim();
    }

    public List<UnitLabeled> getLabeledResult() {
        return labeled;
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes) {

        if (isUnitTag(qName)) {
            currentUnit = new UnitLabeled();

            if (hasRightAttachment(attributes)) {
                currentUnit.setHasRightAttachment(true);
            }
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

    private boolean hasRightAttachment(Attributes attributes) {
        final int attrIdx = attributes.getIndex("rightAttachment");
        if (attrIdx > -1) {
            return "true".equals(attributes.getValue(attrIdx));
        } else {
            return false;
        }
    }

    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
        if (isRelevantTag(qName)) {
            writeData(qName);
        } else if ("unit".equals(qName)) {
            //currentUnit.addLabel(new Pair("\n", null));
            if (CollectionUtils.isNotEmpty(currentUnit.getLabels())) {
                labeled.add(currentUnit);
            } else {
                accumulator.setLength(0);
            }
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
            List<String> tokens = analyzer.tokenizeByCharacter(text);

            boolean begin = true;
            for (String token : tokens) {
                token = token.trim();

                if (token.length() == 0)
                    continue;

                String content = token;
                int i = 0;
                if (begin && !currentTag.equals("<other>")) {
                    currentUnit.addLabel(new Pair<>(content, "I-" + currentTag));
                    begin = false;
                } else {
                    currentUnit.addLabel(new Pair<>(content, currentTag));
                }
            }
            accumulator.setLength(0);

            //labeled.add(new Pair(text, currentTag));
        }
    }

    private boolean isRelevantTag(String qName) {
        if ("pow".equals(qName) || "base".equals(qName)
                || "prefix".equals(qName) || "other".equals(qName)) {
            return true;
        }
        return false;
    }

    private boolean isUnitTag(String qName) {
        if ("unit".equals(qName)) {
            return true;
        }
        return false;
    }

}
