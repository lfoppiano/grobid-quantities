package org.grobid.core.data;

import org.grobid.core.utilities.OffsetPosition;

import org.codehaus.jackson.io.JsonStringEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for managing normalized Unit representation.
 *
 * @author Patrice Lopez
 */
public class Unit {
    private String rawName = null;
    private OffsetPosition offsets = null;
    private UnitDefinition unitDefinition = null;

    private List<UnitBlock> productBlock = null;

    public Unit() {
    }

    public Unit(String rawName) {
        this.rawName = rawName;
    }

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String name) {
        rawName = name;
    }

    public void setOffsetStart(int start) {
        if (offsets == null)
            offsets = new OffsetPosition();
        offsets.start = start;
    }

    public int getOffsetStart() {
        if (offsets != null)
            return offsets.start;
        else
            return -1;
    }

    public void setOffsetEnd(int end) {
        if (offsets == null)
            offsets = new OffsetPosition();
        offsets.end = end;
    }

    public int getOffsetEnd() {
        if (offsets != null)
            return offsets.end;
        else
            return -1;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ ");
        if (rawName != null) {
            builder.append(rawName).append("\t");
        }
        if (offsets != null) {
            builder.append(offsets.toString());
        }
        if (unitDefinition != null)
            builder.append(unitDefinition.toString()).append("\t");

        builder.append(" ]");
        return builder.toString();
    }

    public String toJson() {
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        StringBuilder json = new StringBuilder();
        boolean started = false;
        json.append("{ ");
        if (rawName != null) {
            byte[] encodedRawName = encoder.quoteAsUTF8(rawName);
            String outputRawName = new String(encodedRawName);
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"rawName\" : \"" + outputRawName + "\"");
        }
        if (getUnitDefinition() != null) {
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append(getUnitDefinition().toJson());
        }

        if (offsets != null) {
            if (getOffsetStart() != -1) {
                if (!started) {
                    started = true;
                } else
                    json.append(", ");
                json.append("\"offsetStart\" : " + getOffsetStart());
            }
            if (getOffsetEnd() != -1) {
                if (!started) {
                    started = true;
                } else
                    json.append(", ");
                json.append("\"offsetEnd\" : " + getOffsetEnd());
            }
        }

        json.append(" }");
        return json.toString();
    }

    public UnitDefinition getUnitDefinition() {
        return unitDefinition;
    }

    public void setUnitDefinition(UnitDefinition unitDefinition) {
        this.unitDefinition = unitDefinition;
    }

    public boolean addProductBlock(UnitBlock block) {
        return getProductBlocks().add(block);
    }

    public List<UnitBlock> getProductBlocks() {
        if (productBlock == null) {
            productBlock = new ArrayList<>();
        }
        return productBlock;
    }

    public void setProductBlocks(List<UnitBlock> productForm) {
        this.productBlock = productForm;
    }


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

            sb.append("[");
            sb.append(getPrefix());
            sb.append(getBase());
            sb.append("^");
            if (getPow() == "") {
                sb.append("1");
            } else {
                sb.append(getPow());
            }
            sb.append("]");

            return sb.toString();
        }
    }

}