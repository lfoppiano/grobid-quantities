package org.grobid.core.engines;

import com.google.common.collect.Iterables;
import org.apache.commons.collections4.CollectionUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.data.*;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Basic implementation of substance parser without CRF. The goal here is to be able to bootstrap
 * training data for a next cleaner ML model.
 */
public class DefaultQuantifiedObjectParser extends QuantifiedObjectParser {
    private static final Logger logger = LoggerFactory.getLogger(DefaultQuantifiedObjectParser.class);

    public DefaultQuantifiedObjectParser() {
        super(GrobidModels.DUMMY);
    }

    @Override
    public List<Measurement> process(List<LayoutToken> tokens, List<Measurement> measurements) {
        if (isEmpty(measurements)) {
            return null;
        }
        try {
            String text = LayoutTokensUtil.toText(tokens);
            TextParser textParser = TextParser.getInstance();
            int firstOffset = Iterables.getFirst(tokens, new LayoutToken()).getOffset();
            List<OffsetPosition> measurementsOffsets = measurements.stream()
                .map(m -> new OffsetPosition(m.getRawOffsets().start - firstOffset, m.getRawOffsets().end - firstOffset))
                .collect(Collectors.toList());
            List<Sentence> parsedSentences = textParser.parseText(text, measurementsOffsets);
            int firstTokenOffsetStart = tokens.get(0).getOffset();
            parsedSentences.stream().forEach(s -> {
                s.getOffset().start = s.getOffsetStart() + firstTokenOffsetStart;
                s.getOffset().end = s.getOffsetEnd() + firstTokenOffsetStart;
            });
            int indexMeasurement = 0;
            int offset = 0;

            // this part is for identifying for each sentence, the measurements belonging to the sentence 
            for (Sentence processedSentence : parsedSentences) {
                // list of measurements for the current sentence
                List<Measurement> sentenceMeasurements = new ArrayList<>();
                // Positions of measurements
                List<Integer> positionMeasurements = new ArrayList<>();
                while (indexMeasurement < measurements.size()) {
                    Measurement measurement = measurements.get(indexMeasurement);
                    int position = -1;

                    // is the measurement quantities in the current sentence?
                    if (measurement.getType() == UnitUtilities.Measurement_Type.VALUE) {
                        Quantity quantity = measurement.getQuantityAtomic();
                        if (quantity.getOffsetStart() > processedSentence.getOffsetEnd()) {
                            // next sentence
                            break;
                        }
                        if (quantity.getOffsetEnd() < processedSentence.getOffsetStart()) {
                            // next measurement
                            indexMeasurement++;
                            continue;
                        }
                        position = quantity.getOffsetStart();
                    } else if ((measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) ||
                        (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE)) {
                        // values of the interval do not matter if min/max or base/range
                        List<Quantity> sortedQuantities = QuantityOperations.toQuantityList(measurement).stream()
                            .sorted(Comparator.comparingInt(Quantity::getOffsetStart))
                            .collect(Collectors.toList());

                        Quantity firstQuantity = Iterables.getFirst(sortedQuantities, null);
                        Quantity lastQuantity = Iterables.getLast(sortedQuantities);

                        if ((lastQuantity != null) && (lastQuantity.getOffsetEnd() < processedSentence.getOffsetStart())) {
                            // next measurement
                            indexMeasurement++;
                            continue;
                        }
                        if ((firstQuantity != null) && (firstQuantity.getOffsetStart() > processedSentence.getOffsetEnd())) {
                            // next sentence
                            break;
                        }

                        position = firstQuantity.getOffsetStart();
                    } else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                        // list must be consistent in unit type, and avoid too large chunk
                        List<Quantity> quantities = measurement.getQuantityList();
                        if (CollectionUtils.isNotEmpty(quantities)) {
                            // just exploit the first quantity for positioning
                            Quantity quantity = quantities.get(0);
                            if (quantity.getOffsetEnd() < processedSentence.getOffsetStart()) {
                                // next sentence
                                break;
                            }
                            if (quantity.getOffsetStart() > processedSentence.getOffsetEnd()) {
                                // next measurement
                                indexMeasurement++;
                                continue;
                            }
                            position = quantity.getOffsetStart();
                        }
                    }

                    // if we arrive here, this measurement is in the current sentence

                    sentenceMeasurements.add(measurement);
                    positionMeasurements.add(position);
                    indexMeasurement++;
                }

                // get the list of indexes corresponding to measurement parts
                List<String> indexMeasurementTokens = getIndexMeasurementTokens(sentenceMeasurements, processedSentence);

                // find the syntactic head... this will define the QuantifiedObject to the measurements
                setHeads(processedSentence, sentenceMeasurements, positionMeasurements, indexMeasurementTokens);
            }
        } catch (Exception e) {
            logger.error("error in substance parser: ", e);
        }
        return measurements;
    }


    private void setHeads(Sentence processedSentence,
                          List<Measurement> measurements,
                          List<Integer> positionMeasurements,
                          List<String> indexMeasurementTokens) {
        if (isEmpty(measurements))
            return;

        int startSentencePosition = processedSentence.getOffsetStart();
        FeatureFactory featureFactory = FeatureFactory.getInstance();

        List<SentenceParse> parses = processedSentence.getParses();
        // we're just considering the first best parse
        if ((parses == null) || (parses.size() == 0))
            return;
        SentenceParse parse = parses.get(0);
        int p = 0;
        for (Measurement measurement : measurements) {
            int position = positionMeasurements.get(p);
            p++;
            position = position - startSentencePosition;

            QuantifiedObject substance = null;
            String headStruct = parse.getTokenStructureByPosition(position);
            if (headStruct == null)
                return;

            String[] pieces = null;
            String currentIndex = null;
            String headIndex = null;
            String funct = null;
            String pos = null;
            String previousFunct = "";

            // now loop in the dependency tree
            int level = 1;
            while (substance == null) {
                pieces = headStruct.split("\t");
                if (pieces.length != 8)
                    break;
                currentIndex = pieces[0].trim();
                headIndex = pieces[5].trim();
                funct = pieces[6].trim();
                pos = pieces[3].trim();

                headStruct = parse.getTokenStructureByIndex(headIndex);
                // ignore tokens already identified as measurement parts
                if (indexMeasurementTokens.contains(currentIndex)) {
                    // we do not increment the level as we are still in the measurement phrase
                    previousFunct = funct;
                    if (headStruct == null)
                        break;
                    continue;
                }

                if (funct.equals("root"))
                    break;

                if (level == 1) {
                    // case direct modifier "... of something"

                    // if case of an interval, we need to take the last quantity object for the position
                    UnitUtilities.Measurement_Type type = measurement.getType();
                    if ((measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) ||
                        (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE)) {

                        int lastPosition = position;
                        Quantity quantityLeast = measurement.getQuantityLeast();
                        if (quantityLeast == null)
                            quantityLeast = measurement.getQuantityBase();
                        Quantity quantityMost = measurement.getQuantityMost();
                        if (quantityMost == null)
                            quantityMost = measurement.getQuantityRange();

                        if (quantityLeast != null) {
                            lastPosition = quantityLeast.getOffsetStart() - startSentencePosition;
                            if (lastPosition > position)
                                position = lastPosition;
                        }
                        if (quantityMost != null) {
                            lastPosition = quantityMost.getOffsetStart() - startSentencePosition;
                            if (lastPosition > position)
                                position = lastPosition;
                        }
                    }
                    String nextStruct = getNextStruct(position + 1, indexMeasurementTokens,
                        processedSentence.getOffsetEnd() - processedSentence.getOffsetStart(), parse);
                    if (nextStruct != null) {
                        String[] subpieces = nextStruct.split("\t");
                        if (subpieces.length == 8) {
                            String nextIndex = subpieces[0].trim();
                            String nextToken = subpieces[2].trim();
                            String nextHeadIndex = subpieces[5].trim();
                            String nextFunct = subpieces[6].trim();
                            String nextPos = subpieces[3].trim();
                            if ((nextFunct.equals("prep") && nextPos.startsWith("IN") && !nextToken.equals("to")) ||
                                (nextFunct.equals("nn") && nextPos.startsWith("NN"))) {
                                OffsetPosition phrasePosition = new OffsetPosition(parse.getOffsetStartIndex(nextIndex), parse.getOffsetEndIndex(nextIndex));
                                phrasePosition = getFullPhrase(phrasePosition, parse, indexMeasurementTokens, false);
                                phrasePosition.start = parse.getOffsetEndIndex(nextIndex) + 1;
                                if (phrasePosition.end <= phrasePosition.start) {
                                    break;
                                }
                                String chunk = processedSentence.getSentence().substring(phrasePosition.start, phrasePosition.end);
                                substance = new QuantifiedObject(chunk, chunk);
                                substance.setOffsetStart(phrasePosition.start);
                                substance.setOffsetEnd(phrasePosition.end);
                                break;
                            }
                        }
                    }
                }

                // case dependency to the syntactic head
                if (
                    (funct.equals("nsubjpass") && pos.startsWith("NN")) ||
                        (funct.equals("conj") && pos.startsWith("NN")) ||
                        (funct.equals("appos") && pos.startsWith("NN")) ||
                        (funct.equals("pobj") && pos.startsWith("NN")) ||
                        (previousFunct.equals("num") && pos.startsWith("NN"))
                ) {
                    if (featureFactory.test_digit(pieces[1]))
                        substance = new QuantifiedObject(pieces[1], pieces[1]);
                    else
                        substance = new QuantifiedObject(pieces[1], pieces[2]);
                    OffsetPosition substancePosition =
                        new OffsetPosition(parse.getOffsetStartIndex(currentIndex), parse.getOffsetEndIndex(currentIndex));
                    substancePosition = getFullPhrase(substancePosition, parse, indexMeasurementTokens, true);
                    substance.setOffsetStart(substancePosition.start + startSentencePosition);
                    substance.setOffsetEnd(substancePosition.end + startSentencePosition);
                    substance.setRawName(processedSentence.getSentence().substring(substancePosition.start, substancePosition.end));
                    // normalized name 

                }
                //else
                if (headStruct == null)
                    break;

                if (level == 3)
                    break;
                level++;
                previousFunct = funct;
            }
            if (substance != null)
                measurement.setQuantifiedObject(substance);
        }

    }


    private OffsetPosition getFullPhrase(OffsetPosition offsetPosition,
                                         SentenceParse parse,
                                         List<String> indexMeasurementTokens,
                                         boolean strictExpansion) {
        if (offsetPosition == null)
            return null;
        // get structure for the position of the QuantifiedObject
        String struct = parse.getTokenStructureByPosition(offsetPosition.start);
        if (struct == null)
            return offsetPosition;
        String[] pieces = null;
        String headIndex = null;
        String currentIndex = null;

        pieces = struct.split("\t");
        if (pieces.length != 8)
            return offsetPosition;
        headIndex = pieces[0].trim();

        int headIndexInt = -1;
        try {
            headIndexInt = Integer.parseInt(headIndex);
        } catch (Exception e) {
            logger.info("ClearNLP / Token index is not a parsable number: " + headIndex);
        }

        if (headIndexInt == -1)
            return offsetPosition;

        List<String> children = parse.getTokenChildStructureByHeadIndex(headIndex);
        if ((children == null) || (children.size() == 0)) {
            return offsetPosition;
        }

        // first loop for extension on the right
        for (String child : children) {
            struct = parse.getTokenStructureByIndex(child);
            pieces = struct.split("\t");
            if (pieces.length != 8)
                continue;
            currentIndex = pieces[0].trim();

            int currentIndexInt = -1;
            try {
                currentIndexInt = Integer.parseInt(currentIndex);
            } catch (Exception e) {
                logger.info("ClearNLP / Token index is not a parsable number: " + currentIndex);
            }

            if (currentIndexInt == -1)
                break;

            if (currentIndexInt <= headIndexInt) {
                continue;
            }

            // ignore tokens already identified as measurement parts and stop right expansion
            if (indexMeasurementTokens.contains(currentIndex)) {
                break;
            }

            if ((parse.getOffsetStartIndex(currentIndex) != -1) && (parse.getOffsetEndIndex(currentIndex) != -1)) {
                OffsetPosition subOffsetPosition =
                    new OffsetPosition(parse.getOffsetStartIndex(currentIndex),
                        parse.getOffsetEndIndex(currentIndex));
                //subOffsetPosition = getFullPhrase(subOffsetPosition, parse, indexMeasurementTokens);

                if ((subOffsetPosition.start > offsetPosition.end) &&
                    ((subOffsetPosition.start - offsetPosition.end < 3) || !strictExpansion))
                    offsetPosition.end = subOffsetPosition.end;
            }
        }

        // second loop for extension on the left
        for (int j = children.size() - 1; j >= 0; j--) {
            String child = children.get(j);

            struct = parse.getTokenStructureByIndex(child);

            pieces = struct.split("\t");
            if (pieces.length != 8)
                continue;
            currentIndex = pieces[0].trim();

            int currentIndexInt = -1;
            try {
                currentIndexInt = Integer.parseInt(currentIndex);
            } catch (Exception e) {
                logger.info("ClearNLP / Token index is not a parsable number: " + currentIndex);
            }

            if (currentIndexInt == -1)
                break;

            if (currentIndexInt >= headIndexInt) {
                continue;
            }

            // ignore tokens already identified as measurement parts and stop right expansion
            if (indexMeasurementTokens.contains(currentIndex)) {
                break;
            }

            if ((parse.getOffsetStartIndex(currentIndex) != -1) && (parse.getOffsetEndIndex(currentIndex) != -1)) {
                OffsetPosition subOffsetPosition =
                    new OffsetPosition(parse.getOffsetStartIndex(currentIndex),
                        parse.getOffsetEndIndex(currentIndex));
                //subOffsetPosition = getFullPhrase(subOffsetPosition, parse, indexMeasurementTokens);

                if ((subOffsetPosition.end < offsetPosition.start) &&
                    ((offsetPosition.start - subOffsetPosition.end < 3) || !strictExpansion))
                    offsetPosition.start = subOffsetPosition.start;
            }
        }

        return offsetPosition;
    }


    private List<String> getIndexMeasurementTokens(List<Measurement> measurements,
                                                   Sentence processedSentence) {
        if (CollectionUtils.isEmpty(measurements)) {
            return null;
        }

        List<String> result = new ArrayList<>();
        int startSentencePosition = processedSentence.getOffsetStart();
        List<SentenceParse> parses = processedSentence.getParses();
        // we're just considering the first best parse
        if (CollectionUtils.isEmpty(parses)) {
            return null;
        }

        SentenceParse parse = parses.get(0);

        for (Measurement measurement : measurements) {
            UnitUtilities.Measurement_Type type = measurement.getType();

            if (type == UnitUtilities.Measurement_Type.VALUE) {
                Quantity quantity = measurement.getQuantityAtomic();
                int position = quantity.getOffsetStart();
                addTokenIndex(position - startSentencePosition, quantity.getOffsetEnd() - quantity.getOffsetStart(), parse, result);

                // unit position
                Unit rawUnit = quantity.getRawUnit();
                if (rawUnit != null) {
                    position = rawUnit.getOffsetStart();
                    addTokenIndex(position - startSentencePosition, rawUnit.getOffsetEnd() - rawUnit.getOffsetStart(), parse, result);
                }
            } else if ((type == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) ||
                (type == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE)) {
                // values of the interval do not matter if min/max or base/range
                Quantity quantityLeast = measurement.getQuantityLeast();
                if (quantityLeast == null)
                    quantityLeast = measurement.getQuantityBase();
                Quantity quantityMost = measurement.getQuantityMost();
                if (quantityMost == null)
                    quantityMost = measurement.getQuantityRange();

                if (quantityLeast != null) {
                    int position = quantityLeast.getOffsetStart();
                    addTokenIndex(position - startSentencePosition, quantityLeast.getOffsetEnd() - quantityLeast.getOffsetStart(), parse, result);

                    // unit position
                    Unit rawUnit = quantityLeast.getRawUnit();
                    if (rawUnit != null) {
                        position = rawUnit.getOffsetStart();
                        addTokenIndex(position - startSentencePosition, rawUnit.getOffsetEnd() - rawUnit.getOffsetStart(), parse, result);
                    }
                }

                if (quantityMost != null) {
                    int position = quantityMost.getOffsetStart();
                    addTokenIndex(position - startSentencePosition, quantityMost.getOffsetEnd() - quantityMost.getOffsetStart(), parse, result);

                    // unit position
                    Unit rawUnit = quantityMost.getRawUnit();
                    if (rawUnit != null) {
                        position = rawUnit.getOffsetStart();
                        addTokenIndex(position - startSentencePosition, rawUnit.getOffsetEnd() - rawUnit.getOffsetStart(), parse, result);
                    }
                }
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                // list must be consistent in unit type, and avoid too large chunk
                List<Quantity> quantities = measurement.getQuantityList();
                if ((quantities != null) && (quantities.size() > 0)) {
                    // just exploit the first quantity for positioning
                    Quantity quantity = quantities.get(0);
                    int position = quantity.getOffsetStart();
                    addTokenIndex(position - startSentencePosition, quantity.getOffsetEnd() - quantity.getOffsetStart(), parse, result);

                    // unit position
                    Unit rawUnit = quantity.getRawUnit();
                    if (rawUnit != null) {
                        position = rawUnit.getOffsetStart();
                        addTokenIndex(position - startSentencePosition, rawUnit.getOffsetEnd() - rawUnit.getOffsetStart(), parse, result);
                    }
                }
            }
        }
        return result;
    }

    private List<String> addTokenIndex(int position, int length, SentenceParse parse, List<String> result) {
        String tokenStruct = parse.getTokenStructureByPosition(position);
        if (tokenStruct != null) {
            String[] pieces = tokenStruct.split("\t");
            if (pieces.length == 8) {
                String index = pieces[0].trim();
                if (result == null)
                    result = new ArrayList<String>();
                if (!result.contains(index))
                    result.add(index);
            }
        } else {
            logger.info("Invalid position: " + position + " - no parse result find at this position.");
        }
        // brute force adding all subtokens in the specified interval
        for (int i = 1; i < length; i++) {
            tokenStruct = parse.getTokenStructureByPosition(position + i);
            if (tokenStruct != null) {
                String[] pieces = tokenStruct.split("\t");
                if (pieces.length == 8) {
                    String index = pieces[0].trim();
                    if (result == null)
                        result = new ArrayList<String>();
                    if (!result.contains(index))
                        result.add(index);
                }
            }
        }
        return result;
    }

    private String getNextStruct(int position, List<String> indexMeasurementTokens, int sentenceLength, SentenceParse parse) {
        String childStruct = null;
        int j = 0;
        String currentIndex = null;
        while (childStruct == null) {
            if (position + j >= sentenceLength)
                break;
            childStruct = parse.getTokenStructureByPosition(position + j);
            if (childStruct != null) {
                String[] pieces = childStruct.split("\t");
                if (pieces.length != 8)
                    break;
                currentIndex = pieces[0].trim();
                if (indexMeasurementTokens.contains(currentIndex)) {
                    childStruct = null;
                    j++;
                    continue;
                }
            }
            j++;
        }
        return childStruct;
    }

}