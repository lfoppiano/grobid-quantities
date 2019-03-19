package org.grobid.core.utilities;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;

import java.util.*;
import java.util.stream.Collectors;

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
        if(quantityL != null) {
            out.add(quantityL);
        }

        if(quantityM != null) {
            out.add(quantityM);
        }
        return out;
    }

    public static List<Pair<Integer, Integer>> getOffsetList(Measurement measurement) {
        return toQuantityList(measurement)
                .stream()
                .flatMap(q -> getOffsets(q).stream())
                .sorted(Comparator.comparing(Pair::getRight))
                .collect(Collectors.toList());
    }

    public static List<Pair<Integer, Integer>> getOffsetList(List<Measurement> measurements) {
        // transform measurement to intervals
        return measurements
                .stream()
                .flatMap(m -> toQuantityList(m).stream().flatMap(q -> getOffsets(q).stream()))
                .sorted(Comparator.comparing(Pair::getRight))
                .collect(Collectors.toList());
    }

    public static List<Pair<Integer, Integer>> getOffsets(Quantity quantity) {

        List<Pair<Integer, Integer>> offsets = new ArrayList<>();

        if (quantity != null) {
            offsets.add(new ImmutablePair<>(quantity.getOffsetStart(), quantity.getOffsetEnd()));
            if (quantity.getRawUnit() != null) {
                offsets.add(new ImmutablePair<>(quantity.getRawUnit().getOffsetStart(),
                        quantity.getRawUnit().getOffsetEnd()));
            }
        }

        return offsets.stream()
                .sorted(Comparator.comparing(Pair::getRight))
                .collect(Collectors.toList());

    }


    public static Pair<Integer, Integer> toContainingOffset(List<Pair<Integer, Integer>> offsetList) {
        List<Pair<Integer, Integer>> sorted = offsetList
                .stream()
                .sorted(Comparator.comparing(Pair::getRight))
                .collect(Collectors.toList());

        return new ImmutablePair<>(sorted.get(0).getLeft(), Iterables.getLast(sorted).getRight());
    }

}
