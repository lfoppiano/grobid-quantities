package org.grobid.core.utilities;

import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.UnitDefinition;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.utilities.UnitUtilities;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Try to solve measurement extracted attributes (unit, values).
 *
 * @author Patrice Lopez
 */
public class MeasurementUtilities {
    private static final Logger logger = LoggerFactory.getLogger(MeasurementUtilities.class);

    private QuantityLexicon quantityLexicon = null;
    private UnitUtilities unitUtilities = null;

    public MeasurementUtilities() {
        quantityLexicon = QuantityLexicon.getInstance();
        unitUtilities = UnitUtilities.getInstance();
    }

    /**
     * Check the wellformness of a given list of measurements.
     * In particular, if intervals are not consistent, they are transformed
     * in atomic value measurements.
     */
    public static List<Measurement> postCorrection(List<Measurement> measurements) {
        List<Measurement> newMeasurements = new ArrayList<Measurement>();

        for (Measurement measurement : measurements) {
            if (measurement.getType() == UnitUtilities.Measurement_Type.VALUE) {
                List<Quantity> quantities = measurement.getQuantities();
                if ((quantities == null) || (quantities.size() == 0))
                    continue;
                newMeasurements.add(measurement);
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL) {
                List<Quantity> quantities = measurement.getQuantities();
                if ((quantities == null) || (quantities.size() == 0))
                    continue;
                if ((quantities.size() == 1) || (measurement.getQuantityLeast() == null) || (measurement.getQuantityMost() == null)) {
                    Measurement newMeasurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
                    Quantity quantity = null;
                    if (quantities.size() == 1) {
                        quantity = measurement.getQuantityLeast();
                    } else if ((quantities.size() == 2) && (quantities.get(0) == null)) {
                        quantity = quantities.get(1);
                    } else if (quantities.size() == 2) {
                        quantity = measurement.getQuantityLeast();
                    }
                    if (quantity != null) {
                        newMeasurement.setAtomicQuantity(quantity);
                        newMeasurements.add(newMeasurement);
                    }
                } else if ((quantities.size() == 2) && (measurement.getQuantityLeast() != null) && (measurement.getQuantityMost() != null)) {
                    // if the interval is expressed over a chunck of text which is too large, it is a recognition error
                    // and we can replace it by two atomic measurements
                    Quantity quantityLeast = measurement.getQuantityLeast();
                    Quantity quantityMost = measurement.getQuantityMost();
                    int startL = quantityLeast.getOffsetStart();
                    int endL = quantityLeast.getOffsetEnd();
                    int startM = quantityMost.getOffsetStart();
                    int endM = quantityMost.getOffsetEnd();

                    if ((Math.abs(endL - startM) > 80) && (Math.abs(endM - startL) > 80)) {
                        // we replace the interval measurement by two atomic measurements
                        Measurement newMeasurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
                        newMeasurement.setAtomicQuantity(quantityLeast);
                        newMeasurements.add(newMeasurement);
                        newMeasurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
                        newMeasurement.setAtomicQuantity(quantityMost);
                        newMeasurements.add(newMeasurement);
                    } else
                        newMeasurements.add(measurement);
                } else
                    newMeasurements.add(measurement);
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                // list must be consistent in unit type, and avoid too large chunk 
                List<Quantity> quantities = measurement.getQuantities();
                if ((quantities == null) || (quantities.size() == 0))
                    continue;

                /* // case actually not seen
                Unit currentUnit = null;
                Measurement newMeasurement = new Measurement(UnitUtilities.Measurement_Type.CONJUNCTION);
                for(Quantity quantity : quantities) {
                    if (currentUnit == null)
                        currentUnit = quantity.getRawUnit();
                    else {
                        Unit newUnit = quantity.getRawUnit();

                        // is it a new unit?
                        if ((currentUnit != null) && (newUnit != null) && (!currentUnit.getRawName().equals(newUnit.getRawName())) ) {
                             // we have a new unit, so we split the list
                            if ( (newMeasurement != null) && (newMeasurement.getQuantities() != null) && (newMeasurement.getQuantities().size() > 0) )
                                newMeasurements.add(newMeasurement);
                            newMeasurement = new Measurement(UnitUtilities.Measurement_Type.CONJUNCTION);
                            newMeasurement.addQuantity(quantity);
                            currentUnit = newUnit;
                        }
                        else {
                            // same unit, we extend the current list
                            newMeasurement.addQuantity(quantity);
                        }
                    }

                }
                if ( (newMeasurement != null) && (newMeasurement.getQuantities() != null) && (newMeasurement.getQuantities().size() > 0) )
                    newMeasurements.add(newMeasurement);*/

                // the case of atomic values within list should be cover here
                // in this case, we have a list followed by an atomic value, then a following list without unit attachment, with possibly
                // only one quantity - the correction is to extend the starting list with the remaining list after the atomic value, attaching
                // the unit associated to the starting list to the added quantities 


                newMeasurements.add(measurement);
            }
        }

        return newMeasurements;
    }

    /**
     * Right now, only basic matching of units based on lexicon look-up and value validation
     * via regex.
     */
    public List<Measurement> solve(List<Measurement> measurements) {
        for (Measurement measurement : measurements) {
            if (isEmpty(measurement.getQuantities()))
                continue;
            for (Quantity quantity : measurement.getQuantities()) {
                if (quantity == null)
                    continue;

                Unit rawUnit = quantity.getRawUnit();

                if ((rawUnit != null) && rawUnit.getRawName() != null) {
                    UnitDefinition foundUnit = quantityLexicon.getUnitbyName(rawUnit.getRawName().trim());
                    if (foundUnit == null)
                        foundUnit = quantityLexicon.getUnitbyNotation(rawUnit.getRawName().trim());

                    if (foundUnit != null) {
                        rawUnit.setUnitDefinition(foundUnit);
                    }
                }
            }
        }
        return measurements;
    }
}