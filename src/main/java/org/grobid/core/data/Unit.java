package org.grobid.core.data;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import org.grobid.core.utilities.OffsetPosition;

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
    private boolean hasRightAttachment = false;

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Unit{");
        sb.append("rawName='").append(rawName).append('\'');
        sb.append(", offsets=").append(offsets);
        sb.append(", productBlock=").append(productBlock);
        sb.append('}');
        return sb.toString();
    }

    /*    public String toString() {
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
    }*/

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
            json.append("\"name\" : \"" + outputRawName + "\"");
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

    public boolean hasDefinition() {
        return unitDefinition != null;
    }


    public boolean hasUnitRightAttachment() {
        return hasRightAttachment;
    }

    public void setUnitRightAttachment(boolean hasRightAttachment) {
        this.hasRightAttachment = hasRightAttachment;
    }
}