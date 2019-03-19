package org.grobid.trainer.stax;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.QuantifiedObject;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.trainer.MeasureLabeled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;

public class QuantifiedObjectAnnotationStaxHandler implements StaxParserContentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuantifiedObjectAnnotationStaxHandler.class);

    private StringBuilder accumulator;
    public static final String MEASURE_NAME = "measure";
    public static final String QUANTIFIED_OBJECT_NAME = "quantifiedObject";

    private List<MeasureLabeled> measuresWithoutId = new ArrayList<>();

    private boolean insideMeasure = false;
    private boolean insideQuantifiedObject = false;
    private boolean checkForward = false;

    private List<Pair<String, String>> labeled = new ArrayList<>();

    private Map<String, Pair<QuantifiedObject, MeasureLabeled>> data;

    private QuantifiedObject currentQuantifiedObject = null;

    private MeasureLabeled currentMeasureLabeld = null;

    public QuantifiedObjectAnnotationStaxHandler() {
        this.data = new HashMap<>();
        this.accumulator = new StringBuilder();
    }

    @Override
    public void onStartDocument(XMLStreamReader2 reader) {
    }

    @Override
    public void onEndDocument(XMLStreamReader2 reader) {
        writeData();
    }

    @Override
    public void onStartElement(XMLStreamReader2 reader) {
        final String localName = reader.getName().getLocalPart();

        if (MEASURE_NAME.equals(localName)) {
            writeData();

            String pointer = getAttributeValue(reader, "ptr");
            currentMeasureLabeld = new MeasureLabeled();
            //removing #
            if (isNotBlank(pointer)) {
                String id = pointer.substring(1);
                currentMeasureLabeld.setId(id);
            }

            if(checkForward) {
                if(currentMeasureLabeld.getId() != null) {
                    if(currentQuantifiedObject != null
                            && !StringUtils.equals(currentMeasureLabeld.getId(), currentQuantifiedObject.getId())) {
                        throw new GrobidException("The training data is inconsistent and should be corrected. " +
                                "\n\tmeasureId: " + currentMeasureLabeld.getId() +
                                "\n\tquantifiedObjectId: " + currentQuantifiedObject.getId());
                    }
                }
                checkForward = false;
            }

            insideMeasure = true;

        } else if (QUANTIFIED_OBJECT_NAME.equals(localName)) {
            if(checkForward) {
                throw new GrobidException("The training data is inconsistent and should be corrected. " +
                        "\n\tquantifiedObjectId: " + currentQuantifiedObject.getId());
            }
            String id = getAttributeValue(reader, "id");
            writeData();

            currentQuantifiedObject = new QuantifiedObject();
            currentQuantifiedObject.setId(id);

            insideQuantifiedObject = true;
        }
    }

    @Override
    public void onEndElement(XMLStreamReader2 reader) {
        final String localName = reader.getName().getLocalPart();

        if (MEASURE_NAME.equals(localName)) {
            currentMeasureLabeld.setRawName(accumulator.toString());
            if (currentQuantifiedObject != null) {
                if (currentMeasureLabeld != null
                        && StringUtils.equals(currentMeasureLabeld.getId(), currentQuantifiedObject.getId())) {
                    // we find a match, the object was written already, so let's write the measurement
                    // and set them both to null

                    currentMeasureLabeld = null;
                    currentQuantifiedObject = null;
                } else {
                    throw new GrobidException("The training data is inconsistent and should be corrected. " +
                            "\n\tmeasureId: " + currentMeasureLabeld.getId() +
                            "\n\tquantifiedObjectId: " + currentQuantifiedObject.getId());
                }
            }

            if (currentMeasureLabeld != null && currentMeasureLabeld.getId() == null) {
                currentMeasureLabeld = null;
            }
            writeData(MEASURE_NAME);
            insideMeasure = false;

        } else if (QUANTIFIED_OBJECT_NAME.equals(localName)) {

            currentQuantifiedObject.setRawName(accumulator.toString());

            if (currentMeasureLabeld != null) {
                if (StringUtils.equals(currentQuantifiedObject.getId(), currentMeasureLabeld.getId())) {
                    writeData(QUANTIFIED_OBJECT_NAME + "_left");

                    // Reset
                    currentMeasureLabeld = null;
                    currentQuantifiedObject = null;
                } else {
                    // The quantified object doesn't have any references before so I cannot write it as such.
                    currentMeasureLabeld = null;
                    checkForward = true;
                    writeData(QUANTIFIED_OBJECT_NAME + "_right");
                }
            } else {
                writeData(QUANTIFIED_OBJECT_NAME + "_right");
            }

            insideQuantifiedObject = false;
        } else if("p".equals(localName)) {
            writeData();
            labeled.add(new ImmutablePair<>("\n", null));
        }
    }

    @Override
    public void onCharacter(XMLStreamReader2 reader) {
        String text = reader.getText();
        accumulator.append(text);
    }

    private String getText(XMLStreamReader2 reader) {
        String text = reader.getText();
        text = trim(text);
        return text;
    }

    private String getAttributeValue(XMLStreamReader reader, String attributeName) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            if (attributeName.equals(reader.getAttributeLocalName(i))) {
                return reader.getAttributeValue(i);
            }
        }

        return "";
    }

    private String extractTagContent(XMLEventReader reader, XMLEventWriter writer) throws XMLStreamException {
        XMLEvent event = reader.nextEvent();
        String data = event.asCharacters().getData();
        data = data != null ? data.trim() : "";
        writer.add(event);
        return data;
    }

    public Map<String, Pair<QuantifiedObject, MeasureLabeled>> getData() {
        return data;
    }

    private void writeData() {
        writeData(null);
    }

    private void writeData(String currentTag) {
        if (currentTag == null)
            currentTag = "<other>";
        else if (!currentTag.startsWith("<")) {
            currentTag = "<" + currentTag + ">";
        }

        String text = accumulator.toString();
        List<String> tokens = null;
        try {
            tokens = QuantityAnalyzer.getInstance().tokenize(text);
        } catch (Exception e) {
            throw new GrobidException("fail to tokenize: " + text, e);
        }
        boolean begin = true;
        for (String token : tokens) {
            token = token.trim();
            if (token.length() == 0)
                continue;

            if (begin && (!currentTag.equals("<other>"))) {
                labeled.add(new ImmutablePair<>(token, "I-" + currentTag));
            } else {
                labeled.add(new ImmutablePair<>(token, currentTag));
            }

            begin = false;
        }
        accumulator.setLength(0);

    }

    public List<Pair<String, String>> getLabeled() {
        return labeled;
    }
}
