package org.grobid.core.data;

import org.grobid.core.utilities.UnitUtilities;

import java.util.List;
import java.util.ArrayList;

/**
 * Class for managing a measurement representation. A mesurement is the high level representation 
 * of the expression of a physical measure. A measurement can be an atomic quantity, an interval of 
 * quantities or a list (conjunctive or disjunctive) of quantities or intervals. 
 * 
 * The parser will return a list of measurements as result of the text processing. 
 *
 * @author Patrice Lopez
 */
public class Measurement {

	private UnitUtilities.Measurement_Type type = null;
	private List<Quantity> quantities = null;

    public Measurement(UnitUtilities.Measurement_Type type) {
        this.type = type;
    }

	public UnitUtilities.Measurement_Type getType() {
		return type;
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
}