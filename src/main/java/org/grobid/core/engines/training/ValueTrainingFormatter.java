package org.grobid.core.engines.training;

import nu.xom.Element;
import nu.xom.converters.DOMConverter;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Value;
import org.grobid.core.data.ValueBlock;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

import static org.grobid.core.document.xml.XmlBuilderUtils.TEI_NS;
import static org.grobid.core.document.xml.XmlBuilderUtils.teiElement;

public class ValueTrainingFormatter extends UnitTrainingFormatter {

    protected Element writeItem(Quantity quantity) {
        String rawValue = quantity.getRawValue();
        final Value parsedValue = quantity.getParsedValue();

        Element value = teiElement("value");
        if (parsedValue != null) {
            final ValueBlock parsedValueBlock = parsedValue.getStructure();
            if (parsedValueBlock != null) {
                String rawTaggedValue = parsedValueBlock.getRawTaggedValue();
                String content = "<value xmlns=\"" + TEI_NS + "\">" + rawTaggedValue + "</value>";

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

            } else if (rawValue != null) {
                value.appendChild(teiElement("number", rawValue));
            }
        } else if (rawValue != null) {
            value.appendChild(rawValue);
        }

        return value;
    }
}
