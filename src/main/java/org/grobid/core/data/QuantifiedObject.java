package org.grobid.core.data;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import org.grobid.core.utilities.OffsetPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing the quantitfied object/substance.
 *
 * @author Patrice Lopez
 */
public class QuantifiedObject {
    private String rawName = null;
    private String normalizedName = null;
    private OffsetPosition offsets = null;

    public QuantifiedObject() {
    }

    public QuantifiedObject(String rawName) {
        this.rawName = rawName;
    }

    public QuantifiedObject(String rawName, String normalizedName) {
        this.rawName = rawName;
        this.normalizedName = normalizedName;
    }

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String name) {
        rawName = name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String name) {
        normalizedName = name;
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
        final StringBuilder sb = new StringBuilder("QuantifiedObject{");
        sb.append("rawName='").append(rawName).append('\'');
        sb.append(", normalizedName='").append(normalizedName).append('\'');
        sb.append(", offsets=").append(offsets);
        sb.append('}');
        return sb.toString();
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
        if (normalizedName != null) {
            byte[] encodedNormalizedName = encoder.quoteAsUTF8(normalizedName);
            String outputNormalizedName = new String(encodedNormalizedName);
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"normalizedName\" : \"" + outputNormalizedName + "\"");
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