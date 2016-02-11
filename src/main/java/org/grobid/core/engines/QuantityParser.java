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
import org.grobid.core.utilities.UnitUtilities;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.GrobidProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

import static org.apache.commons.lang3.StringUtils.isBlank;

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
    public List<Measurement> extractQuantities(String text) throws Exception {
        if (isBlank(text)) {
            return null;
        }
        List<Measurement> measurements = new ArrayList<>();
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
            String res;
            try {
                res = label(ress);
            } catch (Exception e) {
                throw new GrobidException("CRF labeling for quantity parsing failed.", e);
            }
            measurements = resultExtraction(res, tokenizations);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }

        return measurements;
    }

    /**
     * Extract identified quantities from a labelled text.
     */
    public List<Measurement> resultExtraction(String result,
                                              List<LayoutToken> tokenizations) {
        List<Measurement> measurements = new ArrayList<>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.QUANTITIES, result, tokenizations);

        String tokenLabel = null;
        List<TaggingTokenCluster> clusters = clusteror.cluster();

        Unit currentUnit = new Unit();
        Quantity currentQuantity = new Quantity();
        Measurement currentMeasurement = new Measurement();
        UnitUtilities.Measurement_Type openMeasurement = null;

        int pos = 0; // position in term of characters for creating the offsets

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            List<LayoutToken> theTokens = cluster.concatTokens();
            String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(cluster.concatTokens()));

            int endPos = pos;
            for (LayoutToken token : theTokens) {
                if (token.getText() != null)
                    endPos += token.getText().length();
            }

            if (clusterLabel == TaggingLabel.VALUE_ATOMIC) {
                System.out.println("atomic value: " + clusterContent);
                if (isMeasurementValid(currentMeasurement)) {
                    measurements.add(currentMeasurement);
                    currentMeasurement = new Measurement();
                    currentUnit = new Unit();
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                if (currentUnit.getRawName() != null) {
                    currentQuantity.setRawUnit(currentUnit);
                    currentMeasurement.setAtomicQuantity(currentQuantity);
                    measurements.add(currentMeasurement);
                    currentMeasurement = new Measurement();
                    currentQuantity = new Quantity();
                } else {
                    // unit will be attached later
                    currentMeasurement.setType(UnitUtilities.Measurement_Type.VALUE);
                    currentMeasurement.setAtomicQuantity(currentQuantity);
                    openMeasurement = UnitUtilities.Measurement_Type.VALUE;
                }
            } else if (clusterLabel == TaggingLabel.VALUE_LEAST) {
                System.out.println("value least: " + clusterContent);
                if (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL) {
                    if (isMeasurementValid(currentMeasurement)) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                if (currentUnit.getRawName() != null)
                    currentQuantity.setRawUnit(currentUnit);
                currentMeasurement.setQuantityLeast(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL;
            } else if (clusterLabel == TaggingLabel.VALUE_MOST) {
                System.out.println("value most: " + clusterContent);
                if (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL) {
                    if (isMeasurementValid(currentMeasurement)) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                if (currentUnit.getRawName() != null) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.setQuantityMost(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL;
            } else if (clusterLabel == TaggingLabel.VALUE_LIST) {
                System.out.println("value in list: " + clusterContent);
                if (openMeasurement != UnitUtilities.Measurement_Type.CONJUNCTION) {
                    if (isMeasurementValid(currentMeasurement)) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentQuantity = new Quantity();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity.setRawValue(clusterContent);
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                if (currentUnit.getRawName() != null)
                    currentQuantity.setRawUnit(currentUnit);
                currentMeasurement.addQuantity(currentQuantity);
                openMeasurement = UnitUtilities.Measurement_Type.CONJUNCTION;
            } else if (clusterLabel == TaggingLabel.UNIT_LEFT) {
                System.out.println("unit (left attachment): " + clusterContent);
                currentUnit.setRawName(clusterContent);
                currentQuantity.setRawUnit(currentUnit);
                if ((currentMeasurement.getQuantities() != null) && (currentMeasurement.getQuantities().size() > 0)) {
                    for (Quantity quantity : currentMeasurement.getQuantities()) {
                        if (quantity.getRawUnit() == null)
                            quantity.setRawUnit(currentUnit);
                        else
                            break;
                    }
                }
                currentUnit = new Unit();
            } else if (clusterLabel == TaggingLabel.UNIT_RIGHT) {
                System.out.println("unit (right attachment): " + clusterContent);
                currentUnit.setRawName(clusterContent);
            }
            pos = endPos + 1;
        }

        if (isMeasurementValid(currentMeasurement)) {
            measurements.add(currentMeasurement);
        }

        return measurements;
    }

    /**
     * Process the content of the specified input file and format the result as training data.
     * <p>
     * Input file can be (i) xml (.xml or .tei extennsion) and it is assumed that we have a patent
     * document, (ii) PDF (.pdf) and it is assumed that we have a scientific article which will
     * be processed by GROBID full text first, (iii) some text (.txt extension).
     *
     * @param inputFile input file
     * @param pathTEI   path to TEI with annotated training data
     * @param id        id
     */
    public void createTraining(String inputFile,
                               String pathTEI,
                               int id) throws IOException {
        File file = new File(inputFile);
        if (!file.exists()) {
            throw new GrobidException("Cannot create training data because input file can not be accessed: " + inputFile);
        }

        if (inputFile.endsWith(".txt") || inputFile.endsWith(".TXT")) {
            String text = FileUtils.readFileToString(file); 

            StringBuilder tei = new StringBuilder();
            tei.append(getTEIHeader(id));

            tei.append("<TEI>");
            // for the moment we suppose we have english only...
            tei.append("\t<text xml:lang=\"en\">\n");

            // we process the text paragraph by paragraph
            String lines[] = text.split("\n");
            StringBuilder paragraph = new StringBuilder();
            for(int i=0; i<lines.length; i++) {
                String line = lines[i].trim();
                if (line.length() != 0) {
                    paragraph.append(line).append("\n");
                }
                else if (paragraph.length() > 0) {
                    // we have a new paragraph
                    text = paragraph.toString().replace("\n", " ");
                    List<LayoutToken> tokenizations = QuantityAnalyzer.tokenizeWithLayoutToken(text);

                    if (tokenizations.size() == 0)
                        continue;

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
                        throw new GrobidException("CRF labeling for quantity parsing failed.", e);
                    }
                    List<Measurement> measurements = resultExtraction(res, tokenizations);


                }
                else
                    continue;
            }

            tei.append("\t</text>");
            tei.append("</TEI>");
        }
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
                if ((localPositions != null) && (localPositions.size() > 0)) {
                    for (int mm = currentQuantityIndex; mm < localPositions.size(); mm++) {
                        if ((posit >= localPositions.get(mm).start) && (posit <= localPositions.get(mm).end)) {
                            isUnitPattern = true;
                            currentQuantityIndex = mm;
                            break;
                        } else if (posit < localPositions.get(mm).start) {
                            isUnitPattern = false;
                            break;
                        } else if (posit > localPositions.get(mm).end) {
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

    private boolean isMeasurementValid(Measurement currentMeasurement) {
        return currentMeasurement.getType() != null &&
                currentMeasurement.getQuantities() != null && currentMeasurement.getQuantities().size() > 0;
	}

    private String getTEIHeader(int id) {
        StringBuilder header = new StringBuilder();

        header.append("<?xml version=\"1.0\" ?>").append("\n").append("<tei>").append("\n\t").append("<teiHeader>");
        if (id != -1) {
            header.append("\n\t\t").append("<fileDesc xml:id=\"_" + id + "\"/>");
        }

        header.append("\n\t\t<encodingDesc>\n");
        header.append("\t\t\t<appInfo>\n");

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);
        String dateISOString = df.format(new java.util.Date());

        header.append("\t\t\t\t<application version=\"").append(GrobidProperties.getVersion())
            .append("\" ident=\"GROBID\" when=\"" + dateISOString + "\">\n");
        header.append("\t\t\t\t\t<ref target=\"https://github.com/kermitt2/grobid\">GROBID - ")
            .append("A machine learning software for extracting information from scholarly documents</ref>\n");
        header.append("\t\t\t\t</application>\n");
        header.append("\t\t\t</appInfo>\n");
        header.append("\t\t</encodingDesc>\n");

        header.append("\t").append("</teiHeader>");

        return header.toString();
    }
}
