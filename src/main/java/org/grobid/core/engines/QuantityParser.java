package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.Measurement;
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
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.utilities.LayoutTokensUtil;

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
            List<LayoutToken> tokenizations = QuantityAnalyzer.tokenizeWithLayoutToken(text);

            if (tokenizations.size() == 0)
                return null;

            String ress = null;
            List<String> texts = new ArrayList<String>();
            for (LayoutToken token : tokenizations) {
            	if (!token.getText().equals(" ")) {
	                texts.add(token.getText());
	            }
            }
			
            // to store unit term positions
            List<OffsetPosition> unitTokenPositions = new ArrayList<OffsetPosition>();
			unitTokenPositions = quantityLexicon.inUnitNames(texts);
            ress = addFeatures(texts, unitTokenPositions);
			String res = null;
			try {
				res = label(ress);
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
                                           List<LayoutToken> tokenizations) {

		List<Quantity> quantities = new ArrayList<Quantity>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.QUANTITIES, result, tokenizations);

        String tokenLabel = null;
        List<TaggingTokenCluster> clusters = clusteror.cluster();

        Unit currentUnit = new Unit();
        Quantity currentQuantity = new Quantity();
        Measurement currentMeasurement = new Measurement();

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(cluster.concatTokens()));
            if (clusterLabel  == TaggingLabel.VALUE_ATOMIC) {
                System.out.println("atomic value: " + clusterContent);
            }
            else if (clusterLabel  == TaggingLabel.VALUE_LEAST) {
                System.out.println("value least: " + clusterContent);
            }
            else if (clusterLabel  == TaggingLabel.VALUE_MOST) {
                System.out.println("value most: " + clusterContent);
            }
            else if (clusterLabel  == TaggingLabel.VALUE_LIST) {
                System.out.println("value in list: " + clusterContent);
            }
            else if (clusterLabel  == TaggingLabel.UNIT_LEFT) {
                System.out.println("unit (left attachment): " + clusterContent);
            }
            else if (clusterLabel  == TaggingLabel.UNIT_RIGHT) {
                System.out.println("unit (right attachment): " + clusterContent);
            }
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
