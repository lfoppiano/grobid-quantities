package org.grobid.core.engines;

import nu.xom.Element;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.UnitBlock;
import org.grobid.core.utilities.UnitUtilities;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.grobid.core.document.xml.XmlBuilderUtils.teiElement;

public class UnitTrainingFormatter {

    protected Element trainingExtraction(List<Measurement> measurements, Element root) {
        for (Measurement measurement : measurements) {
            if (measurement.getType() == UnitUtilities.Measurement_Type.VALUE) {
                Quantity quantity = measurement.getQuantityAtomic();
                if (quantity == null)
                    continue;

                root.appendChild(writeUnit(quantity));
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
                Quantity quantityLeast = measurement.getQuantityLeast();
                Quantity quantityMost = measurement.getQuantityMost();

                if (quantityLeast != null)
                    root.appendChild(writeUnit(quantityLeast));

                if (quantityMost != null)
                    root.appendChild(writeUnit(quantityMost));

            } else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
                Quantity quantityBase = measurement.getQuantityBase();
                Quantity quantityRange = measurement.getQuantityRange();

                if ((quantityBase == null) || (quantityRange == null))
                    continue;

                root.appendChild(writeUnit(quantityBase));
                root.appendChild(writeUnit(quantityRange));

            } else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                List<Quantity> quantities = measurement.getQuantityList();
                for (Quantity quantity : quantities) {
                    if(quantity == null)
                        continue;

                    root.appendChild(writeUnit(quantity));
                }
            }
        }

        return root;

    }

    protected Element writeUnit(Quantity quantity) {
        Unit parsedUnit = quantity.getParsedUnit();

        Element unit = teiElement("unit");
        if (parsedUnit != null) {
            List<UnitBlock> rawUnitBlocks = parsedUnit.getProductBlocks();
            rawUnitBlocks.forEach(rwb -> {
                if(isNotEmpty(rwb.getPrefix())) {
                    unit.appendChild(teiElement("prefix", rwb.getPrefix()));
                }

                if(isNotEmpty(rwb.getBase())) {
                    unit.appendChild(teiElement("base", rwb.getBase()));
                }

                if(isNotEmpty(rwb.getPow())) {
                    unit.appendChild(teiElement("base", rwb.getPow()));
                }
            });

        } else {
            //Not parsed element are leave there in the unit so they won't be parsed correctly and we
            // avoid forgetting about it.
            final Unit rawUnit = quantity.getRawUnit();
            if(rawUnit != null)
                unit.appendChild(rawUnit.getRawName()); 
        }

        return unit;
    }
}
