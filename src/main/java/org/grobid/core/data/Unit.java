package org.grobid.core.data;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.UnitUtilities;

import java.util.List;
import java.util.ArrayList;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import org.codehaus.jackson.io.JsonStringEncoder;

/**
 * Class for managing normalized Unit representation.
 *
 * @author Patrice Lopez
 */
public class Unit {
    // usual full names for the unit, e.g. metre, meter
    private List<String> names = null;

    // standard notation, e.g. g for gram - there might be several notations for an unit
    private List<String> notations = null;

    private UnitUtilities.Unit_Type type;               // type of measurement
    private UnitUtilities.System_Type system;           // type of system of unit

    // to be used only when building a unit during parsing
    private String rawName = null;
    private OffsetPosition offsets = null;

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> name) {
        this.names = names;
    }

    public void addName(String name) {
        if (names == null)
            names = new ArrayList<String>();
        names.add(name);
    }

    public List<String> getNotations() {
        return notations;
    }

    public void setNotations(List<String> not) {
        this.notations = not;
    }

    public void addNotation(String not) {
        if (notations == null)
            notations = new ArrayList<String>();
        notations.add(not);
    }

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String name) {
        rawName = name;
    }

    public UnitUtilities.System_Type getSystem() {
        return system;
    }

    public void setSystem(UnitUtilities.System_Type si) {
        system = si;
    }

    public UnitUtilities.Unit_Type getType() {
        return type;
    }

    public void setType(UnitUtilities.Unit_Type ty) {
        type = ty;
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
        if (notations != null)
            builder.append(notations.toString()).append("\t");
        if (type != null)
            builder.append(type.getName()).append("\t");
        if (system != null)
            builder.append(system.getName()).append("\t");
        if (names != null)
            builder.append(names.toString()).append("\t");
        if (rawName != null)
            builder.append(rawName).append("\t");
        if (offsets != null)
            builder.append(offsets.toString());
        builder.append(" ]");

        return builder.toString();
    }

    public String toJson() {
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        StringBuilder json = new StringBuilder();
        boolean started = false;
        json.append("{ ");
        if (isNotEmpty(notations)) {
            String notation = notations.get(0);
            byte[] encodedNotation = encoder.quoteAsUTF8(notation);
            String outputNotation = new String(encodedNotation);
            json.append("\"notation\" : \"" + outputNotation + "\"");
            started = true;
        }
        if (type != null) {
            byte[] encodedName = encoder.quoteAsUTF8(type.getName());
            String outputName = new String(encodedName);
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"name\" : \"" + outputName + "\"");
        }
        if (system != null) {
            byte[] encodedSystem = encoder.quoteAsUTF8(system.getName());
            String outputSystem = new String(encodedSystem);
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"system\" : \"" + outputSystem + "\"");
        }
        if (rawName != null) {
            byte[] encodedRawName = encoder.quoteAsUTF8(rawName);
            String outputRawName = new String(encodedRawName);
            if (!started) {
                started = true;
            } else
                json.append(", ");
            json.append("\"rawName\" : \"" + outputRawName + "\"");
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
                json.append("\"offsetEnd\" : " + getOffsetStart());
            }
        }

        json.append(" }");
        return json.toString();
    }

}