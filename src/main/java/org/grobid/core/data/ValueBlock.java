package org.grobid.core.data;

import com.fasterxml.jackson.core.io.JsonStringEncoder;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * This class is responsible to hold the structured representation of a value expressed in
 * the form:
 * - number x base^pow
 * - alphanumeric number
 * - time expression (date, time)
 * - exponential formulas e^exp
 */
public class ValueBlock {
    private String number = "";
    private String base = "";
    private String pow = "";
    private String exp = "";
    private String time = "";
    private String alpha = "";

    public ValueBlock() {
    }

    public ValueBlock(String number, String base, String pow) {
        this.number = number;
        this.base = base;
        this.pow = pow;
    }

    public ValueBlock(String number, String exp) {
        this.number = number;
        this.exp = exp;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        if (base != null) {
            this.base = base;
        }
    }

    public Type getType() {
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

    }


    @Override
    public String toString() {
        switch (getType()) {
            case ALPHANUMERIC:
                return getAlpha();
            case TIME:
                return getTime();
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPow() {
        return pow;
    }

    public void setPow(String pow) {
        this.pow = pow;
    }

    public String toJson() {
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        StringBuilder json = new StringBuilder();
        boolean started = false;
        json.append("{ ");
        if (isNotEmpty(number)) {
            byte[] encodedRawName = encoder.quoteAsUTF8(number);
            String outputRawName = new String(encodedRawName);
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"number\" : \"" + outputRawName + "\"");
        }

        if (isNotEmpty(base)) {
            byte[] encodedRawName = encoder.quoteAsUTF8(base);
            String outputRawName = new String(encodedRawName);
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"base\" : \"" + outputRawName + "\"");
        }

        if (isNotEmpty(pow)) {
            byte[] encodedRawName = encoder.quoteAsUTF8(pow);
            String outputRawName = new String(encodedRawName);
            if (!started) {
                started = true;
            } else
                json.append(", ");

            json.append("\"pow\" : \"" + outputRawName + "\"");
        }

        if (isNotEmpty(exp)) {
            byte[] encodedRawName = encoder.quoteAsUTF8(exp);
            String outputRawName = new String(encodedRawName);
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"exp\" : \"" + outputRawName + "\"");
        }

        if (isNotEmpty(alpha)) {
            byte[] encodedRawName = encoder.quoteAsUTF8(alpha);
            String outputRawName = new String(encodedRawName);
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"alpha\" : \"" + outputRawName + "\"");
        }

        if (isNotEmpty(time)) {
            byte[] encodedRawName = encoder.quoteAsUTF8(time);
            String outputRawName = new String(encodedRawName);
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"time\" : \"" + outputRawName + "\"");
        }

        json.append(" }");
        return json.toString();
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAlpha() {
        return alpha;
    }

    public void setAlpha(String alpha) {
        this.alpha = alpha;
    }

    public enum Type {
        NUMBER,
        ALPHANUMERIC,
        EXPONENT,
        TIME,
        UNKNOWN
    }
}


