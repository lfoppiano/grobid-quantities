package org.grobid.core.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Relatively stupid SAX parser which identify structural chunk of textual content like paragraph, titles, ...
 * The parser should "work" (at the expected level of stupidity) with many usual formats like TEI and ST.36.
 *
 * @author Patrice Lopez
 */
public class TextChunkSaxHandler extends DefaultHandler {

    StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text

    private List<String> filteredTags = new ArrayList<>(); // the name of an element for filtering out the
    // corresponding text, e.g. figure

    private boolean accumule = true;

    public List<String> chunks = new ArrayList<>();

    public TextChunkSaxHandler() {
    }

    public void characters(char[] buffer, int start, int length) {
        if (accumule) {
            accumulator.append(buffer, start, length);
        }
    }

    public void setFilteredTags(List<String> filt) {
        filteredTags = filt;
        accumule = false;
    }

    public void addFilteredTag(String filt) {
        filteredTags.add(filt);
        accumule = false;
    }

    public List<String> getChunks() {
        return chunks;
    }

    public String getText() {
        String text = accumulator.toString().trim();
        text = text.replace("\n", " ");
        text = text.replace("\t", " ");
        text = text.replace(" ", " ");
        // the last one is a special "large" space missed by the regex "\\p{Space}+" below
        text = text.replaceAll("\\p{Space}+", " ");
        return text;
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (accumule) {
            if (qName.equals("p") || qName.equals("paragraph")) {
                chunks.add(getText());
                accumulator.setLength(0);
            }
        }
        if (filteredTags.contains(qName)) {
            accumule = true;
        }
    }

    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) throws SAXException {
        if (filteredTags.contains(qName)) {
            accumule = false;
        } else {
            accumule = true;
        }

        if (qName.equals("p") || qName.equals("paragraph")) {
            accumulator.setLength(0);
        }

    }

}
