package org.grobid.core.engines;

import com.google.common.collect.Iterables;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.QuantifiedObject;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorQuantifiedObjects;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.BoundingBoxCalculator;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.QuantityOperations;
import org.grobid.core.utilities.UnicodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.length;
import static org.grobid.core.engines.label.QuantitiesTaggingLabels.*;

/**
 * Parser for identifying and attaching the quantified "substance".
 */
public class QuantifiedObjectParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuantifiedObjectParser.class);

    private static volatile QuantifiedObjectParser instance;

    public static QuantifiedObjectParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new DefaultQuantifiedObjectParser();
    }

    protected QuantifiedObjectParser() {
        super(QuantitiesModels.QUANTIFIED_OBJECT);
    }

    public List<Measurement> process(List<LayoutToken> layoutTokens, List<Measurement> measurements) {

        List<Measurement> newMeasurements = new ArrayList<>();
        Collections.copy(measurements, newMeasurements);

        // List<LayoutToken> for the selected segment
        List<LayoutToken> retokenizeLayoutTokens = QuantityAnalyzer.getInstance().retokenizeLayoutTokens(layoutTokens);

        //Normalisation
        List<LayoutToken> layoutTokenNormalised = retokenizeLayoutTokens
                .stream()
                .map(layoutToken -> {
                            layoutToken.setText(UnicodeUtil.normaliseText(layoutToken.getText()));

                            return layoutToken;
                        }
                ).collect(Collectors.toList());

        if (isEmpty(layoutTokenNormalised))
            return measurements;

        try {
            List<Pair<Integer, Integer>> offsetList = QuantityOperations.getOffsetList(measurements);

            List<Boolean> measurementFlags = synchroniseLayoutTokensWithOffsets(layoutTokenNormalised, offsetList);

            String ress = addFeatures(layoutTokenNormalised, measurementFlags);

            if (StringUtils.isEmpty(ress))
                return newMeasurements;

            String res = null;
            try {
                res = label(ress);
            } catch (Exception e) {
                throw new GrobidException("CRF labeling for quantified object parsing failed.", e);
            }

            List<QuantifiedObject> quantifiedObjects = extractOutput(res, layoutTokenNormalised);

            measurements = attachQuantifiedObjects(quantifiedObjects, measurements);

            return measurements;

        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    public List<Measurement> attachQuantifiedObjects(List<QuantifiedObject> quantifiedObjects, List<Measurement> measurements) {
        if(isEmpty(quantifiedObjects))
            return measurements;

        int indexQuantifiedObject = 0;
        QuantifiedObject currentQuantifiedObject = quantifiedObjects.get(indexQuantifiedObject);

        for (Measurement measurement : measurements) {
            List<Pair<Integer, Integer>> offsetList1 = QuantityOperations.getOffsetList(measurement);
            Pair<Integer, Integer> offsetMeasurement = QuantityOperations.toContainingOffset(offsetList1);

            if(currentQuantifiedObject.getAttachment() == null) {
                indexQuantifiedObject++;
                currentQuantifiedObject = quantifiedObjects.get(indexQuantifiedObject);
            }
            if (currentQuantifiedObject.getAttachment().equals(QuantifiedObject.Attachment.LEFT)) {
                if (offsetMeasurement.getLeft() <= currentQuantifiedObject.getOffsetStart()) {
                    measurement.setQuantifiedObject(currentQuantifiedObject);
                } else {
                    continue;
                }

            } else if (currentQuantifiedObject.getAttachment().equals(QuantifiedObject.Attachment.RIGHT)) {
                if (offsetMeasurement.getLeft() < currentQuantifiedObject.getOffsetStart()) {
                    continue;
                } else {
                    measurement.setQuantifiedObject(currentQuantifiedObject);
                }
            }

            indexQuantifiedObject++;
            if(indexQuantifiedObject < quantifiedObjects.size()) {
                currentQuantifiedObject = quantifiedObjects.get(indexQuantifiedObject);
            } else {
                break;
            }

        }

        return measurements;
    }

    protected List<QuantifiedObject> extractOutput(String result, List<LayoutToken> tokens) {
        List<QuantifiedObject> quantifiedObjects = new ArrayList<>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(QuantitiesModels.QUANTIFIED_OBJECT, result, tokens);
        List<TaggingTokenCluster> clusters = clusteror.cluster();

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            List<LayoutToken> theTokens = cluster.concatTokens();
            String clusterContent = LayoutTokensUtil.toText(theTokens).trim();
            List<BoundingBox> boundingBoxes = null;

            if (!clusterLabel.equals(QUANTIFIED_OBJECT_OTHER))
                boundingBoxes = BoundingBoxCalculator.calculate(theTokens);

            if (clusterLabel.equals(QUANTIFIED_OBJECT_LEFT)) {
                QuantifiedObject quantifiedObject = new QuantifiedObject(clusterContent);
                quantifiedObject.setOffsetStart(theTokens.get(0).getOffset());
                quantifiedObject.setOffsetEnd(Iterables.getLast(theTokens).getOffset() + Iterables.getLast(theTokens).getText().length());
                quantifiedObject.setLayoutTokens(theTokens);
                quantifiedObject.setBoundingBoxes(boundingBoxes);
                quantifiedObject.setAttachment(QuantifiedObject.Attachment.LEFT);

                quantifiedObjects.add(quantifiedObject);

            } else if (clusterLabel.equals(QUANTIFIED_OBJECT_RIGHT)) {
                QuantifiedObject quantifiedObject = new QuantifiedObject(clusterContent);
                quantifiedObject.setOffsetStart(theTokens.get(0).getOffset());
                quantifiedObject.setOffsetEnd(Iterables.getLast(theTokens).getOffset() + Iterables.getLast(theTokens).getText().length());
                quantifiedObject.setLayoutTokens(theTokens);
                quantifiedObject.setBoundingBoxes(boundingBoxes);
                quantifiedObject.setAttachment(QuantifiedObject.Attachment.RIGHT);

                quantifiedObjects.add(quantifiedObject);

            } else if (clusterLabel.equals(QUANTIFIED_OBJECT_OTHER)) {

            } else {
                LOGGER.error("Unexpected label in quantified object parser: " + clusterLabel.getLabel() + " for " + clusterContent);
            }
        }

        return quantifiedObjects;
    }

    /**
     * This method takes in input a list of tokens and a list of offsets representing special entities and
     *
     * @return a list of booleans of the same size of the initial layout token, flagging all the
     * tokens within the offsets
     */
    protected static List<Boolean> synchroniseLayoutTokensWithOffsets(List<LayoutToken> tokens,
                                                                      List<Pair<Integer, Integer>> offsets) {

        List<Boolean> isMeasure = new ArrayList<>();

        if (CollectionUtils.isEmpty(offsets)) {
            tokens.stream().forEach(t -> isMeasure.add(Boolean.FALSE));

            return isMeasure;
        }

        int globalOffset = 0;
        if (CollectionUtils.isNotEmpty(tokens)) {
            globalOffset = tokens.get(0).getOffset();
        }

        int mentionId = 0;
        Pair<Integer, Integer> offset = offsets.get(mentionId);

        for (LayoutToken token : tokens) {
            //normalise the offsets
            int mentionStart = globalOffset + offset.getLeft();
            int mentionEnd = globalOffset + offset.getRight();

            if (token.getOffset() < mentionStart) {
                isMeasure.add(Boolean.FALSE);
                continue;
            } else {
                if (token.getOffset() >= mentionStart
                        && token.getOffset() + length(token.getText()) <= mentionEnd) {
                    isMeasure.add(Boolean.TRUE);
                    continue;
                }

                if (mentionId == offsets.size() - 1) {
                    isMeasure.add(Boolean.FALSE);
                    break;
                } else {
                    isMeasure.add(Boolean.FALSE);
                    mentionId++;
                    offset = offsets.get(mentionId);
                }
            }
        }
        if (tokens.size() > isMeasure.size()) {

            for (int counter = isMeasure.size(); counter < tokens.size(); counter++) {
                isMeasure.add(Boolean.FALSE);
            }
        }

        return isMeasure;
    }

    private String addFeatures(List<LayoutToken> tokens, List<Boolean> isMeasurement) {
        StringBuilder result = new StringBuilder();
        try {

            ListIterator<LayoutToken> it = tokens.listIterator();
            while (it.hasNext()) {
                int index = it.nextIndex();
                LayoutToken token = it.next();

                if (token.getText().trim().equals("@newline")) {
                    result.append("\n");
                    continue;
                }

                String text = token.getText();
                if (text.equals(" ") || text.equals("\n")) {
                    continue;
                }

                FeaturesVectorQuantifiedObjects featuresVector =
                        FeaturesVectorQuantifiedObjects.addFeatures(text, null, isMeasurement.get(index));
                result.append(featuresVector.printVector());
                result.append("\n");
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return result.toString();
    }

}