package org.grobid.core.utilities;

import com.google.common.collect.Iterables;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.layout.LayoutToken;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.length;

public class QuantityOperations {

    public static List<Quantity> toQuantityList(Measurement m) {
        List<Quantity> quantitiesList = new LinkedList<>();

        if (UnitUtilities.Measurement_Type.VALUE.equals(m.getType())) {
            Quantity quantity = m.getQuantityAtomic();
            quantitiesList.add(quantity);
        } else if (UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX.equals(m.getType())) {
            Quantity quantityL = m.getQuantityLeast();
            Quantity quantityM = m.getQuantityMost();

            quantitiesList.addAll(intervalToList(quantityL, quantityM));
        } else if (UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE.equals(m.getType())) {
            Quantity quantityL = m.getQuantityBase();
            Quantity quantityM = m.getQuantityRange();

            quantitiesList.addAll(intervalToList(quantityL, quantityM));
        } else if (UnitUtilities.Measurement_Type.CONJUNCTION.equals(m.getType())) {
            quantitiesList.addAll(m.getQuantityList());
        }

        return quantitiesList;
    }

    public static List<Quantity> intervalToList(Quantity quantityL, Quantity quantityM) {
        List<Quantity> out = new ArrayList<>();

        CollectionUtils.addIgnoreNull(out, quantityL);
        CollectionUtils.addIgnoreNull(out, quantityM);

        return out;
    }

    public static List<Pair<Integer, Integer>> getOffset(Measurement measurement) {
        return toQuantityList(measurement)
            .stream()
            .flatMap(q -> getOffsets(q).stream())
            .sorted(Comparator.comparing(Pair::getRight))
            .collect(Collectors.toList());
    }

    /**
     * transform a list measurement into a list of measurement ordered by the lower offset
     */
    public static List<Pair<Integer, Integer>> getOffset(List<Measurement> measurements) {

        return measurements
            .stream()
            .flatMap(m -> toQuantityList(m).stream().flatMap(q -> getOffsets(q).stream()))
            .sorted(Comparator.comparing(Pair::getRight))
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * Return the list of all offsets in a quantity, from rawValue and rawUnit (if present).
     */
    public static List<Pair<Integer, Integer>> getOffsets(Quantity quantity) {

        List<Pair<Integer, Integer>> offsets = new ArrayList<>();

        if (quantity == null) {
            return offsets;
        }

        offsets.add(new ImmutablePair<>(quantity.getOffsetStart(), quantity.getOffsetEnd()));
        if (quantity.getRawUnit() != null) {
            offsets.add(new ImmutablePair<>(quantity.getRawUnit().getOffsetStart(),
                quantity.getRawUnit().getOffsetEnd()));
        }

        return offsets.stream()
            .sorted(Comparator.comparing(Pair::getRight))
            .collect(Collectors.toList());

    }

    public static List<Pair<Integer, Integer>> getOffsets(List<Quantity> quantities) {

        List<Pair<Integer, Integer>> offsets = new ArrayList<>();

        if (isEmpty(quantities)) {
            return offsets;
        }
        offsets = quantities.stream().flatMap(q -> {
            List<ImmutablePair<Integer, Integer>> output = new ArrayList<>();
            output.add(new ImmutablePair<>(q.getOffsetStart(), q.getOffsetEnd()));
            if (q.getRawUnit() != null) {
                output.add(new ImmutablePair<>(q.getRawUnit().getOffsetStart(),
                    q.getRawUnit().getOffsetEnd()));
            }

            return output.stream();
        }).collect(Collectors.toList());


        return offsets.stream()
            .sorted(Comparator.comparing(Pair::getRight))
            .collect(Collectors.toList());

    }

    /**
     * Given a list of offsets returns the boundaries these offsets cover
     */
    public static Pair<Integer, Integer> getContainingOffset(List<Pair<Integer, Integer>> offsetList) {
        List<Pair<Integer, Integer>> sorted = offsetList
            .stream()
            .sorted(Comparator.comparing(Pair::getRight))
            .collect(Collectors.toList());

        return new ImmutablePair<>(sorted.get(0).getLeft(), Iterables.getLast(sorted).getRight());
    }

    /**
     * Given a quantity compute the offsets
     */
    public static Pair<Integer, Integer> getContainingOffset(Quantity quantity) {
        return getContainingOffset(getOffsets(quantity));
    }

    /**
     * This method takes in input a list of tokens and a list of offsets representing special entities and
     *
     * @return a list of booleans of the same size of the initial layout token, flagging all the
     * tokens within the offsets
     */
    public static List<Boolean> synchroniseLayoutTokensWithOffsets(List<LayoutToken> tokens,
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
}
