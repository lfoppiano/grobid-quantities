package org.grobid.core.data;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.google.common.base.MoreObjects;
import org.grobid.core.utilities.OffsetPosition;

import java.math.BigDecimal;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class Value {
    private String rawName = "";
    private OffsetPosition offsets = new OffsetPosition();
    private ValueBlock structure = null;
    private BigDecimal numeric = null;

    public Value(BigDecimal numeric) {
        this.numeric = numeric;
    }

    public Value() {
    }

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String rawName) {
        this.rawName = rawName;
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

    public OffsetPosition getOffsets() {
        return offsets;
    }

    public void setOffsets(OffsetPosition offsets) {
        this.offsets = offsets;
    }

    public ValueBlock getStructure() {
        return structure;
    }

    public void setStructure(ValueBlock structure) {
        this.structure = structure;
    }

    public BigDecimal getNumeric() {
        return numeric;
    }

    public void setNumeric(BigDecimal numeric) {
        this.numeric = numeric;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("rawName", rawName)
                .add("structure", structure)
                .add("numeric", numeric)
                .toString();
    }

    public String toJson() {
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        StringBuilder json = new StringBuilder();
        boolean started = false;
        json.append("{ ");
        if (isNotEmpty(rawName)) {
            byte[] encodedRawName = encoder.quoteAsUTF8(rawName);
            String outputRawName = new String(encodedRawName);
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"name\" : \"" + outputRawName + "\"");
        }

        if (getNumeric() != null) {
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"numeric\" : " + getNumeric());
        }

        if (getStructure() != null) {
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"structure\" : " + getStructure().toJson() + ", ");
            json.append("\"parsed\" : \"" + getStructure().toString() + "\"");
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
}
