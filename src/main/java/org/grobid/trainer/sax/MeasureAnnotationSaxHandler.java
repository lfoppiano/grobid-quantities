package org.grobid.trainer.sax;


import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.UnitUtilities;
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
public class MeasureAnnotationSaxHandler extends DefaultHandler {

    StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text

    private boolean ignore = false;
    private boolean openList = false;
    private boolean openInterval = false;
    private boolean numEncountered = false;
    private boolean openUnit = false;
    private boolean openAtomicValueWithinList = false;
    private boolean rangeBaseEncountered = false;
    private boolean beginRangeBaseEncountered = false;

    private String currentTag = null;

    private List<Pair<String, String>> labeled = null; // store line by line the labeled data

    public MeasureAnnotationSaxHandler() {
    }

    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
    }

    public String getText() {
        if (accumulator != null) {
            return accumulator.toString().trim();
        } else {
            return null;
        }
    }

    public List<Pair<String, String>> getLabeledResult() {
        return labeled;
    }

    public void endElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName) throws SAXException {
        try {
            if ((!qName.equals("lb")) && (!qName.equals("pb"))) {
                /*if (!qName.equals("num")) && (!qName.equals("measure"))
                    currentTag = "<other>";*/
                writeData(qName);
                currentTag = null;
            }
            if (qName.equals("measure")) {
                if (openAtomicValueWithinList) {
                    openAtomicValueWithinList = false;
                    openUnit = true;
                }
                else if (openUnit) {
                    openUnit = false;
                }
                else {
                    openList = false;
                    openInterval = false;
                    numEncountered = false;
                    rangeBaseEncountered = false;
                    beginRangeBaseEncountered = false;
                }
            } else if (qName.equals("figure")) {
                // figures (which include tables) were ignored !
                ignore = false;
            }
            if (qName.equals("num") || qName.equals("date")) {
                numEncountered = true;
            } /*else if (qName.equals("div")) {
                // let's consider a new CRF input per section
                labeled.add(new Pair("\n", null));
            } */
            else if (qName.equals("p") || qName.equals("paragraph")) {
                // let's consider a new CRF input per paragraph too
                labeled.add(new Pair("\n", null));
                openList = false;
                openInterval = false;
                numEncountered = false;
            }
        } catch (Exception e) {
//		    e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts) throws SAXException {
        try {
            if (qName.equals("lb")) {
                accumulator.append(" +L+ ");
            } else if (qName.equals("pb")) {
                accumulator.append(" +PAGE+ ");
            } else if (qName.equals("space")) {
                accumulator.append(" ");
            } else {
                // we have to write first what has been accumulated yet with the upper-level tag
                String text = getText();
                if (text != null) {
                    if (text.length() > 0) {
                        currentTag = "<other>";
                        writeData(qName);
                    }
                }
                accumulator.setLength(0);

                // we output the remaining text
                if (qName.equals("measure") && !ignore) {

                    int length = atts.getLength();

                    // Process each attribute
                    for (int i = 0; i < length; i++) {
                        // Get names and values for each attribute
                        String name = atts.getQName(i);
                        String value = atts.getValue(i);

                        if ((name != null) && (value != null)) {
                            if (name.equals("type")) {
                                if (value.equals("value")) {
                                    // i.e. atomic value, default case
                                    if (openList) {
                                        // this means that we have an embedded atomic value within a list - yes it happens :)
                                        // e.g.  δ (ppm):8.64(2H, d, J=7.0Hz), 8.46 (2H, d, J=7.0Hz), 8.11(1H, d, J=5.4Hz), 7.27(1H, d, J=5.4Hz)
                                        openAtomicValueWithinList = true;
                                    }
                                } else if (value.equals("interval")) {
                                    openInterval = true;
                                } else if (value.equals("list")) {
                                    openList = true;
                                } else {
                                    // we have a measurement type and the element is identifying a unit
                                    String measureType = value;
                                    // we check if we know this measure type or not
                                    UnitUtilities.Unit_Type unitType = null;
                                    try {
                                        unitType = UnitUtilities.Unit_Type.valueOf(measureType);
                                    } catch (Exception e) {
                                        System.out.println("Warning: unknown measure type, " + value);
                                    }
                                    // if we know the measurement type, we check if we know the unit expression
                                    // if not we add it to the lexicon

                                    if (numEncountered)
                                        currentTag = "<unitLeft>";
                                    else
                                        currentTag = "<unitRight>";
                                    numEncountered = false;
                                    openUnit = true;
                                }
                            } else if (name.equals("unit")) {
                                openUnit = true;
                            } else if (name.equals("scale")) {
                                // nothing to do in principle for the moment...
                            } else {
                                System.out.println("Warning: unknown measure attribute name, " + name);
                            }
                        }
                    }
                } else if ((qName.equals("num") || qName.equals("date")) && !ignore) {
                    boolean whenEncountered = false;
                    int length = atts.getLength();
                    for (int i = 0; i < length; i++) { 
                        String name = atts.getQName(i);
                        if (name != null && name.equals("when")) {
                            whenEncountered = true;
                        }
                    }
                    if ( (length == 0) || ((length == 1) && whenEncountered) ) {
                        // not interval value
                        if (openAtomicValueWithinList)
                            currentTag = "<valueAtomic>";
                        else if (openList) {
                            currentTag = "<valueList>";
                        } else {
                            currentTag = "<valueAtomic>";
                        }
                    } else {
                        // Process each attribute
                        for (int i = 0; i < length; i++) {
                            // Get names and values for each attribute
                            String name = atts.getQName(i);
                            String value = atts.getValue(i);

                            if ((name != null) && (value != null)) {
                                if (name.equals("atLeast") || name.equals("from-iso")) {
                                    currentTag = "<valueLeast>";
                                } else if (name.equals("atMost") || name.equals("to-iso")) {
                                    currentTag = "<valueMost>";
                                } else if (value.equals("base")) {
                                    currentTag = "<valueBase>";
                                    rangeBaseEncountered= true;
                                } else if (value.equals("range")) {
                                    currentTag = "<valueRange>";
                                    rangeBaseEncountered= true;
                                }
                            }
                        }
                    }
                    numEncountered = true;
                } 
                /*else if (qName.equals("div")) {
                    int length = atts.getLength();

		            // Process each attribute
		            for (int i=0; i<length; i++) {
		                // Get names and values for each attribute
		                String name = atts.getQName(i);
		                String value = atts.getValue(i);

		                if ( (name != null) && (value != null) ) {
		                   	if (name.equals("type")) {
		                   		if (value.equals("claims")) {
									
								}
								else if (value.equals("description")) {
									
								}
		                   	}
		                }
		            }
				}*/
                else if (qName.equals("figure")) {
                    // figures are ignored ! this includes tables
                    ignore = true;
                } else if (qName.equals("TEI") || qName.equals("tei") || qName.equals("teiCorpus") ) {
                    //measureBuffer = new StringBuilder();
                    //quantityBuffer = new StringBuilder();
                    labeled = new ArrayList<>();
                    //currentTags = new Stack<String>();
                    accumulator = new StringBuffer();
                    currentTag = null;
                }
            }
        } catch (Exception e) {
//		    e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    private void writeData(String qName) {
        if (currentTag == null)
            currentTag = "<other>";
        if ((qName.equals("other")) ||
                (qName.equals("measure")) || (qName.equals("num")) || (qName.equals("date")) ||
                (qName.equals("paragraph")) || (qName.equals("p")) ||
                (qName.equals("div"))
                ) {
            if (currentTag == null) {
                return;
            }

            /*if (pop) {
                if (!currentTags.empty()) {
					currentTags.pop();
				}
            }*/

            String text = getText();
            // we segment the text
            //StringTokenizer st = new StringTokenizer(text, " \n\t" + TextUtilities.fullPunctuations, true);
            List<String> tokenizations = QuantityAnalyzer.tokenize(text);
            boolean begin = true;
            for (String tok : tokenizations) {
                tok = tok.trim();
                if (tok.length() == 0)
                    continue;

                if (tok.equals("+L+")) {
                    labeled.add(new Pair("@newline", null));
                } else if (tok.equals("+PAGE+")) {
                    // page break should be a distinct feature
                    labeled.add(new Pair("@newpage", null));
                } else {
                    String content = tok;
                    int i = 0;
                    if (content.length() > 0) {
                        if (begin && (!currentTag.equals("<other>")) && (!rangeBaseEncountered || !beginRangeBaseEncountered) ) {
                            labeled.add(new Pair(content, "I-" + currentTag));
                            if (rangeBaseEncountered)
                                beginRangeBaseEncountered = true;
                            begin = false;
                        } else {
                            labeled.add(new Pair(content, currentTag));
                        }
                    }
                }
                begin = false;
            }
            accumulator.setLength(0);
        }
    }

}
