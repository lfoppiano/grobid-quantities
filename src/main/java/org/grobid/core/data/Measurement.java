package org.grobid.core.data;

import org.grobid.core.utilities.UnitUtilities;

import java.util.List;
import java.util.ArrayList;

import org.codehaus.jackson.io.JsonStringEncoder;

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
    	if (quantities == null)
    		quantities = new ArrayList<>();
        quantities.add(quantity);
    }

    public void setQuantities(List<Quantity> quantities) {
        this.quantities = quantities;
    }

    public List<Quantity> getQuantities() {
        return quantities;
    }

    public void setAtomicQuantity(Quantity quantity) {
    	if (quantities == null)
    		quantities = new ArrayList<Quantity>();
        quantities.add(quantity);
    }

    public Quantity getQuantityAtomic() {
    	if (quantities.size() != 1)
    		return null;
    	return quantities.get(0);
    }

    public void setQuantityLeast(Quantity quantity) {
    	if (quantities == null)
    		quantities = new ArrayList<Quantity>();
        if (quantities.size() == 0) {
            quantities.add(quantity);
        } else if (quantities.size() >= 1) {
            quantities.set(0, quantity);
        }
    }

    public Quantity getQuantityLeast() {
    	if (quantities.size() == 0)
    		return null;
    	return quantities.get(0);
    }

    public void setQuantityMost(Quantity quantity) {
    	if (quantities == null)
    		quantities = new ArrayList<Quantity>();
        if (quantities.size() == 0) {
            quantities.add(null);
            quantities.add(quantity);
        } else if (quantities.size() == 1) {
            quantities.add(quantity);
        } else {
            quantities.set(1, quantity);
        }
    }

    public Quantity getQuantityMost() {
    	if (quantities.size() != 2)
    		return null;
    	return quantities.get(1);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type.getName()).append(": ");
        if (isNotEmpty(quantities)) {
	        for (Quantity quantity : quantities) {
	            if (quantity != null) {
	                builder.append(quantity.toString());
	            }
	        }
	    }

        return builder.toString();
    }

    public String toJson() {
        JsonStringEncoder encoder = JsonStringEncoder.getInstance();
        StringBuilder json = new StringBuilder();
        boolean started = false;
        json.append("{ ");
        if (type != null) {
            byte[] encodedName = encoder.quoteAsUTF8(type.getName());
            String outputName = new String(encodedName);
            json.append("\"type\" : \"" + outputName + "\"");
            started = true;
        }

        if (type == UnitUtilities.Measurement_Type.VALUE) {
        	Quantity quantity = getQuantityAtomic();
        	if (quantity != null) {
        		if (!started) {
                    started = true;
                }
                else
                	json.append(", ");
                json.append("\"quantity\" : " + quantity.toJson());
        	}
        }
        else if (type == UnitUtilities.Measurement_Type.INTERVAL) {
        	Quantity quantityLeast = getQuantityLeast();
        	Quantity quantityMost = getQuantityMost();
        	if (quantityLeast != null) {
        		if (!started) {
                    started = true;
                }
                else
                	json.append(", ");
                json.append("\"quantityLeast\" : " + quantityLeast.toJson());
        	}
        	if (quantityMost != null) {
        		if (!started) {
                    started = true;
                }
                else
                	json.append(", ");
                json.append("\"quantityMost\" : " + quantityMost.toJson());
        	}
        }
        else if (type == UnitUtilities.Measurement_Type.CONJUNCTION) {
        	if ( (quantities != null) && (quantities.size() > 0) ) {
        		if (!started) {
                    started = true;
                    json.append("[ ");
                }
                else
                	json.append(", [ ");
                boolean started2 = false;
	        	for(Quantity quantity :  quantities) {
	        		if (quantity != null) {
		        		if (!started2) {
		                    started2 = true;
		                }
		                else
		                	json.append(", ");
		                json.append(quantity.toJson());
		        	}
	        	}
	        	json.append(" ]");
	        }
        }

        json.append(" }");
        return json.toString();
    }
}