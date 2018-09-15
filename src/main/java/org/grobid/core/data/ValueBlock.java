package org.grobid.core.data;

import java.util.List;

/**
 * Created by lfoppiano on 08.03.16.
 */
public class ValueBlock {
    private String value = "";
    private String operation = "";
    private String base = "";
    private String pow = "";


    public ValueBlock(String value, String operation, String base, String pow) {
        setValue(value);
        setOperation(operation);
        setBase(base);
        setPow(pow);
    }

    public ValueBlock(String value) {
        setBase(value);
    }

    public ValueBlock() {

    }


    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        if (base != null) {
            this.base = base;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getValue());
        if(!getOperation().equals("")){
            sb.append("x");
        }

        if (!getBase().equals("") && !getPow().equals("")) {
            sb.append(getBase());
            sb.append("^");
            sb.append(getPow());
        } else {
            System.out.println("something wrong with value, giving base: "+getBase()+ " and pow: " + getPow());
        }

        return sb.toString();
    }

    public static String asProduct(List<ValueBlock> unitBlockList) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (ValueBlock ub : unitBlockList) {
            if (!first) {
                sb.append("·");
            } else {
                first = false;
            }
            sb.append(ub.toString());
        }

        return sb.toString();
    }

    public static String asString(List<ValueBlock> unitBlockList) {
        StringBuilder numerator = new StringBuilder();
        StringBuilder denominator = new StringBuilder();
        boolean firstNumerator = true;
        boolean firstDenominator = true;
        boolean fraction = false;
        for (ValueBlock ub : unitBlockList) {
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getPow() {
        return pow;
    }

    public void setPow(String pow) {
        this.pow = pow;
    }
}
