package org.grobid.core.data;

import org.grobid.core.utilities.UnitUtilities;

import java.util.List;
import java.util.ArrayList;

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
    private List<Quantity> quantities = null;

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

    public void addQuantity(Quantity quantity) {
        if (quantities == null) {
            quantities = new ArrayList<Quantity>();
        }
        quantities.add(quantity);
    }

    public void setQuantities(List<Quantity> quantities) {
        this.quantities = quantities;
    }

    public List<Quantity> getQuantities() {
        return quantities;
    }

    public void setAtomicQuantity(Quantity quantity) {
        quantities = new ArrayList<Quantity>();
        quantities.add(quantity);
    }

    public void setQuantityLeast(Quantity quantity) {
        if (quantities == null) {
            quantities = new ArrayList<Quantity>();
        }
        if (quantities.size() == 0)
            quantities.add(quantity);
        else if (quantities.size() >= 1)
            quantities.set(0, quantity);
    }

    public void setQuantityMost(Quantity quantity) {
        if (quantities == null) {
            quantities = new ArrayList<Quantity>();
        }
        if (quantities.size() == 0) {
            quantities.add(null);
            quantities.add(quantity);
        } else if (quantities.size() == 1)
            quantities.add(quantity);
        else
            quantities.set(1, quantity);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(type.getName()).append(": ");
        if ((quantities != null) && (quantities.size() > 0)) {
            for (Quantity quantity : quantities) {
                if (quantity != null)
                    builder.append(quantity.toString());
            }
        }

        return builder.toString();
    }
}