package org.grobid.core.engines;

import com.google.common.collect.Iterables;
import nu.xom.Attribute;
import nu.xom.Element;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.normalization.NormalizationException;
import org.grobid.core.data.normalization.QuantityNormalizer;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.QuantitiesTaggingLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.label.TaggingLabels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.features.FeaturesVectorQuantities;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.sax.TextChunkSaxHandler;
import org.grobid.core.tokenization.LabeledTokensContainer;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;
import static org.grobid.core.document.xml.XmlBuilderUtils.teiElement;
import static org.grobid.core.engines.label.QuantitiesTaggingLabels.*;

/**
 * Quantity/measurement extraction.
 *
 * @author Patrice Lopez
 */
public class QuantityParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuantityParser.class);

    private static volatile QuantityParser instance;
    private ValueParser valueParser = ValueParser.getInstance();
    private SubstanceParser substanceParser = SubstanceParser.getInstance();
    private EngineParsers parsers;

    public static QuantityParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    /**
     * Create a new instance.
     */
    private static synchronized void getNewInstance() {
        instance = new QuantityParser();
    }

    private QuantityLexicon quantityLexicon = null;
    private MeasurementOperations measurementOperations = null;

    private QuantityParser() {
        super(QuantitiesModels.QUANTITIES);
        quantityLexicon = QuantityLexicon.getInstance();
        measurementOperations = new MeasurementOperations();
        parsers = new EngineParsers();
    }

    /**
     * Extract all occurrences of measurement/quantities from a simple piece of text.
     */
    public List<Measurement> extractQuantities(String text) throws Exception {
        if (isBlank(text)) {
            return null;
        }
        List<Measurement> measurements = new ArrayList<>();
        try {
            text = text.replace("\n", " ");
            text = text.replace("\t", " ");
            List<LayoutToken> tokens = null;
            try {
                tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(text);
            } catch (Exception e) {
                LOGGER.error("fail to tokenize:, " + text, e);
            }

            if ((tokens == null) || (tokens.size() == 0)) {
                return null;
            }

            String ress = null;
            List<String> texts = new ArrayList<>();
            for (LayoutToken token : tokens) {
                if (!token.getText().equals(" ") && !token.getText().equals("\t") && !token.getText().equals("\u00A0")) {
                    texts.add(token.getText());
                }
            }

            // to store unit term positions
            List<OffsetPosition> unitTokenPositions = quantityLexicon.inUnitNames(texts);
            ress = addFeatures(texts, unitTokenPositions);
            String res;
            try {
                res = label(ress);
            } catch (Exception e) {
                throw new GrobidException("CRF labeling for quantity parsing failed.", e);
            }

            measurements = extractMeasurement(text, res, tokens);
            measurements = normalizeMeasurements(measurements);
            measurements = substanceParser.parseSubstance(text, measurements);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }

        return measurements;
    }

    public Pair<List<Measurement>, Document> extractQuantitiesPDF(File file) throws IOException {
        List<Measurement> measurements = new ArrayList<>();
        Document doc = null;
        try {
            GrobidAnalysisConfig config =
                    new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder()
                            .build();
            DocumentSource documentSource =
                    DocumentSource.fromPdf(file, config.getStartPage(), config.getEndPage());
            doc = parsers.getSegmentationParser().processing(documentSource, config);

            // In the following, we process the relevant textual content of the document

            // for refining the process based on structures, we need to filter
            // segment of interest (e.g. header, body, annex) and possibly apply 
            // the corresponding model to further filter by structure types 

            // from the header, we are interested in title, abstract and keywords
            SortedSet<DocumentPiece> documentParts = doc.getDocumentPart(SegmentationLabel.HEADER);
            if (documentParts != null) {
                String header = parsers.getHeaderParser().getSectionHeaderFeatured(doc, documentParts, true);
                List<LayoutToken> tokenizationHeader = doc.getTokenizationParts(documentParts, doc.getTokenizations());
                String labeledResult = null;
                if ((header != null) && (header.trim().length() > 0)) {
                    labeledResult = parsers.getHeaderParser().label(header);

                    BiblioItem resHeader = new BiblioItem();
                    //parsers.getHeaderParser().processingHeaderSection(false, doc, resHeader);
                    resHeader.generalResultMapping(doc, labeledResult, tokenizationHeader);

                    // title
                    List<LayoutToken> titleTokens = resHeader.getLayoutTokens(TaggingLabels.HEADER_TITLE);
                    if (titleTokens != null) {
                        processLayoutTokenSequence(titleTokens, measurements);
                    }

                    // abstract
                    List<LayoutToken> abstractTokens = resHeader.getLayoutTokens(TaggingLabels.HEADER_ABSTRACT);
                    if (abstractTokens != null) {
                        processLayoutTokenSequence(abstractTokens, measurements);
                    }

                    // keywords
                    List<LayoutToken> keywordTokens = resHeader.getLayoutTokens(TaggingLabels.HEADER_KEYWORD);
                    if (keywordTokens != null) {
                        processLayoutTokenSequence(keywordTokens, measurements);
                    }
                }
            }

            // we can process all the body, in the future figure and table could be the 
            // object of more refined processing
            documentParts = doc.getDocumentPart(SegmentationLabel.BODY);
            if (documentParts != null) {
                Pair<String, LayoutTokenization> featSeg = parsers.getFullTextParser().getBodyTextFeatured(doc, documentParts);

                String fulltextTaggedRawResult = null;
                if (featSeg != null) {
                    String featureText = featSeg.getA();
                    LayoutTokenization layoutTokenization = featSeg.getB();

                    if (StringUtils.isNotEmpty(featureText)) {
                        fulltextTaggedRawResult = parsers.getFullTextParser().label(featureText);
                    }

                    TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FULLTEXT, fulltextTaggedRawResult,
                            layoutTokenization.getTokenization(), true);

                    //Iterate and exclude figures and tables
                    for (TaggingTokenCluster cluster : Iterables.filter(clusteror.cluster(),
                            new TaggingTokenClusteror
                                    .LabelTypeExcludePredicate(TaggingLabels.FIGURE, TaggingLabels.TABLE,
                                    TaggingLabels.EQUATION))) {

                        final List<LabeledTokensContainer> labeledTokensContainers = cluster.getLabeledTokensContainers();

                        // extract all the layout tokens from the cluster as a list
                        List<LayoutToken> tokens = labeledTokensContainers.stream()
                                .map(LabeledTokensContainer::getLayoutTokens)
                                .flatMap(List::stream)
                                .collect(Collectors.toList());

                        processLayoutTokenSequence(tokens, measurements);

                    }
                }
            }

            // we don't process references (although reference titles could be relevant)
            // acknowledgement? 

            // we can process annexes
            documentParts = doc.getDocumentPart(SegmentationLabel.ANNEX);
            if (documentParts != null) {
                processDocumentPart(documentParts, doc, measurements);
            }

        } catch (Exception e) {
            throw new GrobidException("Cannot process pdf file: " + file.getPath(), e);
        }

        // for next line, comparable measurement needs to be implemented
        //Collections.sort(measurements);
        return new Pair<List<Measurement>, Document>(measurements, doc);
    }

    /**
     * Process with the quantity model a segment coming from the segmentation model
     */
    private List<Measurement> processDocumentPart(SortedSet<DocumentPiece> documentParts,
                                                  Document doc,
                                                  List<Measurement> measurements) {
        // List<LayoutToken> for the selected segment
        List<LayoutToken> tokenizationParts = doc.getTokenizationParts(documentParts, doc.getTokenizations());
        return processLayoutTokenSequence(tokenizationParts, measurements);
    }

    /**
     * Process with the quantity model an arbitrary sequence of LayoutToken objects
     */
    private List<Measurement> processLayoutTokenSequence(List<LayoutToken> layoutTokens,
                                                         List<Measurement> measurements) {
        // List<LayoutToken> for the selected segment
        List<LayoutToken> tokenizationParts = layoutTokens;

        // text of the selected segment
        String text = LayoutTokensUtil.toText(layoutTokens);

        // we need to retokenize according to the QuantityAnalyzer (which tokenize
        // more than the default grobid-core analyzer)
        tokenizationParts = QuantityAnalyzer.getInstance().retokenizeLayoutTokens(tokenizationParts);

        // list of textual tokens of the selected segment
        List<String> texts = getTexts(tokenizationParts);

        if ((texts == null) || (texts.size() == 0))
            return measurements;

        // positions for lexical match
        List<OffsetPosition> unitTokenPositions = quantityLexicon.inUnitNames(texts);

        // string representation of the feature matrix for CRF lib
        String ress = addFeatures(texts, unitTokenPositions);

        if ((ress == null) || (ress.trim().length() == 0))
            return measurements;

        // labeled result from CRF lib
        String res = null;
        try {
            res = label(ress);
        } catch (Exception e) {
            throw new GrobidException("CRF labeling for quantity parsing failed.", e);
        }

        List<Measurement> localMeasurements = extractMeasurement(text, res, tokenizationParts);
        if ((localMeasurements == null) || (localMeasurements.size() == 0))
            return measurements;

        localMeasurements = normalizeMeasurements(localMeasurements);
        localMeasurements = substanceParser.parseSubstance(text, localMeasurements);

        measurements.addAll(localMeasurements);

        return measurements;
    }

    /**
     * Give the list of textual tokens from a list of LayoutToken
     */
    private static List<String> getTexts(List<LayoutToken> tokenizations) {
        List<String> texts = new ArrayList<>();
        for (LayoutToken token : tokenizations) {
            if (isNotEmpty(trim(token.getText())) &&
                    !token.getText().equals(" ") &&
                    !token.getText().equals("\n") &&
                    !token.getText().equals("\r") &&
                    !token.getText().equals("\t") &&
                    !token.getText().equals("\u00A0")) {
                texts.add(token.getText());
            }
        }
        return texts;
    }

    public List<Measurement> normalizeMeasurements(List<Measurement> measurements) {
        for (Measurement measurement : measurements) {
            if (measurement.getType() == null)
                continue;
            else if (measurement.getType() == UnitUtilities.Measurement_Type.VALUE) {
                normalizeQuantity(measurement.getQuantityAtomic());
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
                normalizeQuantity(measurement.getQuantityLeast());
                normalizeQuantity(measurement.getQuantityMost());
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
                normalizeQuantity(measurement.getQuantityBase());
                normalizeQuantity(measurement.getQuantityRange());
                // the two quantities below are normally not yet set-up
                Quantity quantityLeast = calculateQuantityLeast(measurement.getQuantityBase(), measurement.getQuantityRange());
                if (quantityLeast != null) {
                    measurement.setQuantityLeast(quantityLeast);
                }
                Quantity quantityMost = calculateQuantityMost(measurement.getQuantityBase(), measurement.getQuantityRange());
                if (quantityMost != null) {
                    measurement.setQuantityMost(quantityMost);
                }
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                if (measurement.getQuantityList() != null) {

                    for (Quantity quantity : measurement.getQuantityList()) {

                        //                if (isNotEmpty(quantity.getRawValue())) {
                        //                    String[] parsed = normalizationWrapper.parseRawString(quantity.getRawValue());
                        //                    if (quantity.getRawUnit() == null) {
                        //                        Unit raw = new Unit();
                        //                        raw.setRawName(parsed[1]);
                        //                        quantity.setRawUnit(raw);
                        //                        quantity.setRawValue(parsed[0]);
                        //                    }
                        //                }

                        normalizeQuantity(quantity);
                    }
                }
            }
        }
        return measurements;
    }

    private void normalizeQuantity(Quantity quantity) {
        QuantityNormalizer quantityNormalizer = new QuantityNormalizer();
        if (quantity == null)
            return;
        if (quantity.isNormalized())
            return;
        try {
            Quantity.Normalized quantity1 = quantityNormalizer.normalizeQuantity(quantity);
            if (quantity1 != null) {
                quantity.setNormalizedQuantity(quantity1);
            }
        } catch (NormalizationException ne) {
            final String rawName = quantity.getRawUnit() != null ? quantity.getRawUnit().getRawName() : null;
            LOGGER.warn("Could not normalize the value: '" + quantity.getRawValue()
                    + "' with unit '" + rawName + "'. ", ne.getMessage());
        }
    }

    private Quantity calculateQuantityLeast(Quantity quantityBase, Quantity quantityRange) {
        if ((quantityBase == null) || (quantityRange == null)) {
            return null;
        }
        if ((quantityBase.getParsedValue() == null) || (quantityRange.getParsedValue() == null)) {
            return null;
        }
        Quantity quantityLeast = new Quantity();
        BigDecimal value = quantityBase.getParsedValue().subtract(quantityRange.getParsedValue());
        quantityLeast.setParsedValue(value);
        quantityLeast.setRawValue(value.toString());
        quantityLeast.setParsedUnit(quantityBase.getParsedUnit());
        if ((quantityBase.isNormalized()) && (quantityRange.isNormalized())) {
            Quantity.Normalized normalizedQuantity = new Quantity().new Normalized();
            normalizedQuantity.setValue(quantityBase.getNormalizedQuantity().getValue().subtract(quantityRange.getNormalizedQuantity().getValue()));
            normalizedQuantity.setUnit(quantityBase.getNormalizedQuantity().getUnit());
            quantityLeast.setNormalizedQuantity(normalizedQuantity);
        }
        quantityLeast.setRawUnit(quantityBase.getRawUnit());
        quantityLeast.setOffsetStart(quantityBase.getOffsetStart());
        quantityLeast.setOffsetEnd(quantityBase.getOffsetEnd());
        return quantityLeast;
    }

    private Quantity calculateQuantityMost(Quantity quantityBase, Quantity quantityRange) {
        if ((quantityBase == null) || (quantityRange == null))
            return null;
        if ((quantityBase.getParsedValue() == null) || (quantityRange.getParsedValue() == null))
            return null;

        Quantity quantityMost = new Quantity();
        BigDecimal value = quantityBase.getParsedValue().add(quantityRange.getParsedValue());
        quantityMost.setParsedValue(value);
        quantityMost.setRawValue(value.toString());
        quantityMost.setParsedUnit(quantityBase.getParsedUnit());
        if ((quantityBase.isNormalized()) && (quantityRange.isNormalized())) {
            Quantity.Normalized normalizedQuantity = new Quantity().new Normalized();
            normalizedQuantity.setValue(quantityBase.getNormalizedQuantity().getValue().add(quantityRange.getNormalizedQuantity().getValue()));
            normalizedQuantity.setUnit(quantityBase.getNormalizedQuantity().getUnit());
            quantityMost.setNormalizedQuantity(normalizedQuantity);
        }
        quantityMost.setRawUnit(quantityRange.getRawUnit());
        quantityMost.setOffsetStart(quantityRange.getOffsetStart());
        quantityMost.setOffsetEnd(quantityRange.getOffsetEnd());
        return quantityMost;
    }

    public int batchProcess(String inputDirectory,
                            String outputDirectory,
                            boolean isRecursive) throws IOException {
        return 0;
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
                               int id) throws Exception {
        File file = new File(inputFile);
        if (!file.exists()) {
            throw new GrobidException("Cannot create training data because input file can not be accessed: " + inputFile);
        }

        Element root = getTEIHeader(id);
        if (inputFile.endsWith(".txt") || inputFile.endsWith(".TXT")) {
            root = createTrainingText(file, root);
        } else if (inputFile.endsWith(".xml") || inputFile.endsWith(".XML") || inputFile.endsWith(".tei") || inputFile.endsWith(".TEI")) {
            root = createTrainingXML(file, root);
        } else if (inputFile.endsWith(".pdf") || inputFile.endsWith(".PDF")) {
            root = createTrainingPDF(file, root);
        }

        if (root != null) {
            //System.out.println(XmlBuilderUtils.toXml(root));
            try {
                FileUtils.writeStringToFile(new File(pathTEI), XmlBuilderUtils.toXml(root));
            } catch (IOException e) {
                throw new GrobidException("Cannot create training data because output file can not be accessed: " + pathTEI);
            }
        }
    }

    private Element createTrainingText(File file, Element root) throws IOException {
        String text = FileUtils.readFileToString(file);

        Element textNode = teiElement("text");
        // for the moment we suppose we have english only...
        textNode.addAttribute(new Attribute("xml:lang", "http://www.w3.org/XML/1998/namespace", "en"));

        // we process the text paragraph by paragraph
        String lines[] = text.split("\n");
        StringBuilder paragraph = new StringBuilder();
        List<Measurement> measurements = null;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.length() != 0) {
                paragraph.append(line).append("\n");
            }
            if (((line.length() == 0) || (i == lines.length - 1)) && (paragraph.length() > 0)) {
                // we have a new paragraph
                text = paragraph.toString().replace("\n", " ").replace("\r", " ").replace("\t", " ");
                List<LayoutToken> tokens = null;
                try {
                    tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(text);
                } catch (Exception e) {
                    LOGGER.error("fail to tokenize:, " + text, e);
                }

                if ((tokens == null) || (tokens.size() == 0))
                    continue;

                String ress = null;
                List<String> texts = new ArrayList<>();
                for (LayoutToken token : tokens) {
                    if (!token.getText().equals(" ") && !token.getText().equals("\t") && !token.getText().equals("\u00A0")) {
                        texts.add(token.getText());
                    }
                }

                // to store unit term positions
                List<OffsetPosition> unitTokenPositions = quantityLexicon.inUnitNames(texts);
                ress = addFeatures(texts, unitTokenPositions);
                String res = null;
                try {
                    res = label(ress);
                } catch (Exception e) {
                    throw new GrobidException("CRF labeling for quantity parsing failed.", e);
                }
                measurements = extractMeasurement(text, res, tokens);
                measurements = measurementOperations.resolveMeasurement(measurements);

                textNode.appendChild(trainingExtraction(measurements, text, tokens));
                paragraph = new StringBuilder();
            }
        }
        root.appendChild(textNode);

        return root;
    }

    private Element createTrainingXML(File file, Element root) throws IOException {
        List<Measurement> measurements = null;

        Element textNode = teiElement("text");
        // for the moment we suppose we have english only...
        textNode.addAttribute(new Attribute("xml:lang", "http://www.w3.org/XML/1998/namespace", "en"));

        try {
            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();

            TextChunkSaxHandler handler = new TextChunkSaxHandler();

            //get a new instance of parser
            SAXParser p = spf.newSAXParser();
            p.parse(file, handler);

            List<String> chunks = handler.getChunks();
            for (String text : chunks) {
                //text = text.replace("\n", " ").replace("\t", " ");
                if (text.trim().length() == 0)
                    continue;
                List<LayoutToken> tokenizations = null;
                try {
                    tokenizations = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(text);
                } catch (Exception e) {
                    LOGGER.error("fail to tokenize:, " + text, e);
                }

                if ((tokenizations == null) || (tokenizations.size() == 0))
                    continue;

                String ress = null;
                List<String> texts = new ArrayList<String>();
                for (LayoutToken token : tokenizations) {
                    if (!token.getText().equals(" ") && !token.getText().equals("\t") && !token.getText().equals("\u00A0")) {
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
                } catch (Exception e) {
                    throw new GrobidException("CRF labeling for quantity parsing failed.", e);
                }
                measurements = extractMeasurement(text, res, tokenizations);
                measurements = measurementOperations.resolveMeasurement(measurements);
                if (measurements != null) {
                    System.out.println("\n");
                    for (Measurement measurement : measurements) {
                        System.out.println(measurement.toString());
                    }
                    System.out.println("\n");
                }

                textNode.appendChild(trainingExtraction(measurements, text, tokenizations));
            }
            root.appendChild(textNode);
        } catch (Exception e) {
            throw new GrobidException("Cannot create training data because input XML file can not be parsed: " + file.getPath(), e);
        }

        return root;
    }

    private Element createTrainingPDF(File file, Element root) throws IOException {
        // first we apply GROBID fulltext model on the PDF to get the full text TEI
        Document teiDoc = null;
        try {
            GrobidAnalysisConfig config =
                    new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder()
                            .build();
            teiDoc = GrobidFactory.getInstance().createEngine().fullTextToTEIDoc(file, config);
        } catch (Exception e) {
            throw new GrobidException("Cannot create training data because GROBIL full text model failed on the PDF: " + file.getPath(), e);
        }
        if (teiDoc == null) {
            return null;
        }

        String teiXML = teiDoc.getTei();

        // we parse this TEI string similarly as for createTrainingXML

        List<Measurement> measurements = null;

        Element textNode = teiElement("text");
        // for the moment we suppose we have english only...
        textNode.addAttribute(new Attribute("xml:lang", "http://www.w3.org/XML/1998/namespace", "en"));

        try {
            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();

            TextChunkSaxHandler handler = new TextChunkSaxHandler();

            //get a new instance of parser
            SAXParser p = spf.newSAXParser();
            p.parse(new InputSource(new StringReader(teiXML)), handler);

            List<String> chunks = handler.getChunks();
            for (String text : chunks) {
                text = text.toString().replace("\n", " ").replace("\r", " ").replace("\t", " ");
                // the last one is a special "large" space missed by the regex "\\p{Space}+" used on the SAX parser
                if (text.trim().length() == 0)
                    continue;
                List<LayoutToken> tokenizations = null;
                try {
                    tokenizations = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(text);
                } catch (Exception e) {
                    LOGGER.error("fail to tokenize:, " + text, e);
                }
                if ((tokenizations == null) || (tokenizations.size() == 0))
                    continue;

                String ress = null;
                List<String> texts = new ArrayList<String>();
                for (LayoutToken token : tokenizations) {
                    if (!token.getText().equals(" ") && !token.getText().equals("\t") && !token.getText().equals("\u00A0")) {
                        texts.add(token.getText());
                    }
                }

                // to store unit term positions
                List<OffsetPosition> unitTokenPositions = quantityLexicon.inUnitNames(texts);
                ress = addFeatures(texts, unitTokenPositions);
                String res = null;
                try {
                    res = label(ress);
                } catch (Exception e) {
                    throw new GrobidException("CRF labeling for quantity parsing failed.", e);
                }
                measurements = extractMeasurement(text, res, tokenizations);
                measurements = measurementOperations.resolveMeasurement(measurements);

                textNode.appendChild(trainingExtraction(measurements, text, tokenizations));
            }
            root.appendChild(textNode);
        } catch (Exception e) {
            throw new GrobidException("Cannot create training data because input XML file can not be parsed: " + file.getPath(), e);
        }

        return root;
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
                            name.endsWith(".xml") || name.endsWith(".tei") ||
                            name.endsWith(".XML") || name.endsWith(".TEI");
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
                    String pathTEI = outputDirectory + "/" + file.getName().substring(0, file.getName().length() - 4) + ".training.tei.xml";
                    createTraining(file.getAbsolutePath(), pathTEI, n);
                } catch (final Exception exp) {
                    LOGGER.error("An error occured while processing the following pdf: "
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
                                quantityLexicon.inUnitDictionary(token), isUnitPattern,
                                quantityLexicon.isNumberToken(token));
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

    /**
     * Extract identified quantities from a labelled text.
     */
    public List<Measurement> extractMeasurement(String text,
                                                String result,
                                                List<LayoutToken> tokenizations) {
        List<Measurement> measurements = new ArrayList<>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(QuantitiesModels.QUANTITIES, result, tokenizations);
        List<TaggingTokenCluster> clusters = clusteror.cluster();

        Unit currentUnit = new Unit();
        Measurement currentMeasurement = new Measurement();
        UnitUtilities.Measurement_Type openMeasurement = null;

        int pos = 0; // position in term of characters for creating the offsets

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            List<LayoutToken> theTokens = cluster.concatTokens();
            String clusterContent = LayoutTokensUtil.toText(cluster.concatTokens()).trim();
            List<BoundingBox> boundingBoxes = null;

            if (!clusterLabel.equals(QUANTITY_OTHER))
                boundingBoxes = BoundingBoxCalculator.calculate(cluster.concatTokens());

            if ((pos < text.length() - 1) && (text.charAt(pos) == ' '))
                pos += 1;
            int endPos = pos;
            boolean start = true;
            for (LayoutToken token : theTokens) {
                if (token.getText() != null) {
                    if (start && token.getText().equals(" ")) {
                        pos++;
                        endPos++;
                        continue;
                    }
                    if (start)
                        start = false;
                    endPos += token.getText().length();
                }
            }

            if ((endPos > 0) && (endPos <= text.length()) && (text.charAt(endPos - 1) == ' '))
                endPos--;

            Quantity currentQuantity = null;

            if (clusterLabel.equals(QuantitiesTaggingLabels.QUANTITY_VALUE_ATOMIC)) {
                LOGGER.debug("atomic value: " + clusterContent);
                if (isMeasurementValid(currentMeasurement)) {
                    measurements.add(currentMeasurement);
                    currentMeasurement = new Measurement();
                    currentUnit = new Unit();
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                valueParser.parseValue(currentQuantity);
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.VALUE);
                if (currentUnit.getRawName() != null) {
                    currentQuantity.setRawUnit(currentUnit);
                    currentMeasurement.setAtomicQuantity(currentQuantity);
                    measurements.add(currentMeasurement);
                    currentMeasurement = new Measurement();
                    currentUnit = new Unit();
                    openMeasurement = null;
                } else {
                    // unit will be attached later
                    currentMeasurement.setAtomicQuantity(currentQuantity);
                    openMeasurement = UnitUtilities.Measurement_Type.VALUE;
                }
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_VALUE_LEAST)) {
                LOGGER.debug("value least: " + clusterContent);
                if ((openMeasurement != null) && (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX)) {
                    if (isMeasurementValid(currentMeasurement)) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                valueParser.parseValue(currentQuantity);
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                if (currentUnit.getRawName() != null) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.setQuantityLeast(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_VALUE_MOST)) {
                LOGGER.debug("value most: " + clusterContent);
                if ((openMeasurement != null) && (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX)) {
                    if (isMeasurementValid(currentMeasurement)) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                valueParser.parseValue(currentQuantity);
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                if (currentUnit.getRawName() != null) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.setQuantityMost(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_VALUE_BASE)) {
                LOGGER.debug("base value: " + clusterContent);
                if ((openMeasurement != null) && (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE)) {
                    if (isMeasurementValid(currentMeasurement)) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                valueParser.parseValue(currentQuantity);
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                if (currentUnit.getRawName() != null) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.setQuantityBase(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_VALUE_RANGE)) {
                LOGGER.debug("range value: " + clusterContent);
                if ((openMeasurement != null) && (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE)) {
                    if (isMeasurementValid(currentMeasurement)) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                valueParser.parseValue(currentQuantity);
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                if (currentUnit.getRawName() != null) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.setQuantityRange(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_VALUE_LIST)) {
                LOGGER.debug("value in list: " + clusterContent);
                if ((openMeasurement != null) && (openMeasurement != UnitUtilities.Measurement_Type.CONJUNCTION)) {
                    if (isMeasurementValid(currentMeasurement)) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        //currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                valueParser.parseValue(currentQuantity);
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);

                if (currentUnit.getRawName() != null) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.addQuantityList(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.CONJUNCTION);
                openMeasurement = UnitUtilities.Measurement_Type.CONJUNCTION;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_UNIT_LEFT)) {
                LOGGER.debug("unit (left attachment): " + clusterContent);
                currentUnit = new Unit();
                currentUnit.setRawName(clusterContent);
                currentUnit.setOffsetStart(pos);
                currentUnit.setOffsetEnd(endPos);

                if (openMeasurement == UnitUtilities.Measurement_Type.VALUE) {
                    if (currentMeasurement.getQuantityAtomic() != null) {
                        currentMeasurement.getQuantityAtomic().setRawUnit(currentUnit);
                        currentMeasurement.addBoundingBoxes(boundingBoxes);
                    }
                } else if (openMeasurement == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
                    if ((currentMeasurement.getQuantityMost() != null) &&
                            ((currentMeasurement.getQuantityMost().getRawUnit() == null) ||
                                    (currentMeasurement.getQuantityMost().getRawUnit().getRawName() == null))) {
                        currentMeasurement.getQuantityMost().setRawUnit(currentUnit);

                        if ((currentMeasurement.getQuantityLeast() != null) &&
                                ((currentMeasurement.getQuantityLeast().getRawUnit() == null) ||
                                        (currentMeasurement.getQuantityLeast().getRawUnit().getRawName() == null)))
                            currentMeasurement.getQuantityLeast().setRawUnit(currentUnit);

                        currentMeasurement.addBoundingBoxes(boundingBoxes);
                    }
                } else if (openMeasurement == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
                    if ((currentMeasurement.getQuantityRange() != null) &&
                            ((currentMeasurement.getQuantityRange().getRawUnit() == null) ||
                                    (currentMeasurement.getQuantityRange().getRawUnit().getRawName() == null))) {
                        currentMeasurement.getQuantityRange().setRawUnit(currentUnit);

                        if ((currentMeasurement.getQuantityBase() != null) &&
                                ((currentMeasurement.getQuantityBase().getRawUnit() == null) ||
                                        (currentMeasurement.getQuantityBase().getRawUnit().getRawName() == null)))
                            currentMeasurement.getQuantityBase().setRawUnit(currentUnit);

                        currentMeasurement.addBoundingBoxes(boundingBoxes);
                    }
                } else if (openMeasurement == UnitUtilities.Measurement_Type.CONJUNCTION) {
                    if ((currentMeasurement.getQuantityList() != null) && (currentMeasurement.getQuantityList().size() > 0)) {
                        for (Quantity quantity : currentMeasurement.getQuantityList()) {
                            if ((quantity != null) && ((quantity.getRawUnit() == null) ||
                                    (quantity.getRawUnit().getRawName() == null))) {
                                quantity.setRawUnit(currentUnit);
                                currentMeasurement.addBoundingBoxes(boundingBoxes);
                            } else if ((quantity == null) && (openMeasurement == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX)) {
                                // we skip the least value, but we can still for robustness attach the unit to the upper range quantity
                            } else
                                break;
                        }
                    }
                }
                currentUnit = new Unit();
                if (openMeasurement == UnitUtilities.Measurement_Type.VALUE) {
                    if (isMeasurementValid(currentMeasurement)) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        openMeasurement = null;
                    }
                } else if (openMeasurement == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
                    if (isMeasurementValid(currentMeasurement)) {
                        if ((currentMeasurement.getQuantityLeast() != null) &&
                                (currentMeasurement.getQuantityMost() != null)) {
                            measurements.add(currentMeasurement);
                            currentMeasurement = new Measurement();
                            openMeasurement = null;
                        }
                    }
                } else if (openMeasurement == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
                    if (isMeasurementValid(currentMeasurement)) {
                        if ((currentMeasurement.getQuantityBase() != null) &&
                                (currentMeasurement.getQuantityRange() != null)) {
                            measurements.add(currentMeasurement);
                            currentMeasurement = new Measurement();
                            openMeasurement = null;
                        }
                    }
                }
            } else if (clusterLabel.equals(QUANTITY_UNIT_RIGHT)) {
                LOGGER.debug("unit (right attachment): " + clusterContent);
                if ((openMeasurement == UnitUtilities.Measurement_Type.VALUE) || (openMeasurement == UnitUtilities.Measurement_Type.CONJUNCTION)) {
                    if (isMeasurementValid(currentMeasurement)) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        //currentUnit = new Unit();
                        openMeasurement = null;
                    }
                }
                currentUnit = new Unit();
                currentUnit.setRawName(clusterContent);
                currentUnit.setOffsetStart(pos);
                currentUnit.setOffsetEnd(endPos);
                currentUnit.setUnitRightAttachment(true);
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_OTHER)) {
            } else {
                LOGGER.error("Warning: unexpected label in quantity parser: " + clusterLabel.getLabel() + " for " + clusterContent);
            }

            pos = endPos;
        }

        if (isMeasurementValid(currentMeasurement)) {
            measurements.add(currentMeasurement);
        }

        measurements = MeasurementOperations.postCorrection(measurements);
        return measurements;
    }

    private boolean isMeasurementValid(Measurement currentMeasurement) {
        return ((currentMeasurement.getType() != null) && (
                ((currentMeasurement.getQuantityList() != null) &&
                        (currentMeasurement.getQuantityList().size() > 0)) ||
                        (currentMeasurement.getQuantityAtomic() != null) ||
                        ((currentMeasurement.getQuantityLeast() != null) || (currentMeasurement.getQuantityMost() != null)) ||
                        ((currentMeasurement.getQuantityBase() != null) || (currentMeasurement.getQuantityRange() != null))
        )
        );
    }

    private Element trainingExtraction(List<Measurement> measurements, String text, List<LayoutToken> tokenizations) {
        Element p = teiElement("p");

        int pos = 0;
        for (Measurement measurement : measurements) {
            Element measure = teiElement("measure");

            if (measurement.getType() == UnitUtilities.Measurement_Type.VALUE) {
                measure.addAttribute(new Attribute("type", "value"));
                Quantity quantity = measurement.getQuantityAtomic();
                if (quantity == null)
                    continue;

                int startQ = quantity.getOffsetStart();
                int endQ = quantity.getOffsetEnd();

                Element numNode = teiElement("num");
                numNode.appendChild(text.substring(startQ, endQ));

                Unit unit = quantity.getRawUnit();
                int startU = -1;
                int endU = -1;
                Element unitElement = null;
                if (unit != null) {
                    unitElement = unitToElement(text, unit);
                }

                int initPos = pos;
                int firstPos = pos;
                while (pos < text.length()) {
                    if (pos == startQ) {
                        if (initPos == firstPos) {
                            p.appendChild(text.substring(firstPos, startQ));
                        } else {
                            measure.appendChild(text.substring(initPos, startQ));
                        }
                        measure.appendChild(numNode);
                        pos = endQ;
                        initPos = pos;
                    }
                    if (pos == startU) {
                        if (initPos == firstPos) {
                            p.appendChild(text.substring(firstPos, startU));
                        } else {
                            measure.appendChild(text.substring(initPos, startU));
                        }
                        measure.appendChild(unitElement);
                        pos = endU;
                        initPos = pos;
                    }

                    if ((pos >= endQ) && (pos >= endU))
                        break;
                    pos++;
                }
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
                measure.addAttribute(new Attribute("type", "interval"));
                Quantity quantityLeast = measurement.getQuantityLeast();
                Quantity quantityMost = measurement.getQuantityMost();

                if ((quantityLeast == null) || (quantityMost == null))
                    continue;

                int startQL = quantityLeast.getOffsetStart();
                int endQL = quantityLeast.getOffsetEnd();

                Element numNodeL = teiElement("num");

                if (quantityLeast.getRawValue() != null)
                    numNodeL.addAttribute(new Attribute("atLeast", quantityLeast.getRawValue().trim()));
                else
                    numNodeL.addAttribute(new Attribute("atLeast", "?"));
                numNodeL.appendChild(text.substring(startQL, endQL));

                Unit unitL = quantityLeast.getRawUnit();
                int startUL = -1;
                int endUL = -1;

                Element unitElement = null;
                if (unitL != null) {
                    unitElement = unitToElement(text, unitL);
                }

                int startQM = quantityMost.getOffsetStart();
                int endQM = quantityMost.getOffsetEnd();
                Unit unitM = quantityMost.getRawUnit();

                Element numNodeM = teiElement("num");
                if (quantityMost.getRawValue() != null)
                    numNodeM.addAttribute(new Attribute("atMost", quantityMost.getRawValue().trim()));
                else
                    numNodeM.addAttribute(new Attribute("atMost", "?"));
                numNodeM.appendChild(text.substring(startQM, endQM));

                int startUM = -1;
                int endUM = -1;
                Element unitElementM = null;
                if (unitM != null) {
                    unitElementM = unitToElement(text, unitM);
                }

                int initPos = pos;
                int firstPos = pos;
                while (pos < text.length()) {
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
                        measure.appendChild(unitElement);
                        pos = endUL;
                        initPos = pos;
                    }
                    if ((pos == startUM) && (startUM != startUL)) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startUM));
                        else
                            measure.appendChild(text.substring(initPos, startUM));
                        measure.appendChild(unitElementM);
                        pos = endUM;
                        initPos = pos;
                    }

                    if ((pos >= endQL) && (pos >= endQM) && (pos >= endUL) && (pos >= endUM))
                        break;
                    pos++;
                }
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                measure.addAttribute(new Attribute("type", "list"));
                List<Quantity> quantities = measurement.getQuantityList();
                for (Quantity quantity : quantities) {
                    int startQ = quantity.getOffsetStart();
                    int endQ = quantity.getOffsetEnd();

                    Element numNode = teiElement("num");
                    numNode.appendChild(text.substring(startQ, endQ));

                    Unit unit = quantity.getRawUnit();
                    int startU = -1;
                    int endU = -1;
                    Element unitNode = null;
                    if (unit != null) {
                        unitNode = unitToElement(text, unit);
                    }

                    int initPos = pos;
                    int firstPos = pos;
                    while (pos < text.length()) {
                        if (pos == startQ) {
                            if (initPos == firstPos) {
                                p.appendChild(text.substring(firstPos, startQ));
                            } else {
                                measure.appendChild(text.substring(initPos, startQ));
                            }
                            measure.appendChild(numNode);
                            pos = endQ;
                            initPos = pos;
                        }

                        if (pos == startU) {
                            if (initPos == firstPos) {
                                p.appendChild(text.substring(firstPos, startU));
                            } else {
                                measure.appendChild(text.substring(initPos, startU));
                            }
                            measure.appendChild(unitNode);
                            pos = endU;
                            initPos = pos;
                        }

                        if ((pos >= endQ) && (pos >= endU)) {
                            break;
                        }
                        pos++;
                    }
                }
            }
            p.appendChild(measure);
        }
        p.appendChild(text.substring(pos, text.length()));

        return p;
    }

    private Element unitToElement(String text, Unit unit) {
        int startU = unit.getOffsetStart();
        int endU = unit.getOffsetEnd();

        Element unitNode = teiElement("measure");

        if ((unit.getUnitDefinition() != null) && (unit.getUnitDefinition().getType() != null)) {
            unitNode.addAttribute(new Attribute("type", unit.getUnitDefinition().getType().toString()));
        } else {
            unitNode.addAttribute(new Attribute("type", "?"));
        }

        if (unit.getRawName() != null) {
            unitNode.addAttribute(new Attribute("unit", unit.getRawName().trim()));
        } else {
            unitNode.addAttribute(new Attribute("unit", "?"));
        }

        unitNode.appendChild(text.substring(startU, endU));
        return unitNode;
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
