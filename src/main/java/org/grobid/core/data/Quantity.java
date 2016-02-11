package org.grobid.core.data;

import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.UnitUtilities;

/**
 * Class for managing a quantity representation. A quantity is a basically a value associated to a type 
 * (type of the measurement) and a unit. Quantities are combined to form a measurement.  
 *
 * @author Patrice Lopez
 */
public class Quantity {
	
	private String rawString = null;
	
	private UnitUtilities.Unit_Type type; // type of measurement
	private Unit rawUnit = null;
	private Unit normalizedUnit = null; // which gives also the system of the unit (SI, imperial, etc.)
	private String rawValue = null;
	private double normalizedValue = 0.0;
	
	// as a condition, when the normalized unit is instanciated, its type must be the same as the type
	// of the quantity 
	
	// in case of ranged values, this is the second (highest) value 
	// if rawValueHigh is null, we don't have a range
	private String rawValueHigh = null;
	private double normalizedValueHigh = 0.0;
	
    private OffsetPosition offsets = null;

    public Quantity() {
        offsets = new OffsetPosition();
    }

	public Quantity(String rawString) {
        offsets = new OffsetPosition();
		this.rawString = rawString;
    }

	public String getRawString() {
		return rawString;
	}

	public void setRawString(String raw) {
		this.rawString = raw;
	}

	public UnitUtilities.Unit_Type getType() {
        return type;
    }

	public void setType(UnitUtilities.Unit_Type type) {
        this.type = type;
    }

    public Unit getRawUnit() {
        return rawUnit;
    }

	public void setRawUnit(Unit raw) {
        this.rawUnit = raw;
    }
	
	public Unit getNormalizedUnit() {
        return normalizedUnit;
    }

	public void setNormalizedUnit(Unit normalized) {
        this.normalizedUnit = normalized;
    }
	
	public String getRawValue() {
        return rawValue;
    }

	public void setRawValue(String raw) {
        this.rawValue = raw;
    }
	
	public double getNormalizedValue() {
        return normalizedValue;
    }

	public void setNormalizedValue(double normalized) {
        this.normalizedValue = normalized;
    }

	public String getRawValueHigh() {
        return rawValueHigh;
    }

	public void setRawValueHigh(String raw) {
        this.rawValueHigh = raw;
    }
	
	public double getNormalizedValueHigh() {
        return normalizedValueHigh;
    }

	public void setNormalizedValueHigh(double normalized) {
        this.normalizedValueHigh = normalized;
    }
	
	public void setOffsetStart(int start) {
        offsets.start = start;
    }

    public int getOffsetStart() {
        return offsets.start;
    }

    public void setOffsetEnd(int end) {
        offsets.end = end;
    }

    public int getOffsetEnd() {
        return offsets.end;
    }

	public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ ");
        if (type != null)
            builder.append(type.getName()).append("\t");
        if (rawValue != null)
            builder.append(rawValue).append("\t");
        if (rawUnit != null)
            builder.append(rawUnit.toString()).append("\t");
        if (offsets != null)
            builder.append(offsets.toString());
        builder.append(" ]");
        return builder.toString();
    }

}