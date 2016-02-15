package org.grobid.core.utilities;

import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.Measurement;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.utilities.UnitUtilities;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Try to solve measurement extracted attributes (unit, values).
 *
 * @author Patrice Lopez
 */
public class MeasurementUtilities {
    private static final Logger logger = LoggerFactory.getLogger(MeasurementUtilities.class);

    private QuantityLexicon quantityLexicon = null;

    public MeasurementUtilities() {
        quantityLexicon = QuantityLexicon.getInstance();
    }

    /**
     * Check the wellformness of a given list of measurements. 
     * In particular, if intervals are not consistent, they are transformed 
     * in atomic value measurements. 
     */
    public static List<Measurement> postCorrection(List<Measurement> measurements) {
        List<Measurement> newMeasurements = new ArrayList<Measurement>();

        for(Measurement measurement : measurements) {
            if (measurement.getType() == UnitUtilities.Measurement_Type.VALUE) {
                newMeasurements.add(measurement);
            }
            else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL) {
                List<Quantity> quantities = measurement.getQuantities();
                if ( (quantities.size() == 1) || (measurement.getQuantityLeast() == null) || (measurement.getQuantityMost() == null) ) {
                    Measurement newMeasurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
                    Quantity quantity = null;
                    if (quantities.size() == 1)
                        quantity = measurement.getQuantityLeast();
                    else if ( (quantities.size() == 2)  && (quantities.get(0) == null) )
                        quantity = quantities.get(1);
                    else if (quantities.size() == 2)
                        quantity = measurement.getQuantityLeast();
                    if (quantity != null) {
                        newMeasurement.setAtomicQuantity(quantity);
                        newMeasurements.add(newMeasurement);
                    }
                }
                else if ( (quantities.size() == 2) && (measurement.getQuantityLeast() != null) && (measurement.getQuantityMost() != null) ) {
                    // if the interval is expressed over a chunck of text which is too large, it is a recognition error
                    // and we can replace it by two atomic measurements
                    Quantity quantityLeast = measurement.getQuantityLeast();
                    Quantity quantityMost = measurement.getQuantityMost();
                    int startL = quantityLeast.getOffsetStart();
                    int endL = quantityLeast.getOffsetEnd();
                    int startM = quantityMost.getOffsetStart();
                    int endM = quantityMost.getOffsetEnd();

                    if ( (Math.abs(endL-startM) > 80) && (Math.abs(endM-startL) > 80) ) {
                        // we replace the interval measurement by two atomic measurements
                        Measurement newMeasurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
                        newMeasurement.setAtomicQuantity(quantityLeast);
                        newMeasurements.add(newMeasurement);
                        newMeasurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
                        newMeasurement.setAtomicQuantity(quantityMost);
                        newMeasurements.add(newMeasurement);
                    }
                    else
                        newMeasurements.add(measurement);
                }
                else
                    newMeasurements.add(measurement);
            }
            else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                newMeasurements.add(measurement);
            }
        }

        return newMeasurements;
    }

    /**
     *  Right now, only basic matching of units based on lexicon look-up and value validation
     *  via regex.
     */
    public void solve(Measurement measurement) {
        if (measurement.getQuantities() == null)
            return;
        for(Quantity quantity : measurement.getQuantities()) {
            Unit rawUnit = quantity.getRawUnit();
            
        }
    }

}