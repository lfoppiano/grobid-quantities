package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.engines.AbstractParser;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorQuantities;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.Pair;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.analyzers.GrobidAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Quantity/measurement extraction.
 *
 * @author Patrice Lopez
 */
public class QuantityParser extends AbstractParser {

	private QuantityLexicon quantityLexicon = null;

    public QuantityParser() {
        super(GrobidModels.QUANTITIES);
        quantityLexicon = QuantityLexicon.getInstance();
    }

    /**
     * Extract all occurences of measurement/quantities from a simple piece of text.
     */
    public List<Quantity> extractQuantities(String text) throws Exception {
        if (text == null)
            return null;
        if (text.length() == 0)
            return null;
        List<Quantity> quantities = null;
        try {
            text = text.replace("\n", " ");
            GrobidAnalyzer analyzer = GrobidAnalyzer.getInstance();
            List<String> tokenizations = analyzer.tokenize(text);

            if (tokenizations.size() == 0)
                return null;

            String ress = null;
            List<String> texts = new ArrayList<String>();
            for (String token : tokenizations) {
            	if (!token.equals(" ")) {
	                texts.add(token);
	            }
            }
			
            // to store unit term positions
            List<OffsetPosition> unitTokenPositions = new ArrayList<OffsetPosition>();
			unitTokenPositions = quantityLexicon.inUnitNames(texts);
System.out.println(texts.toString());			
            ress = addFeatures(texts, unitTokenPositions);
System.out.println(ress);	
			String res = null;
			try {
				res = label(ress);
System.out.println(res);		
			}
			catch(Exception e) {
				throw new GrobidException("CRF labeling for quantity parseing failed.", e);
			}
            quantities = resultExtraction(text, res, tokenizations);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return quantities;
    }

    /**
     * Extract identified quantities from a labelled text.
     */
    public List<Quantity> resultExtraction(String text, 
										   String result,
                                           List<String> tokenizations) {

		List<Quantity> quantities = new ArrayList<Quantity>();
        StringTokenizer stt = new StringTokenizer(result, "\n");
		String label = null; // label
        String actual = null; // token
		int offset = 0;
		int addedOffset = 0;
		int p = 0; // iterator for the tokenizations for restauring the original tokenization with
        // respect to spaces
		Quantity currentQuantity = null;
		while (stt.hasMoreTokens()) {
            String line = stt.nextToken();
            if (line.trim().length() == 0) {
                continue;
            }

			StringTokenizer st2 = new StringTokenizer(line, "\t");
            boolean start = true;
			//String separator = "";
            label = null;
            actual = null;
            while (st2.hasMoreTokens()) {
                if (start) {
                    actual = st2.nextToken().trim();
                    start = false;

                    boolean strop = false;
                    while ((!strop) && (p < tokenizations.size())) {
                        String tokOriginal = tokenizations.get(p);
						addedOffset += tokOriginal.length();
						if (tokOriginal.equals(actual)) {
                            strop = true;
                        }
                        p++;
                    }
                } else {
                    label = st2.nextToken().trim();
                }
            }

            if (label == null) {
				offset += addedOffset;
				addedOffset = 0;
                continue;
            }

            if (actual != null) {
				if (label.startsWith("B-")) {      
					if (currentQuantity != null) {
						int localPos = currentQuantity.getOffsetEnd();
						if (label.length() > 1) {  
							String subtag = label.substring(2,label.length()).toLowerCase();
							if (currentQuantity.getRawString().equals(subtag) && 
							   ( (localPos == offset) ) ) {
								currentQuantity.setOffsetEnd(offset+addedOffset);
								offset += addedOffset;
								addedOffset = 0;	
								continue;
							}														
							quantities.add(currentQuantity);
						}
					}
					if (label.length() > 1) {  
						String subtag = label.substring(2,label.length()).toLowerCase();
						currentQuantity = new Quantity(subtag);   
						if ( text.charAt(offset) == ' ') {	
							currentQuantity.setOffsetStart(offset+1);
						}
						else
							currentQuantity.setOffsetStart(offset);
						currentQuantity.setOffsetEnd(offset+addedOffset);
					}  
				}
				else if (label.startsWith("I-")) {  
					if (label.length() > 1) {  
						String subtag = label.substring(2,label.length()).toLowerCase();

					    if ( (currentQuantity != null) && (currentQuantity.getRawString().equals(subtag)) ) {
							currentQuantity.setOffsetEnd(offset+addedOffset);		
						}
						else {
							// should not be the case, but we add the new entity, for robustness      
							if (currentQuantity != null) 
								quantities.add(currentQuantity);
							currentQuantity = new Quantity(subtag);   
							currentQuantity.setOffsetStart(offset);
							currentQuantity.setOffsetEnd(offset+addedOffset);
						}
				   	}
				}
				
				offset += addedOffset;
				addedOffset = 0;
			}			
		}
		
		if (currentQuantity != null) {
			quantities.add(currentQuantity);
		}
		
		return quantities;
	}

	/**
     * Process the content of the specified input file and format the result as training data.
     *
     * @param inputFile    input file
     * @param pathTEI      path to TEI with annotated training data
     * @param id           id
     */
    public void createTraining(String inputFile,
                               String pathTEI,
                               int id) {

    }

    @SuppressWarnings({"UnusedParameters"})
    private String addFeatures(List<String> texts,
                               List<OffsetPosition> unitTokenPositions) {
        int totalLine = texts.size();
        int posit = 0;
		int currentQuantityIndex = 0;
		List<OffsetPosition> localPositions = unitTokenPositions;
        boolean isUnitPattern = false;
        StringBuilder result = new StringBuilder();
        try {
            for (String token : texts) {
				if (token.trim().equals("@newline")) {
					result.append("\n");
					continue;
				}
	
				isUnitPattern = true;				

				// do we have a unit at position posit?
				if ((localPositions != null) && (localPositions.size()>0)) {	
					for(int mm = currentQuantityIndex; mm < localPositions.size(); mm++) {
						if ( (posit >= localPositions.get(mm).start) && (posit <= localPositions.get(mm).end) ) {
							isUnitPattern = true;
							currentQuantityIndex = mm;
							break;
						}
						else if (posit < localPositions.get(mm).start) {
							isUnitPattern = false;
							break;
						}
						else if (posit > localPositions.get(mm).end) {
							continue;
						}
					}
				}
				
                FeaturesVectorQuantities featuresVector =
                        FeaturesVectorQuantities.addFeaturesQuantities(token, null,
                                quantityLexicon.inUnitDictionary(token), isUnitPattern);
                result.append(featuresVector.printVector());
                result.append("\n");
                posit++;
				isUnitPattern = false;
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return result.toString();
    }
}
