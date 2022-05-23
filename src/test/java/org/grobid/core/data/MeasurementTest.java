package org.grobid.core.data;

import org.grobid.core.utilities.UnitUtilities;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MeasurementTest {

    @Test
    public void testIsValid_noValues_shouldReturnFalse() {
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.VALUE);

        assertThat(measurement.isValid(), is(false));
    }

    @Test
    public void testIsValid_Values_shouldReturnTrue() {
        Measurement measurement = new Measurement();
        measurement.setType(UnitUtilities.Measurement_Type.VALUE);
        measurement.setAtomicQuantity(new Quantity("123", new Unit("m")));

        assertThat(measurement.isValid(), is(true));
    }

}