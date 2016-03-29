package org.grobid.core.data;

import java.util.List;

/**
 * Created by lfoppiano on 08.03.16.
 */
public class UnitBlock {
    private String prefix = "";
    private String base = "";
    private String pow = "";


    public UnitBlock(String prefix, String base, String pow) {
        setPrefix(prefix);
        setBase(base);
        setPow(pow);
    }

    public UnitBlock(String base) {
        setBase(base);
    }

    public UnitBlock() {

    }


    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        if (base != null) {
            this.base = base;
        }
    }

    public String getPow() {
        return pow;
    }

    public void setPow(String pow) {
        if (pow != null) {
            this.pow = pow;
        }
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPrefix());
        sb.append(getBase());
        if (getPow() != "") {
            sb.append("^");
            sb.append(getPow());
        }

        return sb.toString();
    }

    public static String asProduct(List<UnitBlock> unitBlockList) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (UnitBlock ub : unitBlockList) {
            if (!first) {
                sb.append("·");
            } else {
                first = false;
            }
            sb.append(ub.toString());
        }

        return sb.toString();
    }

    public static String asString(List<UnitBlock> unitBlockList) {
        StringBuilder numerator = new StringBuilder();
        StringBuilder denominator = new StringBuilder();
        boolean firstNumerator = true;
        boolean firstDenominator = true;
        boolean fraction = false;
        for (UnitBlock ub : unitBlockList) {
            if (!ub.getPow().equals("0")) {
                if (ub.getPow().contains("-")) {
                    fraction = true;
                    String den = ub.toString().replace("-", "");
                    if (den.endsWith("^1")) {
                        den = den.replace("^1", "");
                    }

                    if (!firstDenominator) {
                        denominator.append("·");
                    } else {
                        firstDenominator = false;
                    }
                    denominator.append(den);
                } else {
                    if (!firstNumerator) {
                        numerator.append("·");
                    } else {
                        firstNumerator = false;
                    }

                    numerator.append(ub.toString());
                }
            }
        }

        if (fraction) {
            return numerator.append("/").append(denominator.toString()).toString();
        } else {
            return numerator.toString();
        }
    }
}
