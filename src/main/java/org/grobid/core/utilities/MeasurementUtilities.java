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
                Quantity quantity = measurement.getQuantityAtomic();
                if (quantity == null)
                    continue;

                // first check for a base range interval
                if ( (quantity.getRawValue() != null) && (quantity.getRawValue().indexOf("±") != -1) ) {
                    // base range interval
                    measurement.setType(UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE);
                    String rawValue = quantity.getRawValue();
                    int ind = quantity.getRawValue().indexOf("±");
                    Quantity quantityBase = new Quantity();
                    quantityBase.setRawValue(rawValue.substring(0,ind-1).trim());
                    quantityBase.setRawUnit(quantity.getRawUnit());
                    quantityBase.setOffsetStart(quantity.getOffsetStart());
                    quantityBase.setOffsetEnd(quantity.getOffsetStart()+ind-1);
                    quantityBase.setType(quantity.getType());

                    Quantity quantityRange = new Quantity();
                    quantityRange.setRawValue(rawValue.substring(ind+1, rawValue.length()).trim());
                    quantityRange.setRawUnit(quantity.getRawUnit());
                    quantityRange.setOffsetStart(quantity.getOffsetStart()+ind+1);
                    quantityRange.setOffsetEnd(quantity.getOffsetEnd());
                    quantityRange.setType(quantity.getType());

                    measurement.setQuantityBase(quantityBase);
                    measurement.setQuantityRange(quantityRange);
                    measurement.setAtomicQuantity(null);
                    newMeasurements.add(measurement);
                    System.out.println(measurement.toString());
                }
                else {
                    // if the unit is too far from the value, the measurement needs to be filtered out
                    int start = quantity.getOffsetStart();
                    int end = quantity.getOffsetEnd();
                    Unit rawUnit = quantity.getRawUnit();
                    if (rawUnit != null) {
                        int startU = rawUnit.getOffsetStart();
                        int endU = rawUnit.getOffsetEnd();
                        if ((Math.abs(end - startU) < 40) || (Math.abs(endU - start) < 40)) {
                            newMeasurements.add(measurement);
                        }
                    }
                    else
                        newMeasurements.add(measurement);
                }
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
                Quantity quantityLeast = measurement.getQuantityLeast();
                Quantity quantityMost = measurement.getQuantityMost();

                if ((quantityLeast == null) && (quantityMost == null))
                    continue;
                if ( ((quantityLeast != null) && (quantityMost == null)) || ((quantityLeast == null) && (quantityMost != null)) ) {
                    Measurement newMeasurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
                    Quantity quantity = null;
                    if (quantityLeast != null) {
                        quantity = quantityLeast;
                    } else if (quantityMost != null) {
                        quantity = quantityMost;
                    }
                    if (quantity != null) {
                        newMeasurement.setAtomicQuantity(quantity);
                        newMeasurements.add(newMeasurement);
                    }
                } else if ( (quantityLeast != null) && (quantityMost != null)) {
                    // if the interval is expressed over a chunck of text which is too large, it is a recognition error
                    // and we can replace it by two atomic measurements
                    int startL = quantityLeast.getOffsetStart();
                    int endL = quantityLeast.getOffsetEnd();
                    int startM = quantityMost.getOffsetStart();
                    int endM = quantityMost.getOffsetEnd();

                    if ((Math.abs(endL - startM) > 80) && (Math.abs(endM - startL) > 80)) {
                        // we replace the interval measurement by two atomic measurements
                        Measurement newMeasurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
                        // have to check the position of value and unit for valid atomic measure
                        Unit rawUnit = quantityLeast.getRawUnit();
                        if (rawUnit != null) {
                            int startU = rawUnit.getOffsetStart();
                            int endU = rawUnit.getOffsetEnd();
                            if ((Math.abs(endL - startU) < 40) || (Math.abs(endU - startL) < 40)) {
                                newMeasurement.setAtomicQuantity(quantityLeast);
                                newMeasurements.add(newMeasurement);
                            }
                        }
                        else {
                            newMeasurement.setAtomicQuantity(quantityLeast);
                                newMeasurements.add(newMeasurement);
                        }
                        
                        newMeasurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
                        rawUnit = quantityMost.getRawUnit();
                        if (rawUnit != null) {
                            int startU = rawUnit.getOffsetStart();
                            int endU = rawUnit.getOffsetEnd();
                            if ((Math.abs(endM - startU) < 40) || (Math.abs(endU - startM) < 40)) {
                                newMeasurement.setAtomicQuantity(quantityMost);
                                newMeasurements.add(newMeasurement);
                            }
                        }
                        else {
                            newMeasurement.setAtomicQuantity(quantityMost);
                                newMeasurements.add(newMeasurement);
                        }

                    } else
                        newMeasurements.add(measurement);
                } else
                    newMeasurements.add(measurement);
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                // list must be consistent in unit type, and avoid too large chunk 
                List<Quantity> quantities = measurement.getQuantityList();
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
            if (measurement.getType() == null)
                continue;
            else if (measurement.getType() == UnitUtilities.Measurement_Type.VALUE) {
                updateQuantity(measurement.getQuantityAtomic());
            }
            else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
                updateQuantity(measurement.getQuantityLeast());
                updateQuantity(measurement.getQuantityMost());
            }
            else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
                updateQuantity(measurement.getQuantityBase());
                updateQuantity(measurement.getQuantityRange());
                // the two quantities bellow are normally not yet set-up
                //updateQuantity(measurement.getQuantityLeast());
                //updateQuantity(measurement.getQuantityMost());
            }
            else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                if (measurement.getQuantityList() != null) {
                    for (Quantity quantity : measurement.getQuantityList()) {
                        if (quantity == null)
                            continue;
                        updateQuantity(quantity);
                    }
                }
            }
        }
        return measurements;
    }

    private void updateQuantity(Quantity quantity) {
        if ((quantity != null) && (!quantity.isEmpty())) {
            Unit rawUnit = quantity.getRawUnit();

            if ((rawUnit != null) && rawUnit.getRawName() != null) {
                UnitDefinition foundUnit = quantityLexicon.getUnitbyName(rawUnit.getRawName().trim());
                if (foundUnit == null)
                    foundUnit = quantityLexicon.getUnitbyNotation(rawUnit.getRawName().trim());

                if (foundUnit != null) {
                    rawUnit.setUnitDefinition(foundUnit);
                    if (foundUnit.getType() != null)
                        quantity.setType(foundUnit.getType());
                }
            }
        }
    }
}