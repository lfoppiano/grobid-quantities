package org.grobid.trainer.sax;

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.Pair;

import org.grobid.core.exceptions.GrobidException;

/**
 *  SAX handler for TEI-style annotations. should work for patent PDM and our usual scientific paper encoding. 
 *  Measures are inline quantities annotations. 
 *  The training data for the CRF models are generated during the XML parsing. 
 * 
 *  @author Patrice Lopez
 */
public class MeasureAnnotationSaxHandler extends DefaultHandler {

	StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text

	private boolean ignore = false;
	private boolean openList = false;
	private boolean openInterval = false;
	private boolean numEncountered = false;

	private String currentTag = null;

    private List<Pair<String,String>> labeled = null; // store line by line the labeled data

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

    public List<Pair<String,String>> getLabeledResult() {
    	return labeled;
    }

    public void endElement(java.lang.String uri, 
    					   java.lang.String localName, 
    					   java.lang.String qName) throws SAXException {
		try {
			if ( (!qName.equals("lb")) && (!qName.equals("pb")) ) {
				/*if (!qName.equals("num")) && (!qName.equals("measure"))
					currentTag = "<other>";*/
            	writeData(qName);
				currentTag = null;
        	}
			if (qName.equals("measure")) {
				openList = false;
				openInterval = false;
			}
			else if (qName.equals("figure")) {
				// figures (which include tables) were ignored !
				ignore = false;
			}
			if (qName.equals("num")) {
				numEncountered = true;
			}
		}
		catch (Exception e) {
//		    e.printStackTrace();
			throw new GrobidException("An exception occured while running Grobid.",e);
		}
 	}
	
	public void startElement(String namespaceURI, 
			     			 String localName,
			     			 String qName, 
			     			 Attributes atts) throws SAXException {
		try {
			if (qName.equals("lb")) {
	            accumulator.append(" +L+ ");
	        } 
			else if (qName.equals("pb")) {
	            accumulator.append(" +PAGE+ ");
	        } 
			else if (qName.equals("space")) {
	            accumulator.append(" ");
	        } 
			else {
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
		            for (int i=0; i<length; i++) {
		                // Get names and values for each attribute
		                String name = atts.getQName(i);
		                String value = atts.getValue(i);

		                if ( (name != null) && (value != null) ) {
		                   	if (name.equals("type")) {
		                   		if (value.equals("value")) {
		                   			// i.e. atomic value, default case
								}
								else if (value.equals("interval")) {
									openInterval = true;
								}
								else if (value.equals("list")) {
									openList = true;
								}
								else {
									// we have a measurement type and the element is identifying a unit
									String measureType = value;
									// we check if we know this measure type or not
									// ..

									System.out.println("Warning: unknown measure type, " + value);

									// if we know the measurement type, we check if we know the unit expression
									// if not we add it to the lexicon
									

									if (numEncountered)
										currentTag = "<unitLeft>";
									else
										currentTag = "<unitRight>";
									numEncountered = false;
								}
		                   	}
		                   	/*else if (name.equals("unit")) {
								if (numEncountered)
									currentTag = "<unitLeft>";
								else
									currentTag = "<unitRight>";
								numEncountered = false;
		                   	}*/
							else {
								System.out.println("Warning: unknown measure attribute name, " + name);
							}
		                }
		            }
				}
				else if (qName.equals("num")  && !ignore) {
					int length = atts.getLength();
					if (length == 0) {
						// not interval value
						if (openList) {
							currentTag = "<valueList>";
						}
						else {
							currentTag = "<valueAtomic>";
						}
					}
					else {
			            // Process each attribute
			            for (int i=0; i<length; i++) {
			                // Get names and values for each attribute
			                String name = atts.getQName(i);
			                String value = atts.getValue(i);

			                if ( (name != null) && (value != null) ) {
			                   	if (name.equals("atLeast")) {
									currentTag = "<valueLeast>";
			                   	}
			                   	else if (name.equals("atMost")) {
									currentTag = "<valueMost>";
			                   	}
			                }
			            }
			        }
			        numEncountered = true;
				}
				/*else if (qName.equals("div")  && !ignore) {
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
				}
				else if (qName.equals("TEI") || qName.equals("tei")) {
					//measureBuffer = new StringBuilder();
					//quantityBuffer = new StringBuilder();
					labeled = new ArrayList<Pair<String,String>>();
	        		//currentTags = new Stack<String>();
	        		accumulator = new StringBuffer();
	        		currentTag = null;
				}
			}
		}
		catch (Exception e) {
//		    e.printStackTrace();
			throw new GrobidException("An exception occured while running Grobid.",e);
		}
	}

	private void writeData(String qName) {
		if (currentTag == null)
			currentTag = "<other>";
        if ( (qName.equals("other")) || 
                (qName.equals("measure")) || (qName.equals("num")) ||
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
            StringTokenizer st = new StringTokenizer(text, " \n\t" + TextUtilities.fullPunctuations, true);
            boolean begin = true;
            while (st.hasMoreTokens()) {
                String tok = st.nextToken().trim();
                if (tok.length() == 0) 
					continue;

                if (tok.equals("+L+")) {
                    labeled.add(new Pair("@newline", null));
                } 
				else if (tok.equals("+PAGE+")) {
                    // page break should be a distinct feature
                    labeled.add(new Pair("@newpage", null));
                }
				else {
                    String content = tok;
                    int i = 0;
                    if (content.length() > 0) {
                        if (begin) {
                            labeled.add(new Pair(content, "I-" + currentTag));
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
