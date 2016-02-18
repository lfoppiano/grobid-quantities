package org.grobid.core.utilities;

import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lfoppiano on 18.02.16.
 */
public class MeasurementUtilitiesTest {

    private MeasurementUtilities target;

    @Before
    public void setUp() throws Exception {
        target = new MeasurementUtilities();
    }

    @Ignore("Doesn't test anything...")
    @Test
    public void testSolve() throws Exception {
        List<Measurement> measurementList = new ArrayList<>();

        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.VALUE);

        Quantity quantity = new Quantity("10m", new Unit("meter"));
        measurement.addQuantity(quantity);
        measurementList.add(measurement);

        System.out.println(target.solve(measurementList));
    }


}