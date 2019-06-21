package org.grobid.core.engines.training;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.converters.DOMConverter;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.utilities.UnitUtilities;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.grobid.core.document.xml.XmlBuilderUtils.TEI_NS;
import static org.grobid.core.document.xml.XmlBuilderUtils.teiElement;

public class UnitTrainingFormatter {

    protected List<Element> trainingExtraction(List<Measurement> measurements) {
        List<Element> elements = new ArrayList<>();

        for (Measurement measurement : measurements) {
            if (measurement.getType() == UnitUtilities.Measurement_Type.VALUE) {
                Quantity quantity = measurement.getQuantityAtomic();
                if (quantity == null)
                    continue;

                elements.add(writeItem(quantity));
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
                Quantity quantityLeast = measurement.getQuantityLeast();
                Quantity quantityMost = measurement.getQuantityMost();

                if (quantityLeast != null)
                    elements.add(writeItem(quantityLeast));

                if (quantityMost != null)
                    elements.add(writeItem(quantityMost));

            } else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
                Quantity quantityBase = measurement.getQuantityBase();
                Quantity quantityRange = measurement.getQuantityRange();

                if ((quantityBase == null) || (quantityRange == null))
                    continue;

                elements.add(writeItem(quantityBase));
                elements.add(writeItem(quantityRange));

            } else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                List<Quantity> quantities = measurement.getQuantityList();
                for (Quantity quantity : quantities) {
                    if (quantity == null)
                        continue;

                    elements.add(writeItem(quantity));
                }
            }
        }

        return elements;

    }

    protected Element writeItem(Quantity quantity) {
        Unit parsedUnit = quantity.getParsedUnit();
        Builder parser = new Builder();

        Element unit = teiElement("unit");
        if (parsedUnit != null && isNotEmpty(parsedUnit.getProductBlocks())) {
            String content = "<unit xmlns=\"" + TEI_NS + "\">"
                    + parsedUnit.getProductBlocks().get(0).getRawTaggedValue() + "</unit>";

            DocumentBuilderFactory dbf = null;
            DocumentBuilder db = null;
            try {
                dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);

                db = dbf.newDocumentBuilder();

            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(content));

            try {
                Document doc = db.parse(is);

                final nu.xom.Document convert = DOMConverter.convert(doc);
                final Element rootElement = convert.getRootElement();
                Element element = new Element(rootElement);
//                    element.setNamespaceURI(TEI_NS);
                return element;

            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            //Not parsed element are leave there in the unit so if by mistake we run the files with it they won't be
            // parsed correctly and the process will fail. In this way we avoid forgetting about it.
            final Unit rawUnit = quantity.getRawUnit();
            if (rawUnit != null) {
                unit.appendChild(rawUnit.getRawName());
            }
        }

        return unit;
    }

}
