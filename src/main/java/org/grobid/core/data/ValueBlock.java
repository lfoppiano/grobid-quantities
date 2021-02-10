package org.grobid.core.data;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.core.util.BufferRecyclers;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;

/**
 * This class is responsible to hold the structured representation of a value expressed in
 * the form:
 * - number x base^pow
 * - alphabetic number
 * - time expression (date, time)
 * - exponential formulas e^exp
 */
public class ValueBlock {

    private String rawValue;

    private String rawTaggedValue;

    private Block number = null;

    private Block base = null;

    private Block pow = null;

    private Block exp = null;

    private Block time = null;

    private Block alpha = null;

    public ValueBlock() {
    }

    public ValueBlock(Block number, Block base, Block pow) {
        this.number = number;
        this.base = base;
        this.pow = pow;
    }

    public ValueBlock(Block number, Block exp) {
        this.number = number;
        this.exp = exp;
    }

    public Type getType() {
        if (number != null) {
            if (base != null && pow != null) {
                return Type.NUMBER;
            } else if (exp != null) {
                return Type.EXPONENT;
            }
            return Type.NUMBER;
        } else if (time != null) {
            return Type.TIME;
        } else if (alpha != null) {
            return Type.ALPHABETIC;
        } else {
            if (base != null && pow != null) {
                return Type.NUMBER;
            } else if (exp != null) {
                return Type.EXPONENT;
            }
        }

        return Type.UNKNOWN;

    }

    @Override
    public String toString() {
        switch (getType()) {
            case ALPHABETIC:
                return getAlphaAsString();
            case TIME:
                return getTimeAsString();
            case EXPONENT:
                StringBuilder sb = new StringBuilder();

                if (number != null) {
                    sb.append(number);
                }
                if (getPow() != null && getBase() != null) {
                    sb.append(getBase() + "^" + getPow());
                }

                return sb.toString();
            case NUMBER:
                sb = new StringBuilder();

                if (number != null) {
                    sb.append(number);
                }

                if (getExp() != null) {
                    sb.append("e^" + getExp());
                } else {
                    if (getPow() != null && getBase() != null) {
                        if(number != null) {
                            sb.append(" x ");
                        }
                        sb.append(getBase() + "^" + getPow());
                    }
                }
                return sb.toString();
            case UNKNOWN:
                break;

        }
        return null;
    }

    public String getNumberAsString() {
        return Objects.toString(number, "");
    }

    public Block getNumber() {
        return number;
    }

    public void setNumber(String number) {
        if (this.number == null) this.number = new Block(number);

        this.number.setValue(number);
    }

    public String getBaseAsString() {
        return base.getValue();
    }

    public Block getBase() {
        return base;
    }

    public void setBase(String base) {
        if (base != null) {
            this.base = new Block(base);
        }
    }

    public String getPowAsString() {
        return String.valueOf(pow);
    }

    public Block getPow() {
        return pow;
    }

    public void setPow(String pow) {
        if (this.pow == null) this.pow = new Block(pow);

        this.pow.setValue(pow);
    }

    public String getExpAsString() {
        return String.valueOf(exp);
    }

    public Block getExp() {
        return exp;
    }

    public void setExp(String exp) {
        if (this.exp == null) this.exp = new Block(exp);

        this.exp.setValue(exp);
    }

    public String getTimeAsString() {
        return String.valueOf(time);
    }

    public Block getTime() {
        return time;
    }

    public void setTime(String time) {
        if (this.time == null) this.time = new Block(time);

        this.time.setValue(time);
    }

    public String getAlphaAsString() {
        return String.valueOf(alpha);
    }

    public Block getAlpha() {
        return alpha;
    }

    public void setAlpha(String alpha) {
        if (this.alpha == null) this.alpha = new Block(alpha);

        this.alpha.setValue(alpha);
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public String toJson() {
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        StringBuilder json = new StringBuilder();
        boolean started = false;

        json.append("{ ");
        json.append("\"type\" : \"" + getType() + "\"");
        if (!started) {
            started = true;
        } else
            json.append(", ");


        byte[] encodedRawName = encoder.quoteAsUTF8(toString());
        String outputRawName = new String(encodedRawName);
        if (!started) {
            started = true;
        } else
            json.append(", ");
        json.append("\"formatted\" : \"" + outputRawName + "\"");

        json.append(" }");
        return json.toString();
    }

    public String getRawTaggedValue() {
        return rawTaggedValue;
    }

    public void setRawTaggedValue(String rawTaggedValue) {
        this.rawTaggedValue = rawTaggedValue;
    }

    public enum Type {
        NUMBER,
        ALPHABETIC,
        EXPONENT,
        TIME,
        UNKNOWN
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ValueBlock that = (ValueBlock) o;

        return new EqualsBuilder()
                .append(rawValue, that.rawValue)
                .append(number, that.number)
                .append(base, that.base)
                .append(pow, that.pow)
                .append(exp, that.exp)
                .append(time, that.time)
                .append(alpha, that.alpha)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(rawValue)
                .append(number)
                .append(base)
                .append(pow)
                .append(exp)
                .append(time)
                .append(alpha)
                .toHashCode();
    }
}
