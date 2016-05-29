package org.grobid.core.data;

import org.grobid.core.utilities.UnitUtilities;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.core.io.*;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * Class for managing a measurement representation. A mesurement is the high level representation
 * of the expression of a physical measure. A measurement can be an atomic quantity, an interval of
 * quantities or a list (conjunctive or disjunctive) of quantities or intervals.
 * <p>
 * The parser will return a list of measurements as result of the text processing.
 *
 * @author Patrice Lopez
 */
public class Measurement {
    private UnitUtilities.Measurement_Type type = null;
    private Quantity quantityAtomic = null;
    private Quantity quantityLeast = null;
    private Quantity quantityMost = null;
    private Quantity quantityBase = null;
    private Quantity quantityRange = null;
    private List<Quantity> quantityList = null;
    private QuantifiedObject substance = null; // what is quantified, as extracted from the text

    public Measurement() {
    }

    public Measurement(UnitUtilities.Measurement_Type type) {
        this.type = type;
    }

    public UnitUtilities.Measurement_Type getType() {
        return type;
    }

    public void setType(UnitUtilities.Measurement_Type type) {
        this.type = type;
    }

    public void addQuantityList(Quantity quantity) {
        if (quantityList == null)
            quantityList = new ArrayList<>();
        quantityList.add(quantity);
    }

    public void setQuantityList(List<Quantity> quantities) {
        this.quantityList = quantityList;
    }

    public List<Quantity> getQuantityList() {
        return quantityList;
    }

    public void setAtomicQuantity(Quantity quantity) {
        quantityAtomic = quantity;
    }

    public Quantity getQuantityAtomic() {
        return quantityAtomic;
    }

    public void setQuantityLeast(Quantity quantity) {
        quantityLeast = quantity;
    }

    public Quantity getQuantityLeast() {
        return quantityLeast;
    }

    public void setQuantityMost(Quantity quantity) {
        quantityMost = quantity;
    }

    public Quantity getQuantityMost() {
        return quantityMost;
    }

    public void setQuantityBase(Quantity quantity) {
        quantityBase = quantity;
    }

    public Quantity getQuantityBase() {
        return quantityBase;
    }

    public void setQuantityRange(Quantity quantity) {
        quantityRange = quantity;
    }

    public Quantity getQuantityRange() {
        return quantityRange;
    }

    public void setQuantifiedObject(QuantifiedObject substance) {
        this.substance = substance;
    }

    public QuantifiedObject getQuantifiedObject() {
        return substance;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type.getName()).append(": ");
        if (quantityAtomic != null)
            builder.append("atomic quantity: " + quantityAtomic.toString());
        if (quantityLeast != null)
            builder.append("quantity least: " + quantityLeast.toString());
        if (quantityMost != null)
            builder.append("quantity most: " + quantityMost.toString());
        if (quantityBase != null)
            builder.append("base quantity: " + quantityBase.toString());
        if (quantityRange != null)
            builder.append("range quantity: " + quantityRange.toString());

        if (quantityList != null) {
            for (Quantity quantity : quantityList) {
                builder.append("quantity list: ");
                if (quantity != null) {
                    builder.append(quantity.toString());
                }
            }
        }

        if (substance != null) {
            builder.append(", quantified : " + substance.toString());
        }

        return builder.toString();
    }

    public String toJson() {
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        StringBuilder json = new StringBuilder();
        boolean started = false;
        json.append("{ ");
        byte[] encodedName = null;
        if (type != null) {
            if ((type == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) ||
                    (type == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE))
                encodedName = encoder.quoteAsUTF8("interval");
            else
                encodedName = encoder.quoteAsUTF8(type.getName());
            String outputName = new String(encodedName);
            json.append("\"type\" : \"" + outputName + "\"");
            started = true;
        }

        if (type == UnitUtilities.Measurement_Type.VALUE) {
            Quantity quantity = getQuantityAtomic();
            if (quantity != null) {
                if (!started) {
                    started = true;
                } else
                    json.append(", ");
                json.append("\"quantity\" : " + quantity.toJson());
            }
        } else if (type == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
            Quantity quantityLeast = getQuantityLeast();
            Quantity quantityMost = getQuantityMost();
            if (quantityLeast != null) {
                if (!started) {
                    started = true;
                } else
                    json.append(", ");
                json.append("\"quantityLeast\" : " + quantityLeast.toJson());
            }
            if (quantityMost != null) {
                if (!started) {
                    started = true;
                } else
                    json.append(", ");
                json.append("\"quantityMost\" : " + quantityMost.toJson());
            }
        } else if (type == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
            Quantity quantityBase = getQuantityBase();
            Quantity quantityRange = getQuantityRange();
            Quantity quantityLeast = getQuantityLeast();
            Quantity quantityMost = getQuantityMost();
            if (quantityBase != null) {
                if (!started) {
                    started = true;
                } else
                    json.append(", ");
                json.append("\"quantityBase\" : " + quantityBase.toJson());
            }
            if (quantityRange != null) {
                if (!started) {
                    started = true;
                } else
                    json.append(", ");
                json.append("\"quantityRange\" : " + quantityRange.toJson());
            }
            if (quantityLeast != null) {
                if (!started) {
                    started = true;
                } else
                    json.append(", ");
                json.append("\"quantityLeast\" : " + quantityLeast.toJson());
            }
            if (quantityMost != null) {
                if (!started) {
                    started = true;
                } else
                    json.append(", ");
                json.append("\"quantityMost\" : " + quantityMost.toJson());
            }
        } else if (type == UnitUtilities.Measurement_Type.CONJUNCTION) {
            if ((quantityList != null) && (quantityList.size() > 0)) {
                if (!started) {
                    started = true;
                    json.append("[ ");
                } else {
                    json.append(", \"quantities\": [ ");
                }

                boolean started2 = false;
                for (Quantity quantity : quantityList) {
                    if (quantity != null) {
                        if (!started2) {
                            started2 = true;
                        } else
                            json.append(", ");
                        json.append(quantity.toJson());
                    }
                }
                json.append(" ]");
            }
        }

        if (substance != null) {
            json.append(", \"quantified\" : " + substance.toJson());
        }

        json.append(" }");
        return json.toString();
    }
}