package org.grobid.core.data;

import org.grobid.core.utilities.OffsetPosition;

public class Value {
    private String rawName = "";
    private OffsetPosition offsets = new OffsetPosition();
    private ValueBlock parsedValue = new ValueBlock();

    public Value() {
        
    }

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String rawName) {
        this.rawName = rawName;
    }

    public OffsetPosition getOffsets() {
        return offsets;
    }

    public void setOffsets(OffsetPosition offsets) {
        this.offsets = offsets;
    }

    public ValueBlock getParsedValue() {
        return parsedValue;
    }

    public void setParsedValue(ValueBlock parsedValue) {
        this.parsedValue = parsedValue;
    }
}
