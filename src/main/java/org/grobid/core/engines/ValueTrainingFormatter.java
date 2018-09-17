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

public class ValueTrainingFormatter extends UnitTrainingFormatter {

    protected Element writeUnit(Quantity quantity) {
        String rawValue = quantity.getRawValue();

        Element value = teiElement("value");
        if (rawValue != null) {

            value.appendChild(teiElement("num", rawValue));
        }
        
        return value;
    }
}
