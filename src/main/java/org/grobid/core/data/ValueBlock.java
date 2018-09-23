package org.grobid.core.data;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import org.grobid.core.utilities.OffsetPosition;

/**
 * This class is responsible to hold the structured representation of a value expressed in
 * the form:
 * - number x base^pow
 * - alphanumeric number
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
            return Type.NUMBER;
        } else if (alpha != null) {
            return Type.ALPHANUMERIC;
        } else {
            if (base != null && pow != null) {
                return Type.NUMBER;
            } else if (exp != null) {
                return Type.EXPONENT;
            }
        }

        return Type.UNKNOWN;

    }

    /*public Type getType() {
        if (isNotEmpty(number)) {
            if (isNotEmpty(base) && isNotEmpty(pow)) {
                return Type.NUMBER;
            } else if (isNotEmpty(exp)) {
                return Type.EXPONENT;
            }
            return Type.NUMBER;
        } else if (isNotEmpty(time)) {
            return Type.NUMBER;
        } else if (isNotEmpty(alpha)) {
            return Type.ALPHANUMERIC;
        } else {
            if (isNotEmpty(base) && isNotEmpty(pow)) {
                return Type.NUMBER;
            } else if (isNotEmpty(exp)) {
                return Type.EXPONENT;
            }
        }

        return Type.UNKNOWN;

    }*/


    @Override
    public String toString() {
        switch (getType()) {
            case ALPHANUMERIC:
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
                }
                return sb.toString();
            case UNKNOWN:
                break;

        }
        return null;
    }

    public String getNumberAsString() {
        return String.valueOf(number);
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
//        if (isNotEmpty(number)) {
//            byte[] encodedRawName = encoder.quoteAsUTF8(number);
//            String outputRawName = new String(encodedRawName);
//            if (!started) {
//                started = true;
//            } else
//                json.append(", ");
//            json.append("\"number\" : \"" + outputRawName + "\"");
//        }
//
//        if (isNotEmpty(base)) {
//            byte[] encodedRawName = encoder.quoteAsUTF8(base);
//            String outputRawName = new String(encodedRawName);
//            if (!started) {
//                started = true;
//            } else
//                json.append(", ");
//            json.append("\"base\" : \"" + outputRawName + "\"");
//        }
//
//        if (isNotEmpty(pow)) {
//            byte[] encodedRawName = encoder.quoteAsUTF8(pow);
//            String outputRawName = new String(encodedRawName);
//            if (!started) {
//                started = true;
//            } else
//                json.append(", ");
//
//            json.append("\"pow\" : \"" + outputRawName + "\"");
//        }
//
//        if (isNotEmpty(exp)) {
//            byte[] encodedRawName = encoder.quoteAsUTF8(exp);
//            String outputRawName = new String(encodedRawName);
//            if (!started) {
//                started = true;
//            } else
//                json.append(", ");
//            json.append("\"exp\" : \"" + outputRawName + "\"");
//        }
//
//        if (isNotEmpty(alpha)) {
//            byte[] encodedRawName = encoder.quoteAsUTF8(alpha);
//            String outputRawName = new String(encodedRawName);
//            if (!started) {
//                started = true;
//            } else
//                json.append(", ");
//            json.append("\"alpha\" : \"" + outputRawName + "\"");
//        }
//
//        if (isNotEmpty(time)) {
//            byte[] encodedRawName = encoder.quoteAsUTF8(time);
//            String outputRawName = new String(encodedRawName);
//            if (!started) {
//                started = true;
//            } else
//                json.append(", ");
//            json.append("\"time\" : \"" + outputRawName + "\"");
//        }

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
        ALPHANUMERIC,
        EXPONENT,
        TIME,
        UNKNOWN
    }
}

