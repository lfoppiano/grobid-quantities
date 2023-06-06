package org.grobid.core.utilities;

import com.google.common.collect.Iterables;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.layout.LayoutToken;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.length;

public class QuantityOperations {

    public static Pair<OffsetPosition, String> getMeasurementRawOffsetsAndText(Measurement measurement, List<LayoutToken> tokens) {
        List<Quantity> quantityList = QuantityOperations.toQuantityList(measurement);
        List<LayoutToken> layoutTokens = QuantityOperations.getLayoutTokens(quantityList);
        List<LayoutToken> sortedLayoutTokens = layoutTokens.stream()
            .sorted(Comparator.comparingInt(LayoutToken::getOffset))
            .collect(Collectors.toList());

        int start = sortedLayoutTokens.get(0).getOffset();
        int end = Iterables.getLast(sortedLayoutTokens).getOffset();

        // Token start and end
        Pair<Integer, Integer> extremitiesQuantityAsIndex = getExtremitiesAsIndex(tokens,
                Math.min(start, end), Math.max(start, end) + 1);

        //Offset start and end
        List<OffsetPosition> offsets = QuantityOperations.getOffsets(quantityList);
        List<OffsetPosition> sortedOffsets = offsets.stream()
            .sorted(Comparator.comparingInt(o -> o.start))
            .collect(Collectors.toList());

        int lowerOffset = sortedOffsets.get(0).start;
        int higherOffset = Iterables.getLast(sortedOffsets).end;
        
        String text = LayoutTokensUtil.toText(tokens.subList(extremitiesQuantityAsIndex.getLeft(), extremitiesQuantityAsIndex.getRight())).trim();

        return Pair.of(new OffsetPosition(lowerOffset, higherOffset), text);
    }

    /**
     * Get the index of the layout token referring to the startOffset, endOffset tokens in the
     * supplied token list.
     * <p>
     * The returned are (start, end) with end excluded (same as usual java stuff).
     */
    public static Pair<Integer, Integer> getExtremitiesAsIndex(List<LayoutToken> tokens, int startOffset, int endOffset) {

        if (isEmpty(tokens)) {
            return Pair.of(0, 0);
        }

        if (startOffset > getLayoutTokenListEndOffset(tokens) || endOffset < getLayoutTokenListStartOffset(tokens)) {
            throw new IllegalArgumentException("StartOffset and endOffset are outside the offset boundaries of the layoutTokens. ");
        }
        int start = 0;
        int end = tokens.size() - 1;

        List<LayoutToken> centralTokens = tokens.stream()
            .filter(layoutToken -> (layoutToken.getOffset() >= startOffset && getLayoutTokenEndOffset(layoutToken) <= endOffset)
                    || (layoutToken.getOffset() >= startOffset && layoutToken.getOffset() < endOffset
                    || (getLayoutTokenEndOffset(layoutToken) > startOffset && getLayoutTokenEndOffset(layoutToken) < endOffset)
                )
            )
            .collect(Collectors.toList());

        int layoutTokenIndexStart = start;
        int layoutTokenIndexEnd = end;

        if (isNotEmpty(centralTokens)) {
            layoutTokenIndexStart = tokens.indexOf(centralTokens.get(0));
            layoutTokenIndexEnd = tokens.indexOf(Iterables.getLast(centralTokens));
        }

        // Making it exclusive as any java stuff
        return Pair.of(layoutTokenIndexStart, layoutTokenIndexEnd + 1);
    }

    public static int getLayoutTokenEndOffset(LayoutToken layoutToken) {
        return layoutToken.getOffset() + layoutToken.getText().length();
    }

    public static int getLayoutTokenStartOffset(LayoutToken layoutToken) {
        return layoutToken.getOffset();
    }


    public static int getLayoutTokenListStartOffset(List<LayoutToken> tokens) {
        if (isEmpty(tokens)) {
            return 0;
        }

        LayoutToken firstToken = tokens.get(0);
        return firstToken.getOffset();
    }

    public static int getLayoutTokenListEndOffset(List<LayoutToken> tokens) {
        if (isEmpty(tokens)) {
            return 0;
        }

        LayoutToken lastToken = tokens.get(tokens.size() - 1);
        return lastToken.getOffset() + lastToken.getText().length();
    }

    public static List<Quantity> toQuantityList(Measurement m) {
        List<Quantity> quantitiesList = new LinkedList<>();

        if (UnitUtilities.Measurement_Type.VALUE.equals(m.getType())) {
            quantitiesList.add(m.getQuantityAtomic());
        } else if (UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX.equals(m.getType())) {
            CollectionUtils.addIgnoreNull(quantitiesList, m.getQuantityLeast());
            CollectionUtils.addIgnoreNull(quantitiesList, m.getQuantityMost());
        } else if (UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE.equals(m.getType())) {
            CollectionUtils.addIgnoreNull(quantitiesList, m.getQuantityBase());
            CollectionUtils.addIgnoreNull(quantitiesList, m.getQuantityRange());
        } else if (UnitUtilities.Measurement_Type.CONJUNCTION.equals(m.getType())) {
            quantitiesList.addAll(m.getQuantityList());
        }

        return quantitiesList;
    }

    public static List<OffsetPosition> getOffset(Measurement measurement) {
        return toQuantityList(measurement)
                .stream()
                .flatMap(q -> getOffsets(q).stream())
                .sorted(Comparator.comparing(o -> o.end))
                .collect(Collectors.toList());
    }

    /**
     * transform a list measurement into a list of measurement ordered by the lower offset
     */
    public static List<OffsetPosition> getOffset(List<Measurement> measurements) {

        return measurements
                .stream()
                .flatMap(m -> toQuantityList(m).stream().flatMap(q -> getOffsets(q).stream()))
                .sorted(Comparator.comparing(o -> o.end))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Return the list of all offsets in a quantity, from rawValue and rawUnit (if present).
     */
    public static List<OffsetPosition> getOffsets(Quantity quantity) {

        List<OffsetPosition> offsets = new ArrayList<>();

        if (quantity == null) {
            return offsets;
        }

        offsets.add(new OffsetPosition(quantity.getOffsetStart(), quantity.getOffsetEnd()));
        if (quantity.getRawUnit() != null) {
            offsets.add(new OffsetPosition(quantity.getRawUnit().getOffsetStart(),
                    quantity.getRawUnit().getOffsetEnd()));
        }

        return offsets.stream()
                .sorted(Comparator.comparing(o -> o.end))
                .collect(Collectors.toList());

    }

    public static List<OffsetPosition> getOffsets(List<Quantity> quantities) {

        List<OffsetPosition> offsets = new ArrayList<>();

        if (isEmpty(quantities)) {
            return offsets;
        }
        offsets = quantities.stream().flatMap(q -> {
            List<OffsetPosition> output = getOffsets(q);
            return output.stream();
        }).collect(Collectors.toList());


        return offsets.stream()
                .sorted(Comparator.comparing(o -> o.start))
                .collect(Collectors.toList());

    }

    /**
     * Given a list of offsets returns the boundaries these offsets cover
     */
    public static OffsetPosition getContainingOffset(List<OffsetPosition> offsetList) {
        List<OffsetPosition> sorted = offsetList
                .stream()
                .sorted(Comparator.comparing(o -> o.end))
                .collect(Collectors.toList());

        return new OffsetPosition(Iterables.getFirst(sorted, new OffsetPosition()).start, Iterables.getLast(sorted).end);
    }

    /**
     * Given a quantity compute the offsets
     */
    public static OffsetPosition getContainingOffset(Quantity quantity) {
        return getContainingOffset(getOffsets(quantity));
    }

    /**
     * This method takes in input a list of tokens and a list of offsets representing special entities and
     *
     * @return a list of booleans of the same size of the initial layout token, flagging all the
     * tokens within the offsets
     */
    public static List<Boolean> synchroniseLayoutTokensWithOffsets(List<LayoutToken> tokens,
                                                                   List<OffsetPosition> offsets) {

        List<Boolean> isMeasure = new ArrayList<>();

        if (CollectionUtils.isEmpty(offsets)) {
            tokens.stream().forEach(t -> isMeasure.add(Boolean.FALSE));

            return isMeasure;
        }

        int globalOffset = 0;
        if (isNotEmpty(tokens)) {
            globalOffset = tokens.get(0).getOffset();
        }

        int mentionId = 0;
        OffsetPosition offset = offsets.get(mentionId);

        for (LayoutToken token : tokens) {
            //normalise the offsets
            int mentionStart = globalOffset + offset.start;
            int mentionEnd = globalOffset + offset.end;

            if (token.getOffset() < mentionStart) {
                isMeasure.add(Boolean.FALSE);
                continue;
            } else {
                int tokenOffsetEnd = token.getOffset() + length(token.getText());
                if (token.getOffset() >= mentionStart
                        && tokenOffsetEnd <= mentionEnd) {
                    isMeasure.add(Boolean.TRUE);
                } else {
                    isMeasure.add(Boolean.FALSE);
                }

                if (mentionId == offsets.size() - 1) {
                    break;
                } else {
                    if (tokenOffsetEnd >= mentionEnd) {
                        mentionId++;
                        offset = offsets.get(mentionId);
                    }
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

    /**
     * Return a list of layoutToken of all the elements of the measurement
     **/
    public static List<LayoutToken> getLayoutTokens(List<Quantity> quantities) {
        return quantities
                .stream()
                .flatMap(q -> {
                    List<LayoutToken> lt = getLayoutTokens(q);
                    return lt.stream();
                })
                .collect(Collectors.toList());
    }

    public static List<LayoutToken> getLayoutTokens(Quantity q) {
        List<LayoutToken> lt = new ArrayList<>(q.getLayoutTokens());
        if (q.getRawUnit() != null) {
            lt.addAll(q.getRawUnit().getLayoutTokens());
        }
        return lt;
    }


    public static UnitUtilities.Unit_Type getType(Measurement m) {
        if (UnitUtilities.Measurement_Type.VALUE.equals(m.getType())) {
            return m.getQuantityAtomic().getType();
        } else if (UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX.equals(m.getType())) {
            Quantity quantityBase = m.getQuantityLeast();
            Quantity quantityRange = m.getQuantityMost();
            if (quantityBase != null && quantityBase.getType() != null) {
                return quantityBase.getType();
            } else if (quantityRange != null && quantityRange.getType() != null) {
                return quantityRange.getType();
            } else {
                return UnitUtilities.Unit_Type.UNKNOWN;
            }
        } else if (UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE.equals(m.getType())) {
            Quantity quantityBase = m.getQuantityBase();
            Quantity quantityRange = m.getQuantityRange();
            if (quantityBase != null && quantityBase.getType() != null) {
                return quantityBase.getType();
            } else if (quantityRange != null && quantityRange.getType() != null) {
                return quantityRange.getType();
            } else {
                return UnitUtilities.Unit_Type.UNKNOWN;
            }

        } else if (UnitUtilities.Measurement_Type.CONJUNCTION.equals(m.getType())) {
            if (isNotEmpty(m.getQuantityList())) {
                for(Quantity q : m.getQuantityList()) {
                    if(q.getType() != null) {
                        return q.getType();
                    }
                }
            }
            return UnitUtilities.Unit_Type.UNKNOWN;
        }

        throw new GrobidException("Invalid measurement, missing type");
    }
}
