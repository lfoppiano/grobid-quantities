package org.grobid.core.utilities;

import com.google.common.collect.Iterables;
import net.sf.saxon.lib.Logger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.layout.LayoutToken;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.length;

public class QuantityOperations {

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

        return new OffsetPosition(sorted.get(0).start, Iterables.getLast(sorted).end);
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
                    List<LayoutToken> lt = new ArrayList<>(q.getLayoutTokens());
                    if (q.getRawUnit() != null) {
                        lt.addAll(q.getRawUnit().getLayoutTokens());
                    }
                    return lt.stream();
                })
                .collect(Collectors.toList());
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
