package org.grobid.core.engines;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

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
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.document.xml.NodeChildrenIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isBlank;

import static org.grobid.core.document.xml.XmlBuilderUtils.fromString;
import static org.grobid.core.document.xml.XmlBuilderUtils.teiElement;

/**
 * Quantity/measurement extraction.
 *
 * @author Patrice Lopez
 */
public class QuantityParser extends AbstractParser {
    private static final Logger logger = LoggerFactory.getLogger(FullTextParser.class);

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
            measurements = resultExtraction(text, res, tokenizations);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }

        return measurements;
    }

    public int batchProcess(String inputDirectory,
                             String outputDirectory,
                             boolean isRecursive) throws IOException {
        return 0;
    }

    /**
     * Extract identified quantities from a labelled text.
     */
    public List<Measurement> resultExtraction(String text,
                                              String result,
                                              List<LayoutToken> tokenizations) {
        List<Measurement> measurements = new ArrayList<>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.QUANTITIES, result, tokenizations);
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
            String clusterContent = //LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(cluster.concatTokens()));
                LayoutTokensUtil.toText(cluster.concatTokens());

            int endPos = pos;
            for (LayoutToken token : theTokens) {
                if (token.getText() != null)
                    endPos += token.getText().length();
            }

            switch (clusterLabel) {
                case QUANTITY_VALUE_ATOMIC:
                    System.out.println("atomic value: " + clusterContent);
                    if (isMeasurementValid(currentMeasurement)) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                    currentQuantity = new Quantity();
                    currentQuantity.setRawValue(clusterContent);
                    if (text.charAt(pos) == ' ') {
                        pos++;
                    }
                    currentQuantity.setOffsetStart(pos);
                    if (text.charAt(endPos-1) == ' ')
                        endPos--;
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
                    break;
                case QUANTITY_VALUE_LEAST:
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
                    if (text.charAt(pos) == ' ') {
                        pos++;
                    }
                    currentQuantity.setOffsetStart(pos);
                    if (text.charAt(endPos-1) == ' ')
                        endPos--;
                    currentQuantity.setOffsetEnd(endPos);
                    if (currentUnit.getRawName() != null)
                        currentQuantity.setRawUnit(currentUnit);
                    currentMeasurement.setQuantityLeast(currentQuantity);
                    currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL);
                    openMeasurement = UnitUtilities.Measurement_Type.INTERVAL;
                    break;
                case QUANTITY_VALUE_MOST:
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
                    if (text.charAt(pos) == ' ') {
                        pos++;
                    }
                    currentQuantity.setOffsetStart(pos);
                    if (text.charAt(endPos-1) == ' ')
                        endPos--;
                    currentQuantity.setOffsetEnd(endPos);
                    if (currentUnit.getRawName() != null) {
                        currentQuantity.setRawUnit(currentUnit);
                    }
                    currentMeasurement.setQuantityMost(currentQuantity);
                    currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL);
                    openMeasurement = UnitUtilities.Measurement_Type.INTERVAL;
                    break;
                case QUANTITY_VALUE_LIST:
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
                    if (text.charAt(pos) == ' ') {
                        pos++;
                    }
                    currentQuantity.setOffsetStart(pos);
                    if (text.charAt(endPos-1) == ' ')
                        endPos--;
                    currentQuantity.setOffsetEnd(endPos);
                    if (currentUnit.getRawName() != null)
                        currentQuantity.setRawUnit(currentUnit);
                    currentMeasurement.addQuantity(currentQuantity);
                    openMeasurement = UnitUtilities.Measurement_Type.CONJUNCTION;
                    break;
                case QUANTITY_UNIT_LEFT:
                    System.out.println("unit (left attachment): " + clusterContent);
                    currentUnit.setRawName(clusterContent);
                    currentQuantity.setRawUnit(currentUnit);
                    if (text.charAt(pos) == ' ') {
                        pos++;
                    }
                    currentUnit.setOffsetStart(pos);
                    if (text.charAt(endPos-1) == ' ')
                        endPos--;
                    currentUnit.setOffsetEnd(endPos);
                    if ((currentMeasurement.getQuantities() != null) && (currentMeasurement.getQuantities().size() > 0)) {
                        for (Quantity quantity : currentMeasurement.getQuantities()) {
                            if (quantity.getRawUnit() == null)
                                quantity.setRawUnit(currentUnit);
                            else
                                break;
                        }
                    }
                    currentUnit = new Unit();
                    break;
                case QUANTITY_UNIT_RIGHT:
                    System.out.println("unit (right attachment): " + clusterContent);
                    if (text.charAt(pos) == ' ') {
                        pos++;
                    }
                    currentUnit.setOffsetStart(pos);
                    if (text.charAt(endPos-1) == ' ')
                        endPos--;
                    currentUnit.setOffsetEnd(endPos);
                    currentUnit.setRawName(clusterContent);
                    break;
                case QUANTITY_OTHER:
                    break; 
                default:
                    logger.error("Warning: unexpected label in quantity parser: " + clusterLabel + " for " + clusterContent);
            }
            pos = endPos;
        }

        if (isMeasurementValid(currentMeasurement)) {
            measurements.add(currentMeasurement);
        }

        measurements = postCorrection(measurements);

        return measurements;
    }

    /**
     * Check the wellformness of a given list of measurements. 
     * In particular, if intervals are not consistent, they are transformed 
     * in atomic value measurements. 
     */
    private List<Measurement> postCorrection(List<Measurement> measurements) {
        List<Measurement> newMeasurements = new ArrayList<Measurement>();

        for(Measurement measurement : measurements) {
            if (measurement.getType() == UnitUtilities.Measurement_Type.VALUE) {
                newMeasurements.add(measurement);
            }
            else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL) {
                List<Quantity> quantities = measurement.getQuantities();
                if ( (quantities.size() == 1) || (measurement.getQuantityLeast() == null) || (measurement.getQuantityMost() == null) ) {
                    Measurement newMeasurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
                    Quantity quantity = null;
                    if (quantities.size() == 1)
                        quantity = measurement.getQuantityLeast();
                    else if ( (quantities.size() == 2)  && (quantities.get(0) == null) )
                        quantity = quantities.get(1);
                    else if (quantities.size() == 2)
                        quantity = measurement.getQuantityLeast();
                    if (quantity != null) {
                        newMeasurement.setAtomicQuantity(quantity);
                        newMeasurements.add(newMeasurement);
                    }
                }
                else if ( (quantities.size() == 2) && (measurement.getQuantityLeast() != null) && (measurement.getQuantityMost() != null) ) {
                    // if the interval is expressed over a chunck of text which is too large, it is a recognition error
                    // and we can replace it by two atomic measurements
                    Quantity quantityLeast = measurement.getQuantityLeast();
                    Quantity quantityMost = measurement.getQuantityMost();
                    int startL = quantityLeast.getOffsetStart();
                    int endL = quantityLeast.getOffsetEnd();
                    int startM = quantityMost.getOffsetStart();
                    int endM = quantityMost.getOffsetEnd();

                    if ( (Math.abs(endL-startM) > 80) && (Math.abs(endM-startL) > 80) ) {
                        // we replace the interval measurement by two atomic measurements
                        Measurement newMeasurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
                        newMeasurement.setAtomicQuantity(quantityLeast);
                        newMeasurements.add(newMeasurement);
                        newMeasurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
                        newMeasurement.setAtomicQuantity(quantityMost);
                        newMeasurements.add(newMeasurement);
                    }
                    else
                        newMeasurements.add(measurement);
                }
                else
                    newMeasurements.add(measurement);
            }
            else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                newMeasurements.add(measurement);
            }
        }

        return newMeasurements;
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
            Element root = getTEIHeader(id);
            Element textNode = teiElement("text");
            // for the moment we suppose we have english only...
            textNode.addAttribute(new Attribute("xml:lang", "http://www.w3.org/XML/1998/namespace", "en"));

            // we process the text paragraph by paragraph
            String lines[] = text.split("\n");
            StringBuilder paragraph = new StringBuilder();
            List<Measurement> measurements = null;
            for(int i=0; i<lines.length; i++) {
                String line = lines[i].trim();
                if (line.length() != 0) {
                    paragraph.append(line).append("\n");
                }
                if ( ((line.length() == 0) || (i == lines.length-1)) && (paragraph.length() > 0) ) {
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
                    measurements = resultExtraction(text, res, tokenizations);
                    if (measurements != null) {
                        System.out.println("\n");
                        for (Measurement measurement : measurements) {
                            System.out.println(measurement.toString());
                        }
                    }

                    textNode.appendChild(trainingExtraction(measurements, text, tokenizations));
                    paragraph = new StringBuilder();
                }
            }

            root.appendChild(textNode);
            System.out.println(XmlBuilderUtils.toXml(root));
            try {
                FileUtils.writeStringToFile(new File(pathTEI), XmlBuilderUtils.toXml(root));
            }
            catch(IOException e) {
                throw new GrobidException("Cannot create training data because output file can not be accessed: " + pathTEI);
            }
        }
    }

    @SuppressWarnings({"UnusedParameters"})
    public int createTrainingBatch(String inputDirectory,
                                   String outputDirectory, 
                                   int ind) throws IOException {
        try {
            File path = new File(inputDirectory);
            if (!path.exists()) {
                throw new GrobidException("Cannot create training data because input directory can not be accessed: " + inputDirectory);
            }

            File pathOut = new File(outputDirectory);
            if (!pathOut.exists()) {
                throw new GrobidException("Cannot create training data because ouput directory can not be accessed: " + outputDirectory);
            }

            // we process all pdf files in the directory
            File[] refFiles = path.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    System.out.println(name);
                    return name.endsWith(".pdf") || name.endsWith(".PDF") || 
                           name.endsWith(".txt") || name.endsWith(".TXT") || 
                           name.endsWith(".xml") || name.endsWith(".tei"); 
                }
            });

            if (refFiles == null)
                return 0;

            System.out.println(refFiles.length + " files to be processed.");

            int n = 0;
            if (ind == -1) {
                // for undefined identifier (value at -1), we initialize it to 0
                n = 1;
            }
            for (final File file : refFiles) {
                try {
                    if (file.getAbsolutePath().endsWith(".txt")) {
                        String pathTEI = outputDirectory + "/" + file.getName().replace(".txt", ".training.tei.xml");
                        createTraining(file.getAbsolutePath(), pathTEI, n);
                    }
                } catch (final Exception exp) {
                    logger.error("An error occured while processing the following pdf: " 
                        + file.getPath() + ": " + exp);
                }
                if (ind != -1)
                    n++;
            }

            return refFiles.length;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occured while running Grobid batch.", exp);
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

    private Element trainingExtraction(List<Measurement> measurements, String text, List<LayoutToken> tokenizations) {
        Element p = teiElement("p");

        int pos = 0;
        for(Measurement measurement : measurements) {
            Element measure = teiElement("measure");
            List<Quantity> quantities = measurement.getQuantities();
            if (measurement.getType() == UnitUtilities.Measurement_Type.VALUE) {
                measure.addAttribute(new Attribute("type", "value"));
                if (quantities.size() != 1)
                    continue;
                Quantity quantity = quantities.get(0);
                int startQ = quantity.getOffsetStart();
                int endQ = quantity.getOffsetEnd();

                Element numNode = teiElement("num");
                numNode.appendChild(text.substring(startQ, endQ));

                Unit unit = quantity.getRawUnit();
                int startU = -1;
                int endU = -1; 
                Element unitNode = null;
                if (unit != null) {
                    startU = unit.getOffsetStart();
                    endU = unit.getOffsetEnd();

                    unitNode = teiElement("measure");
                    unitNode.addAttribute(new Attribute("type", "?"));
                    unitNode.addAttribute(new Attribute("unit", "?"));
                    unitNode.appendChild(text.substring(startU, endU));
                }

                int initPos = pos;
                int firstPos = pos;
                while(pos < text.length()) {
                    if (pos == startQ) {
                        if (initPos == firstPos)
                           p.appendChild(text.substring(firstPos, startQ));
                       else
                           measure.appendChild(text.substring(initPos, startQ));
                        measure.appendChild(numNode);
                        pos = endQ;
                        initPos = pos;
                    }
                    if (pos == startU) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startU));
                        else
                            measure.appendChild(text.substring(initPos, startU));
                        measure.appendChild(unitNode);
                        pos = endU;
                        initPos = pos;
                    }

                    if ( (pos >= endQ) && (pos >= endU) ) 
                        break; 
                    pos++;
                }
            }
            else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL) {
                measure.addAttribute(new Attribute("type", "interval"));
                if (quantities.size() != 2)
                    continue;
                Quantity quantityLeast = quantities.get(0);
                Quantity quantityMost = quantities.get(1);

                int startQL = quantityLeast.getOffsetStart();
                int endQL = quantityLeast.getOffsetEnd();

                Element numNodeL = teiElement("num");
                numNodeL.addAttribute(new Attribute("atLeast", "?"));
                numNodeL.appendChild(text.substring(startQL, endQL));

                Unit unitL = quantityLeast.getRawUnit();
                int startUL = -1;
                int endUL = -1;

                Element unitNodeL = null;
                if (unitL != null) {
                    startUL = unitL.getOffsetStart();
                    endUL = unitL.getOffsetEnd();

                    unitNodeL = teiElement("measure");
                    unitNodeL.addAttribute(new Attribute("type", "?"));
                    unitNodeL.addAttribute(new Attribute("unit", "?"));
                    unitNodeL.appendChild(text.substring(startUL, endUL));
                }

                int startQM = quantityMost.getOffsetStart();
                int endQM = quantityMost.getOffsetEnd();
                Unit unitM = quantityMost.getRawUnit();

                Element numNodeM = teiElement("num");
                numNodeM.addAttribute(new Attribute("atMost", "?"));
                numNodeM.appendChild(text.substring(startQM, endQM));

                int startUM = -1;
                int endUM = -1;
                Element unitNodeM = null;
                if (unitM != null) {
                    startUM = unitM.getOffsetStart();
                    endUM = unitM.getOffsetEnd();
                    
                    unitNodeM = teiElement("measure");
                    unitNodeM.addAttribute(new Attribute("type", "?"));
                    unitNodeM.addAttribute(new Attribute("unit", "?"));
                    unitNodeM.appendChild(text.substring(startUM, endUM));
                }

                int initPos = pos;
                int firstPos = pos;
                while(pos < text.length()) {
                    if (pos == startQL) {
                        if (initPos == firstPos)
                           p.appendChild(text.substring(firstPos, startQL));
                       else
                           measure.appendChild(text.substring(initPos, startQL));
                        measure.appendChild(numNodeL);
                        pos = endQL;
                        initPos = pos;
                    }
                    if (pos == startQM) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startQM));
                        else
                            measure.appendChild(text.substring(initPos, startQM));
                        measure.appendChild(numNodeM);
                        pos = endQM;
                        initPos = pos;
                    }
                    if (pos == startUL) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startUL));
                        else
                            measure.appendChild(text.substring(initPos, startUL));
                        measure.appendChild(unitNodeL);
                        pos = endUL;
                        initPos = pos;
                    }
                    if ((pos == startUM) && (startUM != startUL) ) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startUM));
                        else
                            measure.appendChild(text.substring(initPos, startUM));
                        measure.appendChild(unitNodeM);
                        pos = endUM;
                        initPos = pos;
                    }

                    if ( (pos >= endQL) && (pos >= endQM) && (pos >= endUL) && (pos >= endUM) )
                        break; 
                    pos++;
                }
            }
            else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                measure.addAttribute(new Attribute("type", "list"));
                for(Quantity quantity : quantities) {
                    int startQ = quantity.getOffsetStart();
                    int endQ = quantity.getOffsetEnd();

                    Element numNode = teiElement("num");
                    numNode.appendChild(text.substring(startQ, endQ));

                    Unit unit = quantity.getRawUnit();
                    int startU = -1;
                    int endU = -1; 
                    Element unitNode = null;
                    if (unit != null) {
                        startU = unit.getOffsetStart();
                        endU = unit.getOffsetEnd();

                        unitNode = teiElement("measure");
                        unitNode.addAttribute(new Attribute("type", "?"));
                        unitNode.addAttribute(new Attribute("unit", "?"));
                        unitNode.appendChild(text.substring(startU, endU));
                    }

                    int initPos = pos;
                    int firstPos = pos;
                    while(pos < text.length()) {
                        if (pos == startQ) {
                            if (initPos == firstPos)
                               p.appendChild(text.substring(firstPos, startQ));
                           else
                               measure.appendChild(text.substring(initPos, startQ));
                            measure.appendChild(numNode);
                            pos = endQ;
                            initPos = pos;
                        }
                        if (pos == startU) {
                            if (initPos == firstPos)
                                p.appendChild(text.substring(firstPos, startU));
                            else
                                measure.appendChild(text.substring(initPos, startU));
                            measure.appendChild(unitNode);
                            pos = endU;
                            initPos = pos;
                        }

                        if ( (pos >= endQ) && (pos >= endU) ) 
                            break; 
                        pos++;
                    }
                }
            }
            p.appendChild(measure);    
        }
        p.appendChild(text.substring(pos, text.length()));
        
        return p;    
    }

    private boolean isMeasurementValid(Measurement currentMeasurement) {
        return currentMeasurement.getType() != null &&
                currentMeasurement.getQuantities() != null && currentMeasurement.getQuantities().size() > 0;
	}

    private Element getTEIHeader(int id) {
        Element tei = teiElement("tei");
        Element teiHeader = teiElement("teiHeader");

        if (id != -1) {
            Element fileDesc = teiElement("fileDesc");
            fileDesc.addAttribute(new Attribute("xml:id", "http://www.w3.org/XML/1998/namespace", "_" + id));
            teiHeader.appendChild(fileDesc);
        }

        Element encodingDesc = teiElement("encodingDesc");

        Element appInfo = teiElement("appInfo");

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);
        String dateISOString = df.format(new java.util.Date());

        Element application = teiElement("application");
        application.addAttribute(new Attribute("version", GrobidProperties.getVersion()));
        application.addAttribute(new Attribute("ident", "GROBID"));
        application.addAttribute(new Attribute("when", dateISOString));

        Element ref = teiElement("ref");
        ref.addAttribute(new Attribute("target", "https://github.com/kermitt2/grobid"));
        ref.appendChild("A machine learning software for extracting information from scholarly documents");

        application.appendChild(ref);
        appInfo.appendChild(application);
        encodingDesc.appendChild(appInfo);
        teiHeader.appendChild(encodingDesc);
        tei.appendChild(teiHeader);

        return tei;
    }
}
