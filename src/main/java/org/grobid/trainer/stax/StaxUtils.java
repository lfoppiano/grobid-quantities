package org.grobid.trainer.stax;

import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Map;

public class StaxUtils {

    public static void traverse(XMLStreamReader2 reader, final StaxParserContentHandler contentHandler) throws XMLStreamException {
        traverse(reader, streamReader -> {
            XMLStreamReader2 reader1 = (XMLStreamReader2) streamReader;
            switch (reader1.getEventType()) {
                case XMLStreamReader2.START_DOCUMENT:
                    contentHandler.onStartDocument(reader1);
                    break;
                case XMLStreamReader2.START_ELEMENT:
                    contentHandler.onStartElement(reader1);
                    break;
                case XMLStreamReader2.END_ELEMENT:
                    contentHandler.onEndElement(reader1);
                    break;
                case XMLStreamConstants.CHARACTERS:
                    contentHandler.onCharacter(reader1);
                    break;
                case XMLStreamReader2.END_DOCUMENT:
                    contentHandler.onEndDocument(reader1);
                    break;
            }
        });
    }

    public static void traverse(XMLStreamReader2 streamReader, StaxClosure closure) throws XMLStreamException {
        closure.process(streamReader);
        while (streamReader.hasNext()) {
            streamReader.next();
            closure.process(streamReader);
        }
    }

    public static void traverse(XMLStreamReader streamReader, String[] tags, StaxClosure closure) throws XMLStreamException {
        while (streamReader.hasNext()) {
            streamReader.next();
            if (XMLStreamReader.START_ELEMENT == streamReader.getEventType()) {
                String localPart = streamReader.getName().getLocalPart();
                for (String tag : tags) {
                    if (tag.equals(localPart)) {
                        closure.process(streamReader);
                    }
                }
            }
        }
    }

    public static Map<String, String> parse(XMLStreamReader streamReader, EndCondition endCondition, String... tags) throws XMLStreamException {
        Map<String, String> mapping = new HashMap<String, String>();
        while (streamReader.hasNext() && !endCondition.mustExit(streamReader)) {
            streamReader.next();
            if (XMLStreamReader.START_ELEMENT == streamReader.getEventType()) {
                String localPart = streamReader.getName().getLocalPart();
                for (String tag : tags) {
                    if (tag.equals(localPart)) {
                        streamReader.next();
                        if (XMLStreamReader.CHARACTERS == streamReader.getEventType()) {
                            mapping.put(tag, streamReader.getText());
                        }
                    }
                }
            }
        }
        return mapping;
    }

    public static String getAttributeByLocalName(XMLStreamReader reader, String localName) {
        String result = "";
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            QName attribute = reader.getAttributeName(i);
            if (attribute != null && attribute.getLocalPart().equals(localName)) {
                result = reader.getAttributeValue(i);
            }
        }
        return result;
    }


    public interface StaxClosure {
        void process(XMLStreamReader streamReader) throws XMLStreamException;
    }

    public interface EndCondition {
        boolean mustExit(XMLStreamReader streamReader) throws XMLStreamException;
    }

    public static class ReachedClosingTagCondition implements EndCondition {

        private String endTagName;

        public ReachedClosingTagCondition(String tagName) {
            this.endTagName = tagName;
        }

        @Override
        public boolean mustExit(XMLStreamReader streamReader) throws XMLStreamException {
            return XMLStreamReader.END_ELEMENT == streamReader.getEventType()
                    && endTagName.equals(streamReader.getName().getLocalPart());
        }
    }


}
