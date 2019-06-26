package org.grobid.core.data;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Block block = (Block) o;

        return new EqualsBuilder()
                .append(value, block.value)
                .append(offsets, block.offsets)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .append(offsets)
                .toHashCode();
    }
}
