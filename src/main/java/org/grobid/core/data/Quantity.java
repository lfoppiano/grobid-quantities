package org.grobid.core.data;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.UnitUtilities;
import org.grobid.core.utilities.WordsToNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
/*import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;*/

/**
 * Class for managing a quantity representation.
 * A quantity is a basically a value associated to a type (type of the measurement) and a unit.
 * All known quantities can be normalized to the base normalization measure system.
 * Quantities are combined to form a measurement.
 *
 * @author Patrice Lopez
 */
public class Quantity {
    private static final Logger logger = LoggerFactory.getLogger(Quantity.class);

    private Unit rawUnit = null;
    private Unit parsedUnit = null;
    private String rawValue = null;
    private BigDecimal parsedValue = null;

    private Quantity.Normalized normalizedQuantity = null;

    // as a condition, when the normalized unit is instantiated, its type must be the same as the type of the quantity
    // offset for the value only, the offsets for the unit expression are available in the raw Unit object
    // (given the fact that the same unit can be shared by several Quantity object)
    private OffsetPosition offsets = null;

    public Quantity() {
    }

    public Quantity(String rawValue, Unit rawUnit) {
        this.rawValue = rawValue;
        this.rawUnit = rawUnit;
    }

    public UnitUtilities.Unit_Type getType() {
        if (isNormalized()) {
            return getNormalizedQuantity().getType();
        } else if (parsedUnit != null && parsedUnit.getUnitDefinition() != null) {
            return getParsedUnit().getUnitDefinition().getType();
        } else {
            if (rawUnit != null && getRawUnit().hasDefinition()) {
                return getRawUnit().getUnitDefinition().getType();
            }
        }

        return null;
    }

    public Unit getRawUnit() {
        return rawUnit;
    }

    public void setRawUnit(Unit raw) {
        this.rawUnit = raw;
    }

    public Quantity.Normalized getNormalizedQuantity() {
        return normalizedQuantity;
    }

    public void setNormalizedQuantity(Quantity.Normalized normalized) {
        this.normalizedQuantity = normalized;
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String raw) {
        this.rawValue = raw;
    }

    /**
     * Set the value of the quantity and the parsedVavlue.
     * TODO: I don't know whether is better to manage the failure should be managed by who
     * is setting the value or just ignored.
     *
     * @param raw
     * @param local LOCALE (default is ENGLISH)
     * @throws NumberFormatException
     */
    /*public void setValue(String raw, Locale local) {
        this.rawValue = raw;

        // if we have alphabetical characters, we use the word to number parser
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher matcher = pattern.matcher(raw);
        if (matcher.find()) {
            WordsToNumber w2n = WordsToNumber.getInstance();
            this.parsedValue = w2n.normalize(raw, local);
        } else {
            NumberFormat format = NumberFormat.getInstance(local);
            try {
                Number number = format.parse(raw);
                this.parsedValue = new BigDecimal(number.toString());
            } catch (ParseException pe) {
                logger.error("Invalid value expression: " + raw + " , for LOCALE: " + local);
            }
        }
    }*/

    public BigDecimal getParsedValue() {
        return parsedValue;
    }

    public void setParsedValue(BigDecimal parsedValue) {
        this.parsedValue = parsedValue;
    }

    public void setOffsetStart(int start) {
        if (!hasOffset()) {
            offsets = new OffsetPosition();
        }
        offsets.start = start;
    }

    public int getOffsetStart() {
        if (hasOffset()) {
            return offsets.start;
        } else {
            return -1;
        }
    }

    public void setOffsetEnd(int end) {
        if (!hasOffset()) {
            offsets = new OffsetPosition();
        }
        offsets.end = end;
    }

    public int getOffsetEnd() {
        if (hasOffset()) {
            return offsets.end;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Quantity{");
        sb.append("type=").append(getType());
        sb.append(", rawUnit=").append(rawUnit);
        sb.append(", rawValue='").append(rawValue).append('\'');
        sb.append(", parsedValue=").append(parsedValue);
        sb.append(", normalizedQuantity=").append(normalizedQuantity);
        sb.append(", offsets=").append(offsets);
        sb.append('}');
        return sb.toString();
    }

    /*
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ ");

        if (type != null)
            builder.append(type.getName()).append("\t");

        if (rawValue != null)
            builder.append(rawValue).append("\t");

        if (rawUnit != null)
            builder.append(rawUnit.toString()).append("\t");

        if (isParseable()) {
            builder.append(parsedValue).append("\t");
        }

        if (isNormalized()) {
            builder.append(normalizedQuantity).append("\t");
        }

        if (hasOffset()) {
            builder.append(offsets.toString());
        }

        builder.append(" ]");
        return builder.toString();
    }
    */

    private boolean hasOffset() {
        return offsets != null;
    }

    private boolean isParseable() {
        return parsedValue != null;
    }

    // WARNING! a quantity can have a value without unit, and not being empty (e.g. counts) !!
    public boolean isEmpty() {
        return rawUnit == null && (StringUtils.isEmpty(rawValue) || !isParseable()) && (isNormalized() && normalizedQuantity.isEmpty());
    }

    public boolean isNormalized() {
        return normalizedQuantity != null;
    }

    public String toJson() {
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        StringBuilder json = new StringBuilder();
        boolean started = false;
        json.append("{ ");

        if (getType() != null) {
            byte[] encodedName = encoder.quoteAsUTF8(getType().getName());
            String outputName = new String(encodedName);
            json.append("\"type\" : \"" + outputName + "\"");
            started = true;
        }

        if (rawValue != null) {
            if (!started) {
                started = true;
            } else {
                json.append(", ");
            }
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
            } else
                json.append(", ");
            json.append("\"rawUnit\" : " + rawUnit.toJson());
        }

        if (isParseable()) {
            if (!started) {
                started = true;
            } else {
                json.append(", ");
            }
            json.append("\"parsedValue\" : " + parsedValue);
        }

        /*if (parsedUnit != null) {
            if (!started) {
                started = true;
            } else {
                json.append(", ");
            }
            json.append("\"parsedUnit\" : " + parsedUnit.toJson());
        }*/

        if (isNormalized()) {
            if (!started) {
                started = true;
            } else {
                json.append(", ");
            }
            json.append("\"normalizedQuantity\" : " + normalizedQuantity.getValue() + ",");
            json.append("\"normalizedUnit\" : " + normalizedQuantity.getUnit().toJson());
        }

        if (hasOffset()) {
            if (getOffsetStart() != -1) {
                if (!started) {
                    started = true;
                } else {
                    json.append(", ");
                }
                json.append("\"offsetStart\" : " + getOffsetStart());
            }

            if (getOffsetEnd() != -1) {
                if (!started) {
                    started = true;
                } else {
                    json.append(", ");
                }
                json.append("\"offsetEnd\" : " + getOffsetEnd());
            }
        }

        json.append(" }");
        return json.toString();
    }

    public Unit getParsedUnit() {
        return parsedUnit;
    }

    public void setParsedUnit(Unit parsedUnit) {
        this.parsedUnit = parsedUnit;
    }

    public class Normalized {
        private String rawValue = null;
        private BigDecimal value = null;
        private Unit unit = null;

        private UnitUtilities.Unit_Type type;

        public String getRawValue() {
            return rawValue;
        }

        public void setRawValue(String rawValue) {
            this.rawValue = rawValue;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        public Unit getUnit() {
            return unit;
        }

        public void setUnit(Unit unit) {
            this.unit = unit;
        }

        //A normalized quantity without either a value or an unit, is not valid, therefore empty.
        public boolean isEmpty() {
            return unit == null || value == null;
        }

        public String toJson() {
            JsonStringEncoder encoder = JsonStringEncoder.getInstance();
            StringBuilder json = new StringBuilder();
            boolean started = false;
            json.append("{ ");

            if (unit != null) {
                if (!started) {
                    started = true;
                } else {
                    json.append(", ");
                }
                json.append("\"normalizedUnit\":" + unit.toJson());
            }

            if (value != null) {
                if (!started) {
                    started = true;
                } else {
                    json.append(", ");
                }
                json.append("\"normalizedValue\":" + value.toString());
            }

            json.append("}");
            return json.toString();

        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Normalized{");
            sb.append("rawValue='").append(rawValue).append('\'');
            sb.append(", value=").append(value);
            sb.append(", unit=").append(unit);
            sb.append('}');
            return sb.toString();
        }

        public UnitUtilities.Unit_Type getType() {
            if (hasUnitDefinition()) {
                return getUnit().getUnitDefinition().getType();
            }
            return null;
        }

        private boolean hasUnitDefinition() {
            return getUnit() != null && getUnit().hasDefinition();
        }
    }
}