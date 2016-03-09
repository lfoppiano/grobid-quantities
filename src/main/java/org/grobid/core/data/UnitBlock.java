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
        this.prefix = prefix;
        this.base = base;
        this.pow = pow;
    }

    public UnitBlock() {

    }


    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getPow() {
        return pow;
    }

    public void setPow(String pow) {
        this.pow = pow;
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

    public static String unitBlocksToString(List<UnitBlock> unitBlockList) {
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
}
