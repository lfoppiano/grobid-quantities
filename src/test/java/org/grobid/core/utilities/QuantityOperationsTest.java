package org.grobid.core.utilities;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.layout.LayoutToken;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;

public class QuantityOperationsTest {

    @Test
    public void testGetOffsetList() {
        List<Measurement> measurementList = new ArrayList<>();

        Measurement measurement1 = new Measurement();
        measurement1.setType(UnitUtilities.Measurement_Type.VALUE);
        final Unit unit = new Unit("kg", 4, 6);
        measurement1.setAtomicQuantity(new Quantity("20", unit, 2, 4));
        measurementList.add(measurement1);

        Measurement measurement2 = new Measurement();
        measurement2.setType(UnitUtilities.Measurement_Type.VALUE);
        final Unit unit2 = new Unit("mm", 85, 87);
        measurement2.setAtomicQuantity(new Quantity("90", unit2, 83, 85));
        measurementList.add(measurement2);

        Measurement measurement3 = new Measurement();
        measurement3.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        final Unit unit3 = new Unit("mm", 120, 123);
        measurement3.setQuantityMost(new Quantity("90", unit3, 125, 127));
        measurementList.add(measurement3);

        Measurement measurement4 = new Measurement();
        measurement4.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        measurement4.setQuantityMost(new Quantity("90", null, 150, 155));
        measurementList.add(measurement4);

        List<Pair<Integer, Integer>> offsetList = QuantityOperations.getOffset(measurementList);

        assertThat(offsetList, hasSize(7));

        assertThat(offsetList.get(0).getLeft(), is(2));
        assertThat(offsetList.get(0).getRight(), is(4));

        assertThat(offsetList.get(1).getLeft(), is(4));
        assertThat(offsetList.get(1).getRight(), is(6));
    }

    @Test
    public void testToQuantityList_atomic() throws Exception {
        Measurement measurement1 = new Measurement();
        measurement1.setType(UnitUtilities.Measurement_Type.VALUE);
        final Unit unit = new Unit("kg", 10, 12);
        measurement1.setAtomicQuantity(new Quantity("20", unit, 2, 4));

        List<Quantity> quantities = QuantityOperations.toQuantityList(measurement1);

        assertThat(quantities, hasSize(1));
    }

    @Test
    public void testToQuantityList_interval() throws Exception {
        Measurement measurement3 = new Measurement();
        measurement3.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        final Unit unit3 = new Unit("mm", 120, 123);
        measurement3.setQuantityMost(new Quantity("90", unit3, 125, 127));

        List<Quantity> quantities = QuantityOperations.toQuantityList(measurement3);

        assertThat(quantities, hasSize(1));
    }

    @Test
    public void testToQuantity_interval2() throws Exception {
        Measurement measurement3 = new Measurement();
        measurement3.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        measurement3.setQuantityMost(new Quantity("90", new Unit("mm", 120, 123), 125, 127));
        measurement3.setQuantityLeast(new Quantity("90", null, 150, 155));

        List<Quantity> quantities = QuantityOperations.toQuantityList(measurement3);

        assertThat(quantities, hasSize(2));
    }

    @Test
    public void testToQuantity_range1() throws Exception {
        Measurement measurement3 = new Measurement();
        measurement3.setType(UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE);
        measurement3.setQuantityBase(new Quantity("90", new Unit("mm", 120, 123), 125, 127));
        measurement3.setQuantityRange(new Quantity("1", null, 150, 155));

        List<Quantity> quantities = QuantityOperations.toQuantityList(measurement3);

        assertThat(quantities, hasSize(2));
    }

    @Test
    public void testToQuantity_range2() throws Exception {
        Measurement measurement3 = new Measurement();
        measurement3.setType(UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE);
        measurement3.setQuantityBase(new Quantity("90", new Unit("mm", 120, 123), 125, 127));

        List<Quantity> quantities = QuantityOperations.toQuantityList(measurement3);

        assertThat(quantities, hasSize(1));
    }

    @Test
    public void testGetOffsets() throws Exception {
        Quantity q1 = new Quantity("90", new Unit("mm", 120, 123), 125, 127);
        List<Pair<Integer, Integer>> offsets = QuantityOperations.getOffsets(q1);

        assertThat(offsets, hasSize(2));
        assertThat(offsets.get(0).getRight(), is(lessThan(offsets.get(1).getRight())));
        assertThat(offsets.get(0).getLeft(), is(lessThan(offsets.get(1).getLeft())));
    }

    @Test
    public void testGetOffset_sorting() throws Exception {
        Measurement measurement1 = new Measurement();
        measurement1.setType(UnitUtilities.Measurement_Type.VALUE);
        final Unit unit = new Unit("kg", 10, 12);
        measurement1.setAtomicQuantity(new Quantity("20", unit, 2, 4));

        Measurement measurement3 = new Measurement();
        measurement3.setType(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX);
        measurement3.setQuantityMost(new Quantity("90", new Unit("mm", 120, 123), 125, 127));
        measurement3.setQuantityLeast(new Quantity("90", null, 150, 155));

        List<Pair<Integer, Integer>> offsets = QuantityOperations.getOffset(Arrays.asList(measurement3, measurement1));

        assertThat(offsets, hasSize(5));
        assertThat(offsets.get(0).getRight(), is(lessThan(offsets.get(1).getRight())));
        assertThat(offsets.get(0).getLeft(), is(lessThan(offsets.get(1).getLeft())));
    }

    @Test
    public void testGetOffset_list() throws Exception {
        List<Quantity> list = Arrays.asList(
                new Quantity("bao", null, 3, 110),
                new Quantity("miao", new Unit("seconds", 2, 24), 22, 150)
        );

        List<Pair<Integer, Integer>> offsets = QuantityOperations.getOffsets(list);

        assertThat(offsets, hasSize(3));
    }

    @Test
    public void testGetContainingOffset() throws Exception {
        Pair<Integer, Integer> offsets = QuantityOperations.getContainingOffset(
                new Quantity("miao", new Unit("seconds", 2, 24), 22, 150));

        assertThat(offsets.getLeft(), is(2));
        assertThat(offsets.getRight(), is(150));
    }

    @Test
    public void testSynchroniseLayoutTokenWithMentions_longMention() {
        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken("A 20kg ingot is made in a " +
                "high frequency induction melting furnace and forged to 30mm in thickness and 90mm in width at 850 to 1,150°C.");

        List<Pair<Integer, Integer>> offsetList = Arrays.asList(
                new ImmutablePair<>(2, 50),
                new ImmutablePair<>(103, 134)
        );

        List<Boolean> booleans = QuantityOperations.synchroniseLayoutTokensWithOffsets(tokens, offsetList);

        assertThat(booleans, hasSize(tokens.size()));

        assertThat(booleans.get(2), is(true));
        assertThat(booleans.get(3), is(true));
        assertThat(booleans.get(4), is(true));
        assertThat(booleans.get(5), is(true));
        assertThat(booleans.get(6), is(true));
        assertThat(booleans.get(7), is(true));
        assertThat(booleans.get(8), is(true));
        assertThat(booleans.get(9), is(true));
        assertThat(booleans.get(10), is(true));
        assertThat(booleans.get(11), is(true));
        assertThat(booleans.get(12), is(true));
        assertThat(booleans.get(13), is(true));
        assertThat(booleans.get(14), is(true));
    }

    @Test
    public void testSynchroniseLayoutTokenWithMentions_consecutives() {

        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken("A 20kg ingot is made in a " +
                "high frequency induction melting furnace and forged to 30mm in thickness and 90mm in width at 850 to 1,150°C.");

        List<Pair<Integer, Integer>> offsetList = Arrays.asList(
                new ImmutablePair<>(2, 4),
                new ImmutablePair<>(4, 6),
                new ImmutablePair<>(81, 83),
                new ImmutablePair<>(83, 85),
                new ImmutablePair<>(103, 105),
                new ImmutablePair<>(105, 107),
                new ImmutablePair<>(120, 123),
                new ImmutablePair<>(127, 132),
                new ImmutablePair<>(132, 134)
        );

        List<Boolean> booleans = QuantityOperations.synchroniseLayoutTokensWithOffsets(tokens, offsetList);

        assertThat(booleans, hasSize(tokens.size()));

        assertThat(booleans.stream().filter(b-> b).count(), is(greaterThan(0L)));

        assertThat(booleans.get(2), is(true));
        assertThat(booleans.get(3), is(true));
        assertThat(booleans.get(4), is(false));
    }

}