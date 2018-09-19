package org.grobid.core.engines;

import nu.xom.Element;
import org.grobid.core.data.*;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.grobid.core.document.xml.XmlBuilderUtils.teiElement;

public class ValueTrainingFormatter extends UnitTrainingFormatter {

    protected Element writeItem(Quantity quantity) {
        String rawValue = quantity.getRawValue();
        final Value parsedValue = quantity.getParsedValue();

        Element value = teiElement("value");
        if (parsedValue != null) {
            final ValueBlock parsedValueBlock = parsedValue.getStructure();
            if (parsedValueBlock != null) {
                if (isNotEmpty(parsedValueBlock.getNumber())) {
                    value.appendChild(teiElement("number", parsedValueBlock.getNumber()));
                }
                if (isNotEmpty(parsedValueBlock.getBase())) {
                    value.appendChild(teiElement("base", parsedValueBlock.getBase()));
                }
                if (isNotEmpty(parsedValueBlock.getPow())) {
                    value.appendChild(teiElement("pow", parsedValueBlock.getPow()));
                }
            }
        } else if (rawValue != null) {
            value.appendChild(teiElement("val", rawValue));
        }

        return value;
    }
}
