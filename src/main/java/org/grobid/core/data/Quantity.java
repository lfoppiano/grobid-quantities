package org.grobid.core.data;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.UnitUtilities;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.quantity.Length;
import java.util.Map;

import org.codehaus.jackson.io.JsonStringEncoder;

/**
 * Class for managing a quantity representation. A quantity is a basically a value associated to a type
 * (type of the measurement) and a unit. Quantities are combined to form a measurement.
 *
 * @author Patrice Lopez
 */
public class Quantity {
    private UnitUtilities.Unit_Type type; // type of measurement

    private Unit rawUnit = null;
    private String rawValue = null;
    private String rawString = null;

    private Unit normalizedUnit = null; // which gives also the system of the unit (SI, imperial, etc.)
    private double normalizedValue = 0.0;

    // as a condition, when the normalized unit is instanciated, its type must be the same as the type
    // of the quantity

    // offset for the value only, the offsets for the unit expression are available in the raw Unit object
    // (given the fact that the same unit can be shared by several Quantity object)
    private OffsetPosition offsets = null;
    private Map<String, Integer> productForm;

    public Quantity() {
    }

    public Quantity(String rawString) {
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

    public void setOffsetStart(int start) {
        if (offsets == null)
            offsets = new OffsetPosition();
        offsets.start = start;
    }

    public int getOffsetStart() {
        if (offsets != null)
            return offsets.start;
        else return -1;
    }

    public void setOffsetEnd(int end) {
        if (offsets == null)
            offsets = new OffsetPosition();
        offsets.end = end;
    }

    public int getOffsetEnd() {
        if (offsets != null)
            return offsets.end;
        else return -1;
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

    public boolean isEmpty() {
        return StringUtils.isEmpty(rawValue) || ((rawUnit == null || rawUnit.isEmpty()) && StringUtils.isEmpty(rawValue));
    }

    public boolean isNormalized() {
        return normalizedUnit != null;
    }

    public void setProductForm(Map<String, Integer> productForm) {
        this.productForm = productForm;
    }

    public String toJson() {
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        StringBuilder json = new StringBuilder();
        boolean started = false;
        json.append("{ ");
        if (type != null) {
            byte[] encodedName = encoder.quoteAsUTF8(type.getName());
            String outputName = new String(encodedName);
            json.append("\"type\" : \"" + outputName + "\"");
            started = true;
        }
        if (rawValue != null) {
            if (!started) {
                started = true;
            }
            else
                json.append(", ");
            byte[] encodedRawValue = encoder.quoteAsUTF8(rawValue);
            String outputRawValue = new String(encodedRawValue);
            if (!started) {
                json.append(", ");
                started = true;
            }
            json.append("\"rawValue\" : \"" + outputRawValue + "\"");
        }
        if (rawUnit != null) {
            if (!started) {
                started = true;
            }
            else
                json.append(", ");
            json.append("\"rawUnit\" : " + rawUnit.toJson() );
        }
        if (offsets != null) {
            if (getOffsetStart() != -1) {
                if (!started) {
                    started = true;
                }
                else
                    json.append(", ");
                json.append("\"offsetStart\" : " + getOffsetStart());
            }
            if (getOffsetEnd() != -1) {
                if (!started) {
                    started = true;
                }
                else
                    json.append(", ");
                json.append("\"offsetEnd\" : " + getOffsetEnd());
            }
        }

        json.append(" }");
        return json.toString();
    }

}