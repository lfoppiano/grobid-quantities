package org.grobid.core.data;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import org.grobid.core.utilities.OffsetPosition;

/**
 * Class for representing the quantified object/substance. For  given measurement, the
 * quantified object/substance is the entity which is measured.
 * The quantified object is described with a raw string - the surface form of its mention
 * in the text, a normalized form and optionally an entity object resulting from the
 * disambiguation against a knowledge base - usually Wikipedia/Wikidata.
 *
 * @author Patrice Lopez
 */
public class QuantifiedObject {
    private String id = null;
    private String rawName = null;
    private String normalizedName = null;
    private OffsetPosition offsets = null;
    //private NerdEntity = null;

    public QuantifiedObject() {
    }

    public QuantifiedObject(String rawName) {
        this.rawName = rawName;
    }

    public QuantifiedObject(String rawName, String normalizedName) {
        this(rawName);
        this.normalizedName = normalizedName;
    }

    public QuantifiedObject(String rawName, String normalisedName, String id) {
        this(rawName, normalisedName);
        this.id = id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}