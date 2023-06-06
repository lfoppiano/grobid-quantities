package org.grobid.core.engines;

import com.google.common.collect.Iterables;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Layout;
import org.grobid.core.GrobidModel;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.Value;
import org.grobid.core.data.normalization.NormalizationException;
import org.grobid.core.data.normalization.QuantityNormalizer;
import org.grobid.core.data.normalization.UnitNormalizer;
import org.grobid.core.engines.label.QuantitiesTaggingLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorQuantities;
import org.grobid.core.lang.SentenceDetector;
import org.grobid.core.lang.impl.OpenNLPSentenceDetector;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.grobid.core.engines.label.QuantitiesTaggingLabels.*;
import static org.grobid.core.engines.utilities.LabellingUtils.correctLabelling;
import static org.grobid.core.utilities.QuantityOperations.getExtremitiesAsIndex;

/**
 * Quantity/measurement extraction.
 *
 * @author Patrice Lopez
 */
public class QuantityParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuantityParser.class);

    private static volatile QuantityParser instance;
    private ValueParser valueParser;
    private QuantifiedObjectParser quantifiedObjectParser;
    private QuantityNormalizer quantityNormalizer;
    //    private EnglishTokenizer tokeniser;
    private boolean disableSubstanceParser = false;
    public static Pattern MINUS_SIGN_REGEX = Pattern.compile("\u2212");

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
            instance.setQuantifiedObjectParser(substanceParser);
        }
        instance.setDisableSubstanceParser(disableSubstanceParser);

        return instance;
    }

    private QuantityLexicon quantityLexicon;

    private MeasurementOperations measurementOperations;

    protected QuantityParser(GrobidModel model, QuantityLexicon quantityLexicon, MeasurementOperations measurementOperations, ValueParser valueParser) {
        super(model);
        this.quantityLexicon = quantityLexicon;
        this.measurementOperations = measurementOperations;
        this.valueParser = valueParser;
        instance = this;
    }

    @Inject
    public QuantityParser() {
        super(QuantitiesModels.QUANTITIES);
        quantityLexicon = QuantityLexicon.getInstance();
        UnitNormalizer unitNormaliser = new UnitNormalizer();
        measurementOperations = new MeasurementOperations(unitNormaliser);
        quantityNormalizer = new QuantityNormalizer();

        valueParser = new ValueParser();
        instance = this;
//        this.tokeniser = new EnglishTokenizer();
    }

    public List<Measurement> process(List<LayoutToken> layoutTokens) {

        List<Measurement> measurements = new ArrayList<>();

        // List<LayoutToken> for the selected segment
        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().retokenizeLayoutTokens(layoutTokens);

        //Normalisation
        List<LayoutToken> layoutTokenNormalised = tokens.stream()
            .map(layoutToken -> {
                    layoutToken.setText(UnicodeUtil.normaliseText(layoutToken.getText()));
                    layoutToken.setText(MINUS_SIGN_REGEX.matcher(layoutToken.getText()).replaceAll("-"));

                    return layoutToken;
                }
            ).collect(Collectors.toList());

        // list of textual tokens of the selected segment
        //List<String> texts = getTexts(tokenizationParts);

        if (isEmpty(layoutTokenNormalised))
            return measurements;

        try {
            // positions for lexical match
            List<OffsetPosition> unitTokenPositions = quantityLexicon.inUnitNames(layoutTokenNormalised);

            // string representation of the feature matrix for CRF lib
            String ress = addFeatures(layoutTokenNormalised, unitTokenPositions);

            if (StringUtils.isEmpty(ress))
                return measurements;

            // labeled result from CRF lib
            String resultLabelling = null;
            try {
                resultLabelling = label(ress);
            } catch (Exception e) {
                throw new GrobidException("CRF labeling for quantity parsing failed.", e);
            }

            String fixedResultLabelling = correctLabelling(resultLabelling);

            List<OffsetPosition> sentences = getSentencesOffsets(layoutTokenNormalised);

            List<Measurement> localMeasurements = extractMeasurement(layoutTokenNormalised, fixedResultLabelling, sentences);
            if (isEmpty(localMeasurements))
                return measurements;

            localMeasurements = measurementOperations.resolveMeasurement(localMeasurements);
            try {
                localMeasurements = normalizeMeasurements(localMeasurements);
            } catch (Exception e) {
                LOGGER.error("Normalisation failed. Skipping it. ", e);
            }

            if (!disableSubstanceParser) {
                localMeasurements = quantifiedObjectParser.process(layoutTokenNormalised, localMeasurements);
            } else {
                LOGGER.warn("Substance parser disabled, skipping it. ");
            }

            measurements.addAll(localMeasurements);
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }

        return measurements;
    }

    protected List<OffsetPosition> getSentencesOffsets(List<LayoutToken> tokens) {
        SentenceDetector segmenter = new OpenNLPSentenceDetector();
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> results = segmenter.detect(text);

        if (CollectionUtils.isEmpty(results)) {
            results = Arrays.asList(new OffsetPosition(0, text.length()));
        }

        return results;
    }

    /**
     * Extract all occurrences of measurement/quantities from a simple piece of text.
     */
    public List<Measurement> process(String text) {
        if (isBlank(text)) {
            return null;
        }

        String textPreprocessed = text.replace("\r\n", " ");
//        textReplaced = textReplaced.replace("\n", " ");
//        textReplaced = textReplaced.replace("\t", " ");

        List<LayoutToken> tokens = null;
        try {
            tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(textPreprocessed);
        } catch (Exception e) {
            LOGGER.error("fail to tokenize:, " + text, e);
        }

        if (CollectionUtils.isEmpty(tokens)) {
            return null;
        }
        return process(tokens);
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

    public List<Measurement> extractMeasurement(List<LayoutToken> tokens, String result) {
        return extractMeasurement(tokens, result, Arrays.asList(new OffsetPosition(0, LayoutTokensUtil.toText(tokens).length())));
    }

    /**
     * Extract identified quantities from a labeled text.
     */
    public List<Measurement> extractMeasurement(List<LayoutToken> tokens, String result, List<OffsetPosition> sentences) {
        List<Measurement> measurements = new ArrayList<>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(QuantitiesModels.QUANTITIES, result, tokens);
        List<TaggingTokenCluster> clusters = clusteror.cluster();

        Unit currentUnit = new Unit();
        Measurement currentMeasurement = new Measurement();
        UnitUtilities.Measurement_Type openMeasurement = null;

        int currentSentenceIndex = 0;
        OffsetPosition currentSentence = sentences.get(currentSentenceIndex);
        LayoutToken defaultLayoutToken = new LayoutToken();
        ;
        ;
        defaultLayoutToken.setOffset(0);
        int offset = Iterables.getFirst(tokens, defaultLayoutToken).getOffset();

        int pos = 0; // position in terms of characters for creating the offsets

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

//            String text = LayoutTokensUtil.toText(tokens);
//            if ((pos < text.length() - 1) && (text.charAt(pos) == ' '))
//                pos += 1;
//            int endPos = pos;
//            boolean start = true;
//            for (LayoutToken token : theTokens) {
//                if (token.getText() != null) {
//                    if (start && token.getText().equals(" ")) {
//                        pos++;
//                        endPos++;
//                        continue;
//                    }
//                    if (start)
//                        start = false;
//                    endPos += token.getText().length();
//                }
//            }
//
//            if ((endPos > 0) && (endPos <= text.length()) && (text.charAt(endPos - 1) == ' '))
//                endPos--;

            Quantity currentQuantity = null;

            int startPos = theTokens.get(0).getOffset();
            int endPos = startPos + clusterContent.length();

            if (clusterLabel.equals(QuantitiesTaggingLabels.QUANTITY_VALUE_ATOMIC)) {
                LOGGER.debug("atomic value: " + clusterContent);
                if (currentMeasurement.isValid()) {
                    measurements.add(currentMeasurement);
                    currentMeasurement = new Measurement();
                    currentUnit = new Unit();
                }
                currentQuantity = new Quantity(clusterContent, null, startPos, endPos);
                final Value parsedValue = valueParser.parseValue(currentQuantity.getRawValue());
                if (parsedValue != null) {
                    currentQuantity.setParsedValue(parsedValue);
                }
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

                //I consider this quantity belonging to a new Measurement IF
                // 1) there is an open measurement AND the open measurement is of the same type
                // 3) the least quantity already present is not null OR
                // 4) the mostQuantity not in the same sentence
                if (openMeasurement != null) {
                    populateRawOffsetsAndText(currentMeasurement, tokens);
                    if (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX ||
                        currentMeasurement.getQuantityLeast() != null ||
                        !currentSentence.equals(findSentenceOffset(sentences, currentMeasurement, offset))
                    ) {
                        if (currentMeasurement.isValid()) {
                            measurements.add(currentMeasurement);
                            currentMeasurement = new Measurement();
                            currentUnit = new Unit();
                        }
                    } else {
                        Quantity quantityMost = currentMeasurement.getQuantityMost();

                        if (quantityMost != null) {
                            // Workaround - if the list item is more than 8 tokens from the previous, we start a new measurement
                            Pair<Integer, Integer> extremitiesAsIndex = getExtremitiesAsIndex(tokens, quantityMost.getOffsetEnd(), endPos);
                            int tokensInBetween = tokens.subList(extremitiesAsIndex.getLeft(), extremitiesAsIndex.getRight()).size();
                            if (tokensInBetween > 8 ||
                                !currentSentence.equals(findSentenceOffset(sentences, currentMeasurement, offset))) {
                                if (currentMeasurement.isValid()) {
                                    measurements.add(currentMeasurement);
                                    currentMeasurement = new Measurement();
                                    currentUnit = new Unit();
                                }
                            }
                        }
                    }
                }

                currentQuantity = new Quantity(clusterContent, null, startPos, endPos);
                final Value parsedValue = valueParser.parseValue(currentQuantity.getRawValue());
                if (parsedValue != null) {
                    currentQuantity.setParsedValue(parsedValue);
                }
                currentQuantity.setLayoutTokens(theTokens);
                if (currentUnit.getRawName() != null
                    && currentSentence.equals(findSentenceOffset(sentences, new OffsetPosition(currentUnit.getOffsetStart(), currentUnit.getOffsetEnd()), offset))) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.setQuantityLeast(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_VALUE_MOST)) {
                LOGGER.debug("value most: " + clusterContent);
                if (openMeasurement != null) {
                    populateRawOffsetsAndText(currentMeasurement, tokens);
                    if (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX ||
                        currentMeasurement.getQuantityMost() != null ||
                        !currentSentence.equals(findSentenceOffset(sentences, currentMeasurement, offset))) {
                        if (currentMeasurement.isValid()) {
                            measurements.add(currentMeasurement);
                            currentMeasurement = new Measurement();
                            currentUnit = new Unit();
                        }
                    } else {
                        Quantity quantityLeast = currentMeasurement.getQuantityLeast();

                        if (quantityLeast != null) {
                            // Workaround - if the list item is more than 8 tokens from the previous, we start a new measurement
                            Pair<Integer, Integer> extremitiesAsIndex = getExtremitiesAsIndex(tokens, quantityLeast.getOffsetEnd(), endPos);
                            int tokensInBetween = tokens.subList(extremitiesAsIndex.getLeft(), extremitiesAsIndex.getRight()).size();
                            if (tokensInBetween > 8 ||
                                !currentSentence.equals(findSentenceOffset(sentences, currentMeasurement, offset))) {
                                if (currentMeasurement.isValid()) {
                                    measurements.add(currentMeasurement);
                                    currentMeasurement = new Measurement();
                                    currentUnit = new Unit();
                                }
                            }
                        }
                    }
                }

                currentQuantity = new Quantity(clusterContent, null, startPos, endPos);
                final Value parsedValue = valueParser.parseValue(currentQuantity.getRawValue());
                if (parsedValue != null) {
                    currentQuantity.setParsedValue(parsedValue);
                }
                currentQuantity.setLayoutTokens(theTokens);
                if (currentUnit.getRawName() != null
                    && currentSentence.equals(findSentenceOffset(sentences, new OffsetPosition(currentUnit.getOffsetStart(), currentUnit.getOffsetEnd()), offset))) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.setQuantityMost(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_VALUE_BASE)) {
                LOGGER.debug("base value: " + clusterContent);
                if (openMeasurement != null &&
                    (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE ||
                        currentMeasurement.getQuantityBase() != null ||
                        !currentSentence.equals(findSentenceOffset(sentences, currentMeasurement, offset)))
                ) {
                    if (currentMeasurement.isValid()) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity(clusterContent, null, startPos, endPos);
                final Value parsedValue = valueParser.parseValue(currentQuantity.getRawValue());
                if (parsedValue != null) {
                    currentQuantity.setParsedValue(parsedValue);
                }
                currentQuantity.setLayoutTokens(theTokens);
                if (currentUnit.getRawName() != null
                    && currentSentence.equals(findSentenceOffset(sentences, new OffsetPosition(currentUnit.getOffsetStart(), currentUnit.getOffsetEnd()), offset))) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.setQuantityBase(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_VALUE_RANGE)) {
                LOGGER.debug("range value: " + clusterContent);
                if (openMeasurement != null &&
                    (openMeasurement != UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE ||
                        currentMeasurement.getQuantityRange() != null ||
                        !currentSentence.equals(findSentenceOffset(sentences, currentMeasurement, offset)))
                ) {
                    if (currentMeasurement.isValid()) {
                        measurements.add(currentMeasurement);
                        currentMeasurement = new Measurement();
                        currentUnit = new Unit();
                    }
                }
                currentQuantity = new Quantity(clusterContent, null, startPos, endPos);
                final Value parsedValue = valueParser.parseValue(currentQuantity.getRawValue());
                if (parsedValue != null) {
                    currentQuantity.setParsedValue(parsedValue);
                }
                currentQuantity.setLayoutTokens(theTokens);
                if (currentUnit.getRawName() != null
                    && currentSentence.equals(findSentenceOffset(sentences, new OffsetPosition(currentUnit.getOffsetStart(), currentUnit.getOffsetEnd()), offset))) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.setQuantityRange(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE);
                openMeasurement = UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_VALUE_LIST)) {
                LOGGER.debug("value in list: " + clusterContent);
                if (openMeasurement != null) {
                    if (openMeasurement != UnitUtilities.Measurement_Type.CONJUNCTION) {
                        if (currentMeasurement.isValid()) {
                            measurements.add(currentMeasurement);
                            currentMeasurement = new Measurement();
                            //currentUnit = new Unit();
                        }
                    } else {
                        List<Quantity> quantityList = currentMeasurement.getQuantityList();
                        if (isNotEmpty(quantityList)) {
                            Quantity lastQuantityinList = quantityList.get(quantityList.size() - 1);

                            // Workaround - if the list item is more than 10 character from the previous, we start a new measurement
                            Pair<Integer, Integer> extremitiesAsIndex = getExtremitiesAsIndex(tokens, lastQuantityinList.getOffsetEnd(), endPos);
                            int tokensInBetween = tokens.subList(extremitiesAsIndex.getLeft(), extremitiesAsIndex.getRight()).size();
                            if (tokensInBetween > 8 ||
                                !currentSentence.equals(findSentenceOffset(sentences, currentMeasurement, offset))) {
                                if (currentMeasurement.isValid()) {
                                    measurements.add(currentMeasurement);
                                    currentMeasurement = new Measurement();
                                    //currentUnit = new Unit();
                                }
                            }
                        }
                    }
                }
                currentQuantity = new Quantity(clusterContent, null, startPos, endPos);
                final Value parsedValue = valueParser.parseValue(currentQuantity.getRawValue());
                if (parsedValue != null) {
                    currentQuantity.setParsedValue(parsedValue);
                }
                currentQuantity.setLayoutTokens(theTokens);

                if (currentUnit.getRawName() != null
                    && currentSentence.equals(findSentenceOffset(sentences, new OffsetPosition(currentUnit.getOffsetStart(), currentUnit.getOffsetEnd()), offset))) {
                    currentQuantity.setRawUnit(currentUnit);
                }
                currentMeasurement.addQuantityToList(currentQuantity);
                currentMeasurement.setType(UnitUtilities.Measurement_Type.CONJUNCTION);
                openMeasurement = UnitUtilities.Measurement_Type.CONJUNCTION;
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_UNIT_LEFT)) {
                LOGGER.debug("unit (left attachment): " + clusterContent);
                currentUnit = new Unit(clusterContent, startPos, endPos);
                currentUnit.setLayoutTokens(theTokens);

                if (openMeasurement == UnitUtilities.Measurement_Type.VALUE) {
                    if (currentMeasurement.getQuantityAtomic() != null) {
                        currentMeasurement.getQuantityAtomic().setRawUnit(currentUnit);
                        currentMeasurement.addBoundingBoxes(boundingBoxes);
                    }
                } else if (openMeasurement == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
                    if (currentMeasurement.getQuantityMost() != null &&
                        (currentMeasurement.getQuantityMost().getRawUnit() == null ||
                            currentMeasurement.getQuantityMost().getRawUnit().getRawName() == null)) {
                        currentMeasurement.getQuantityMost().setRawUnit(currentUnit);
                    }

                    if ((currentMeasurement.getQuantityLeast() != null) &&
                        ((currentMeasurement.getQuantityLeast().getRawUnit() == null) ||
                            (currentMeasurement.getQuantityLeast().getRawUnit().getRawName() == null))) {
                        currentMeasurement.getQuantityLeast().setRawUnit(currentUnit);
                    }
                    currentMeasurement.addBoundingBoxes(boundingBoxes);

                } else if (openMeasurement == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
                    if (currentMeasurement.getQuantityRange() != null &&
                        (currentMeasurement.getQuantityRange().getRawUnit() == null ||
                            currentMeasurement.getQuantityRange().getRawUnit().getRawName() == null)) {
                        currentMeasurement.getQuantityRange().setRawUnit(currentUnit);

                        if (currentMeasurement.getQuantityBase() != null &&
                            (currentMeasurement.getQuantityBase().getRawUnit() == null ||
                                currentMeasurement.getQuantityBase().getRawUnit().getRawName() == null))
                            currentMeasurement.getQuantityBase().setRawUnit(currentUnit);

                        currentMeasurement.addBoundingBoxes(boundingBoxes);
                    }
                } else if (openMeasurement == UnitUtilities.Measurement_Type.CONJUNCTION) {
                    if (CollectionUtils.isNotEmpty(currentMeasurement.getQuantityList())) {
                        for (Quantity quantity : currentMeasurement.getQuantityList()) {
                            if (quantity != null
                                && (quantity.getRawUnit() == null || quantity.getRawUnit().getRawName() == null)) {
                                quantity.setRawUnit(currentUnit);
                                currentMeasurement.addBoundingBoxes(boundingBoxes);
                            } else if (quantity == null && openMeasurement == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
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
                } else if (openMeasurement == UnitUtilities.Measurement_Type.CONJUNCTION) {
                    if (currentMeasurement.isValid()) {
                        if (CollectionUtils.isNotEmpty(currentMeasurement.getQuantityList())) {
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
                        openMeasurement = null;
                    }
                }
                currentUnit = new Unit(clusterContent, startPos, endPos);
                currentUnit.setLayoutTokens(theTokens);
                currentUnit.setUnitRightAttachment(true);
                currentMeasurement.addBoundingBoxes(boundingBoxes);
            } else if (clusterLabel.equals(QUANTITY_OTHER)) {
            } else {
                LOGGER.error("Warning: unexpected label in quantity parser: " + clusterLabel.getLabel() + " for " + clusterContent);
            }

            pos = endPos;
            while (pos > currentSentence.end) {
                if (currentSentenceIndex < sentences.size() - 1) {
                    currentSentenceIndex++;
                    currentSentence = sentences.get(currentSentenceIndex);
                } else {
                    // There are spurious characters that are not recognised as sentences at the end of the stream, 
                    // we ignore it and whatever comes out will be considered on a different sentence 
                    break;
                }
            }
        }

        if (currentMeasurement.isValid()) {
            measurements.add(currentMeasurement);
        }

        measurements.stream().forEach(m -> populateRawOffsetsAndText(m, tokens));

        measurements = MeasurementOperations.postCorrection(measurements);
        return measurements;
    }

    private static void populateRawOffsetsAndText(Measurement m, List<LayoutToken> tokens) {
        final Pair<OffsetPosition, String> measurementRawOffsetsAndText = QuantityOperations.getMeasurementRawOffsetsAndText(m, tokens);
        m.setRawOffsets(measurementRawOffsetsAndText.getLeft());
        m.setRawString(measurementRawOffsetsAndText.getRight().replace("\n", " "));
    }

    private OffsetPosition findSentenceOffset(List<OffsetPosition> sentences, OffsetPosition offsets) {
        return findSentenceOffset(sentences, offsets);
    }

    private OffsetPosition findSentenceOffset(List<OffsetPosition> sentences, OffsetPosition offsets, int offset) {
        OffsetPosition currentMeasureOffset = QuantityOperations.getContainingOffset(Arrays.asList(offsets));

        List<OffsetPosition> sentencesCurrentMeasure = sentences.stream().filter(op -> op.start < currentMeasureOffset.start && op.end > currentMeasureOffset.end)
            .collect(Collectors.toList());

        return sentencesCurrentMeasure.get(0);
    }

    private OffsetPosition findSentenceOffset(List<OffsetPosition> sentences, Measurement measurement) {
        return findSentenceOffset(sentences, measurement, 0);
    }

    private OffsetPosition findSentenceOffset(List<OffsetPosition> sentences, Measurement measurement, int offset) {
        OffsetPosition currentMeasureOffset = measurementOperations.calculateExtremitiesOffsets(measurement);
        List<OffsetPosition> sentencesCurrentMeasure = sentences.stream()
            .filter(sop -> sop.start < currentMeasureOffset.start - offset && sop.end > currentMeasureOffset.end - offset)
            .collect(Collectors.toList());

        if (sentencesCurrentMeasure.size() > 1) {
            throw new GrobidException("The measurement " + measurement + " is spread among two sentences.");
        } else if (CollectionUtils.isEmpty(sentencesCurrentMeasure)) {
            LOGGER.warn("Cannot find sentence. The entity might be inconsistent: " + measurement.getRawOffsets().toString());
            return new OffsetPosition(-1, -1);
        }
        return sentencesCurrentMeasure.get(0);
    }

    public void setDisableSubstanceParser(boolean disableSubstanceParser) {
        this.disableSubstanceParser = disableSubstanceParser;
    }

    public void setQuantifiedObjectParser(QuantifiedObjectParser quantifiedObjectParser) {
        this.quantifiedObjectParser = quantifiedObjectParser;
    }

    public ValueParser getValueParser() {
        return valueParser;
    }
}
