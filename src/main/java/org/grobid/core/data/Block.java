package org.grobid.core.data;

import org.grobid.core.utilities.OffsetPosition;

public class Block {
    private String value = "";
    private OffsetPosition offsets = new OffsetPosition();

    public Block(String value, OffsetPosition offsets) {
        this.value = value;
        this.offsets = offsets;
    }

    public Block(String value) {
        this.value = value;
    }

    public OffsetPosition getOffsets() {
        return offsets;
    }

    public void setOffsets(OffsetPosition offsets) {
        this.offsets = offsets;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
