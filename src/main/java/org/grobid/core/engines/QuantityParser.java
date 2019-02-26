package org.grobid.core.engines;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.*;
import org.grobid.core.data.normalization.NormalizationException;
import org.grobid.core.data.normalization.QuantityNormalizer;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.QuantitiesTaggingLabels;
import org.grobid.core.engines.label.SegmentationLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.label.TaggingLabels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorQuantities;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.tokenization.LabeledTokensContainer;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
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
    private QuantifiedObjectParser substanceParser;
    private QuantityNormalizer quantityNormalizer = new QuantityNormalizer();
    private EngineParsers parsers;
    private boolean disableSubstanceParser = false;

    public static QuantityParser getInstance(boolean disableSubstance) {
        if (instance == null) {
            instance = getNewInstance(disableSubstance);
        }
        return instance;
    }

    public static QuantityParser getInstance() {
        if (instance == null) {
            instance = getNewInstance(false);
        }
        return instance;
    }

    private static synchronized QuantityParser getNewInstance(boolean disableSubstanceParser) {
        QuantityParser instance = new QuantityParser();

        if (!disableSubstanceParser) {
            QuantifiedObjectParser substanceParser = QuantifiedObjectParser.getInstance();
            instance.setSubstanceParser(substanceParser);
        }
        instance.setDisableSubstanceParser(disableSubstanceParser);

        return instance;
    }

    private QuantityLexicon quantityLexicon = null;
    private MeasurementOperations measurementOperations = null;

    private QuantityParser() {
        super(QuantitiesModels.QUANTITIES);
        quantityLexicon = QuantityLexicon.getInstance();
        measurementOperations = new MeasurementOperations();
        parsers = new EngineParsers();
    }

    public List<Measurement> process(List<LayoutToken> layoutTokens) {

        List<Measurement> measurements = new ArrayList<>();

        //Normalisation
        layoutTokens.stream().map(layoutToken -> {
                    layoutToken.setText(UnicodeUtil.normaliseTextAndRemoveSpaces(layoutToken.getText()));

                    return layoutToken;
                }
        );

        // List<LayoutToken> for the selected segment
        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().retokenizeLayoutTokens(layoutTokens);

        // list of textual tokens of the selected segment
        //List<String> texts = getTexts(tokenizationParts);

        if (isEmpty(tokens))
            return measurements;

        try {
            // positions for lexical match
            List<OffsetPosition> unitTokenPositions = quantityLexicon.inUnitNames(tokens);

            // string representation of the feature matrix for CRF lib
            String ress = addFeatures(tokens, unitTokenPositions);

            if (StringUtils.isEmpty(ress))
                return measurements;

            // labeled result from CRF lib
            String res = null;
            try {
                res = label(ress);
            } catch (Exception e) {
                throw new GrobidException("CRF labeling for quantity parsing failed.", e);
            }

            List<Measurement> localMeasurements = extractMeasurement(tokens, res);
            if (isEmpty(localMeasurements))
                return measurements;

            localMeasurements = measurementOperations.resolveMeasurement(localMeasurements);
            try {
                localMeasurements = normalizeMeasurements(localMeasurements);
            } catch (Exception e) {
                LOGGER.error("Normalisation failed. Skipping it. ", e);
            }

            if (!disableSubstanceParser) {
                localMeasurements = substanceParser.parseSubstance(tokens, localMeasurements);
            } else {
                LOGGER.warn("Substance parser disabled, skpping it. ");
            }

            measurements.addAll(localMeasurements);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }

        return measurements;
    }

    /**
     * Extract all occurrences of measurement/quantities from a simple piece of text.
     */
    public List<Measurement> process(String text) {
        if (isBlank(text)) {
            return null;
        }

        text = text.replace("\r", " ");
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
        return process(tokens);
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
            SortedSet<DocumentPiece> documentParts = doc.getDocumentPart(SegmentationLabels.HEADER);
            if (documentParts != null) {
                org.apache.commons.lang3.tuple.Pair<String, List<LayoutToken>> headerStruct = parsers.getHeaderParser().getSectionHeaderFeatured(doc, documentParts, true);
                List<LayoutToken> tokenizationHeader = headerStruct.getRight();//doc.getTokenizationParts(documentParts, doc.getTokenizations());
                String header = headerStruct.getLeft();
                String labeledResult = null;
                if ((header != null) && (header.trim().length() > 0)) {
                    labeledResult = parsers.getHeaderParser().label(header);

                    BiblioItem resHeader = new BiblioItem();
                    //parsers.getHeaderParser().processingHeaderSection(false, doc, resHeader);
                    resHeader.generalResultMapping(doc, labeledResult, tokenizationHeader);

                    // title
                    List<LayoutToken> titleTokens = resHeader.getLayoutTokens(TaggingLabels.HEADER_TITLE);
                    if (titleTokens != null) {
                        measurements.addAll(process(titleTokens));
                    }

                    // abstract
                    List<LayoutToken> abstractTokens = resHeader.getLayoutTokens(TaggingLabels.HEADER_ABSTRACT);
                    if (abstractTokens != null) {
                        measurements.addAll(process(abstractTokens));
                    }

                    // keywords
                    List<LayoutToken> keywordTokens = resHeader.getLayoutTokens(TaggingLabels.HEADER_KEYWORD);
                    if (keywordTokens != null) {
                        measurements.addAll(process(keywordTokens));
                    }
                }
            }

            // we can process all the body, in the future figure and table could be the
            // object of more refined processing
            documentParts = doc.getDocumentPart(SegmentationLabels.BODY);
            if (documentParts != null) {
                org.apache.commons.lang3.tuple.Pair<String, LayoutTokenization> featSeg = parsers.getFullTextParser().getBodyTextFeatured(doc, documentParts);

                String fulltextTaggedRawResult = null;
                if (featSeg != null) {
                    String featureText = featSeg.getLeft();
                    LayoutTokenization layoutTokenization = featSeg.getRight();

                    if (StringUtils.isNotEmpty(featureText)) {
                        fulltextTaggedRawResult = parsers.getFullTextParser().label(featureText);
                    }

                    TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FULLTEXT, fulltextTaggedRawResult,
                            layoutTokenization.getTokenization(), true);

                    //Iterate and exclude figures and tables
                    for (TaggingTokenCluster cluster : Iterables.filter(clusteror.cluster(),
                            new TaggingTokenClusteror
                                    .LabelTypeExcludePredicate(TaggingLabels.TABLE_MARKER, TaggingLabels.EQUATION, TaggingLabels.CITATION_MARKER,
                                    TaggingLabels.FIGURE_MARKER, TaggingLabels.EQUATION_MARKER, TaggingLabels.EQUATION_LABEL))) {

                        if (cluster.getTaggingLabel().equals(TaggingLabels.FIGURE)) {
                            //apply the figure model to only get the caption
                            final Figure processedFigure = parsers.getFigureParser()
                                    .processing(cluster.concatTokens(), cluster.getFeatureBlock());
                            measurements.addAll(process(processedFigure.getCaptionLayoutTokens()));
                        } else if (cluster.getTaggingLabel().equals(TaggingLabels.TABLE)) {
                            //apply the table model to only get the caption/description 
                            final Table processedTable = parsers.getTableParser().processing(cluster.concatTokens(), cluster.getFeatureBlock());
                            measurements.addAll(process(processedTable.getFullDescriptionTokens()));
                        } else {
                            final List<LabeledTokensContainer> labeledTokensContainers = cluster.getLabeledTokensContainers();

                            // extract all the layout tokens from the cluster as a list
                            List<LayoutToken> tokens = labeledTokensContainers.stream()
                                    .map(LabeledTokensContainer::getLayoutTokens)
                                    .flatMap(List::stream)
                                    .collect(Collectors.toList());

                            measurements.addAll(process(tokens));
                        }

                    }
                }
            }

            // we don't process references (although reference titles could be relevant)
            // acknowledgement?

            // we can process annexes
            documentParts = doc.getDocumentPart(SegmentationLabels.ANNEX);
            if (documentParts != null) {
                measurements.addAll(processDocumentPart(documentParts, doc));
            }

        } catch (Exception e) {
            throw new GrobidException("Cannot process pdf file: " + file.getPath(), e);
        }

        // for next line, comparable measurement needs to be implemented
        //Collections.sort(measurements);
        return new Pair<>(measurements, doc);
    }

    /**
     * Process with the quantity model a segment coming from the segmentation model
     */
    private List<Measurement> processDocumentPart(SortedSet<DocumentPiece> documentParts,
                                                  Document doc) {
        // List<LayoutToken> for the selected segment
        List<LayoutToken> layoutTokens
                = doc.getTokenizationParts(documentParts, doc.getTokenizations());
        return process(layoutTokens);
    }

    /**
     * Give the list of textual tokens from a list of LayoutToken
     */
    /*private static List<String> getTexts(List<LayoutToken> tokenizations) {
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
    }*/
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
        BigDecimal value = quantityBase.getParsedValue().getNumeric().subtract(quantityRange.getParsedValue().getNumeric());
        quantityLeast.setParsedValue(new Value(value));
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
        BigDecimal value = quantityBase.getParsedValue().getNumeric().add(quantityRange.getParsedValue().getNumeric());
        quantityMost.setParsedValue(new Value(value));
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


    @SuppressWarnings({"UnusedParameters"})
    private String addFeatures(List<LayoutToken> tokens,
                               List<OffsetPosition> unitTokenPositions) {
        int totalLine = tokens.size();
        int posit = 0;
        int currentQuantityIndex = 0;
        List<OffsetPosition> localPositions = unitTokenPositions;
        boolean isUnitPattern = false;
        StringBuilder result = new StringBuilder();
        try {
            for (LayoutToken token : tokens) {
                if (token.getText().trim().equals("@newline")) {
                    result.append("\n");
                    continue;
                }

                String text = token.getText();
                if (text.equals(" ") || text.equals("\n")) {
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
                        FeaturesVectorQuantities.addFeaturesQuantities(text, null,
                                quantityLexicon.inUnitDictionary(text), isUnitPattern,
                                quantityLexicon.isNumberToken(text));
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
     * Extract identified quantities from a labeled text.
     */
    public List<Measurement> extractMeasurement(List<LayoutToken> tokens, String result) {
        List<Measurement> measurements = new ArrayList<>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(QuantitiesModels.QUANTITIES, result, tokens);
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
            String clusterContent = LayoutTokensUtil.toText(theTokens).trim();
            List<BoundingBox> boundingBoxes = null;

            if (!clusterLabel.equals(QUANTITY_OTHER))
                boundingBoxes = BoundingBoxCalculator.calculate(theTokens);

            String text = LayoutTokensUtil.toText(tokens);
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
                if (currentMeasurement.isValid()) {
                    measurements.add(currentMeasurement);
                    currentMeasurement = new Measurement();
                    currentUnit = new Unit();
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                final Value parsedValue = valueParser.parseValue(currentQuantity.getRawValue());
                if (parsedValue != null) {
                    currentQuantity.setParsedValue(parsedValue);
                }
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                currentQuantity.setLayoutTokens(theTokens);
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
                if (((openMeasurement != null) && (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX))
                        || currentMeasurement.getQuantityLeast() != null) {
                    if (currentMeasurement.isValid()) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                final Value parsedValue = valueParser.parseValue(currentQuantity.getRawValue());
                if (parsedValue != null) {
                    currentQuantity.setParsedValue(parsedValue);
                }
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                currentQuantity.setLayoutTokens(theTokens);
                if (currentUnit.getRawName() != null) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.setQuantityLeast(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_VALUE_MOST)) {
                LOGGER.debug("value most: " + clusterContent);
                if (((openMeasurement != null) && (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX))
                        || currentMeasurement.getQuantityMost() != null) {
                    if (currentMeasurement.isValid()) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                final Value parsedValue = valueParser.parseValue(currentQuantity.getRawValue());
                if (parsedValue != null) {
                    currentQuantity.setParsedValue(parsedValue);
                }
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                currentQuantity.setLayoutTokens(theTokens);
                if (currentUnit.getRawName() != null) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.setQuantityMost(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_VALUE_BASE)) {
                LOGGER.debug("base value: " + clusterContent);
                if (((openMeasurement != null) && (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE))
                        || currentMeasurement.getQuantityBase() != null) {
                    if (currentMeasurement.isValid()) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                final Value parsedValue = valueParser.parseValue(currentQuantity.getRawValue());
                if (parsedValue != null) {
                    currentQuantity.setParsedValue(parsedValue);
                }
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                currentQuantity.setLayoutTokens(theTokens);
                if (currentUnit.getRawName() != null) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.setQuantityBase(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_VALUE_RANGE)) {
                LOGGER.debug("range value: " + clusterContent);
                if (((openMeasurement != null) && (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE))
                        || currentMeasurement.getQuantityRange() != null) {
                    if (currentMeasurement.isValid()) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                final Value parsedValue = valueParser.parseValue(currentQuantity.getRawValue());
                if (parsedValue != null) {
                    currentQuantity.setParsedValue(parsedValue);
                }
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                currentQuantity.setLayoutTokens(theTokens);
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
                    if (currentMeasurement.isValid()) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        //currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity();
                currentQuantity.setRawValue(clusterContent);
                final Value parsedValue = valueParser.parseValue(currentQuantity.getRawValue());
                if (parsedValue != null) {
                    currentQuantity.setParsedValue(parsedValue);
                }
                currentQuantity.setOffsetStart(pos);
                currentQuantity.setOffsetEnd(endPos);
                currentQuantity.setLayoutTokens(theTokens);

                if (currentUnit.getRawName() != null) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.addQuantityToList(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.CONJUNCTION);
                openMeasurement = UnitUtilities.Measurement_Type.CONJUNCTION;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_UNIT_LEFT)) {
                LOGGER.debug("unit (left attachment): " + clusterContent);
                currentUnit = new Unit();
                currentUnit.setRawName(clusterContent);
                currentUnit.setOffsetStart(pos);
                currentUnit.setOffsetEnd(endPos);
                currentUnit.setLayoutTokens(theTokens);

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
                    if (currentMeasurement.isValid()) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        openMeasurement = null;
                    }
                } else if (openMeasurement == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
                    if (currentMeasurement.isValid()) {
                        if ((currentMeasurement.getQuantityLeast() != null) &&
                                (currentMeasurement.getQuantityMost() != null)) {
                            measurements.add(currentMeasurement);
                            currentMeasurement = new Measurement();
                            openMeasurement = null;
                        }
                    }
                } else if (openMeasurement == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
                    if (currentMeasurement.isValid()) {
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
                    if (currentMeasurement.isValid()) {
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
                currentUnit.setLayoutTokens(theTokens);
                currentUnit.setUnitRightAttachment(true);
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_OTHER)) {
            } else {
                LOGGER.error("Warning: unexpected label in quantity parser: " + clusterLabel.getLabel() + " for " + clusterContent);
            }

            pos = endPos;
        }

        if (currentMeasurement.isValid()) {
            measurements.add(currentMeasurement);
        }

        measurements = MeasurementOperations.postCorrection(measurements);
        return measurements;
    }

    public void setDisableSubstanceParser(boolean disableSubstanceParser) {
        this.disableSubstanceParser = disableSubstanceParser;
    }

    public void setSubstanceParser(QuantifiedObjectParser substanceParser) {
        this.substanceParser = substanceParser;
    }
}
