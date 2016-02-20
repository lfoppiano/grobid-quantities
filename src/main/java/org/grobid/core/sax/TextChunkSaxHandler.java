package org.grobid.core.sax;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Relatively stupid SAX parser which identify structural chunk of textual content like paragraph, titles, ...
 * The parser should work with many usual formats like TEI and ST.36. 
 * 
 * @author Patrice Lopez
 */
public class TextChunkSaxHandler extends DefaultHandler {

    StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text

    private List<String> filters = null; // the name of an element for filtering out the
                                        // corresponding text, e.g. figure

    private boolean accumule = true;

    public List<String> chunks = null;

    public TextChunkSaxHandler() {
    }

    public void characters(char[] buffer, int start, int length) {
        if (accumule) {
            accumulator.append(buffer, start, length);
        }
    }

    public void setFilter(List<String> filt) {
        filters = filt;
        accumule = false;
    }

    public void addFilter(String filt) {
        if (filters == null)
            filters = new ArrayList<String>();
        filters.add(filt);
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
        // the last one is a special "large" space missed by the regex "\\p{Space}+" bellow
        text = text.replaceAll("\\p{Space}+", " ");
        return text;
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (accumule) {
            if (qName.equals("p") || qName.equals("paragraph")) {
                if (chunks == null) 
                    chunks = new ArrayList<String>();
                chunks.add(getText());
                accumulator.setLength(0);
            }
        }
        if ((filters != null) && filters.equals(qName)) {
            accumule = true;
        }
    }

    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
        if ((filters != null) && filters.equals(qName)) {
            accumule = false;
        }
        else 
            accumule = true;

        if (qName.equals("p") || qName.equals("paragraph")) {
            accumulator.setLength(0);
        }

    }

}
