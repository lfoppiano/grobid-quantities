package org.grobid.core.utilities;

import nu.xom.Attribute;
import nu.xom.Element;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.data.*;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.Page;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.grobid.core.document.xml.XmlBuilderUtils.teiElement;

public class TeiUtils {

    private static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";

    /**
     * Serialises measurements response as a TEI document with inline annotations in the text
     * and rich structured annotations in a standOff section.
     */
    public static String toTei(MeasurementsResponse response, String text) {
        Element root = getQuantitiesTEIHeader(-1);

        Element textNode = teiElement("text");
        textNode.addAttribute(new Attribute("xml:lang", XML_NAMESPACE, "en"));
        Element body = teiElement("body");
        Element p = teiElement("p");

        List<Measurement> measurements = (response != null && response.getMeasurements() != null)
                ? response.getMeasurements()
                : Collections.emptyList();

        buildInlineParagraph(p, measurements, text);
        body.appendChild(p);
        textNode.appendChild(body);
        root.appendChild(textNode);

        if ((response != null && response.getRuntime() > 0) || !measurements.isEmpty()) {
            Element standOff = buildStandOff(response, measurements, text != null);
            root.appendChild(standOff);
        }

        return XmlBuilderUtils.toXml(root);
    }

    public static String toTei(MeasurementsResponse response) {
        return toTei(response, null);
    }

    public static String toTei(List<Measurement> measurements, String text) {
        return toTei(new MeasurementsResponse(measurements), text);
    }

    public static Element getQuantitiesTEIHeader(int id) {
        Element tei = teiElement("tei");
        Element teiHeader = teiElement("teiHeader");

        if (id != -1) {
            Element fileDesc = teiElement("fileDesc");
            fileDesc.addAttribute(new Attribute("xml:id", XML_NAMESPACE, "_" + id));
            teiHeader.appendChild(fileDesc);
        }

        Element encodingDesc = teiElement("encodingDesc");

        Element appInfo = teiElement("appInfo");

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);
        String dateISOString = df.format(new java.util.Date());

        Element application = teiElement("application");
        application.addAttribute(new Attribute("version", GrobidProperties.getVersion()));
        application.addAttribute(new Attribute("ident", "grobid-quantities"));
        application.addAttribute(new Attribute("when", dateISOString));

        Element ref = teiElement("ref");
        ref.addAttribute(new Attribute("target", "https://github.com/kermitt2/grobid-quantities"));
        ref.appendChild("grobid-quantities - A machine learning library for extracting and normalising physical measurements from scientific literature");

        application.appendChild(ref);
        appInfo.appendChild(application);
        encodingDesc.appendChild(appInfo);
        teiHeader.appendChild(encodingDesc);
        tei.appendChild(teiHeader);

        return tei;
    }

    // ==========================================
    // Inline markup construction
    // ==========================================

    private static class InlineSpan {
        int start;
        int end;
        Element element;

        InlineSpan(int start, int end, Element element) {
            this.start = start;
            this.end = end;
            this.element = element;
        }
    }

    private static class MeasureSpan {
        int start;
        int end;
        Element element;

        MeasureSpan(int start, int end, Element element) {
            this.start = start;
            this.end = end;
            this.element = element;
        }
    }

    private static void buildInlineParagraph(Element p, List<Measurement> measurements, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        if (measurements == null || measurements.isEmpty()) {
            p.appendChild(text);
            return;
        }

        List<MeasureSpan> measureSpans = new ArrayList<>();
        for (int i = 0; i < measurements.size(); i++) {
            Measurement m = measurements.get(i);
            MeasureSpan ms = createInlineMeasure(m, i, text);
            if (ms != null) {
                measureSpans.add(ms);
            }
        }

        // Sort by start offset
        measureSpans.sort(Comparator.comparingInt(m -> m.start));

        // Filter overlapping measures and emit into paragraph
        int pos = 0;
        for (MeasureSpan ms : measureSpans) {
            if (ms.start >= pos && ms.end <= text.length()) {
                if (ms.start > pos) {
                    p.appendChild(text.substring(pos, ms.start));
                }
                p.appendChild(ms.element);
                pos = ms.end;
            }
        }

        if (pos < text.length()) {
            p.appendChild(text.substring(pos));
        }
    }

    private static MeasureSpan createInlineMeasure(Measurement m, int index, String text) {
        if (m == null || m.getType() == null) {
            return null;
        }

        String id = "m" + index;
        List<InlineSpan> spans = new ArrayList<>();

        if (m.getType() == UnitUtilities.Measurement_Type.VALUE) {
            Quantity q = m.getQuantityAtomic();
            if (q != null) {
                addQuantitySpans(spans, q, id + "-num", id + "-unit", text, null, null, null);
            }
        } else if (m.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
            Quantity least = m.getQuantityLeast();
            Quantity most = m.getQuantityMost();
            if (least != null) {
                String atLeast = getQuantityNumericOrRaw(least);
                addQuantitySpans(spans, least, id + "-num-least", id + "-unit-least", text, atLeast, null, null);
            }
            if (most != null) {
                String atMost = getQuantityNumericOrRaw(most);
                addQuantitySpans(spans, most, id + "-num-most", id + "-unit", text, null, atMost, null);
            }
        } else if (m.getType() == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
            Quantity base = m.getQuantityBase();
            Quantity range = m.getQuantityRange();
            if (base != null) {
                addQuantitySpans(spans, base, id + "-num-base", id + "-unit-base", text, null, null, "base");
            }
            if (range != null) {
                addQuantitySpans(spans, range, id + "-num-range", id + "-unit", text, null, null, "range");
            }
        } else if (m.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
            if (m.getQuantityList() != null) {
                for (int j = 0; j < m.getQuantityList().size(); j++) {
                    Quantity q = m.getQuantityList().get(j);
                    if (q != null) {
                        addQuantitySpans(spans, q, id + "-num-" + j, id + "-unit-" + j, text, null, null, null);
                    }
                }
            }
        }

        if (spans.isEmpty()) {
            if (m.getRawOffsets() != null && m.getRawOffsets().start >= 0 &&
                    m.getRawOffsets().end > m.getRawOffsets().start && m.getRawOffsets().end <= text.length()) {
                Element measure = createMeasureElement(m, id);
                measure.appendChild(text.substring(m.getRawOffsets().start, m.getRawOffsets().end));
                return new MeasureSpan(m.getRawOffsets().start, m.getRawOffsets().end, measure);
            }
            return null;
        }

        // Sort spans and filter any internal overlaps
        spans.sort(Comparator.comparingInt(s -> s.start));
        List<InlineSpan> nonOverlapping = new ArrayList<>();
        int lastEnd = -1;
        for (InlineSpan s : spans) {
            if (s.start >= lastEnd) {
                nonOverlapping.add(s);
                lastEnd = s.end;
            }
        }

        int mStart = nonOverlapping.get(0).start;
        int mEnd = nonOverlapping.get(nonOverlapping.size() - 1).end;

        Element measure = createMeasureElement(m, id);
        int innerPos = mStart;
        for (InlineSpan s : nonOverlapping) {
            if (s.start > innerPos) {
                measure.appendChild(text.substring(innerPos, s.start));
            }
            measure.appendChild(s.element);
            innerPos = s.end;
        }
        if (innerPos < mEnd) {
            measure.appendChild(text.substring(innerPos, mEnd));
        }

        return new MeasureSpan(mStart, mEnd, measure);
    }

    private static Element createMeasureElement(Measurement m, String id) {
        Element measure = teiElement("measure");
        measure.addAttribute(new Attribute("xml:id", XML_NAMESPACE, id));

        String typeAttr = "value";
        if (m.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX ||
                m.getType() == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
            typeAttr = "interval";
        } else if (m.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
            typeAttr = "list";
        }
        measure.addAttribute(new Attribute("type", typeAttr));
        measure.addAttribute(new Attribute("corresp", "#ann-" + id));

        if (m.getQuantifiedObject() != null && StringUtils.isNotBlank(m.getQuantifiedObject().getRawName())) {
            measure.addAttribute(new Attribute("commodity", m.getQuantifiedObject().getRawName()));
        }

        return measure;
    }

    private static void addQuantitySpans(List<InlineSpan> spans, Quantity q, String numId, String unitId,
                                         String text, String atLeast, String atMost, String numType) {
        if (q.getOffsetStart() >= 0 && q.getOffsetEnd() > q.getOffsetStart() && q.getOffsetEnd() <= text.length()) {
            Element num = teiElement("num");
            num.addAttribute(new Attribute("xml:id", XML_NAMESPACE, numId));
            if (numType != null) {
                num.addAttribute(new Attribute("type", numType));
            }
            if (atLeast != null) {
                num.addAttribute(new Attribute("atLeast", atLeast));
            } else if (atMost != null) {
                num.addAttribute(new Attribute("atMost", atMost));
            } else if (q.getParsedValue() != null && q.getParsedValue().getNumeric() != null) {
                num.addAttribute(new Attribute("value", q.getParsedValue().getNumeric().toPlainString()));
            }
            num.appendChild(text.substring(q.getOffsetStart(), q.getOffsetEnd()));
            spans.add(new InlineSpan(q.getOffsetStart(), q.getOffsetEnd(), num));
        }

        if (q.getRawUnit() != null) {
            Unit u = q.getRawUnit();
            if (u.getOffsetStart() >= 0 && u.getOffsetEnd() > u.getOffsetStart() && u.getOffsetEnd() <= text.length()) {
                // Deduplicate if already present
                boolean alreadyAdded = spans.stream().anyMatch(s -> s.start == u.getOffsetStart() && s.end == u.getOffsetEnd());
                if (!alreadyAdded) {
                    Element unitElem = teiElement("unit");
                    unitElem.addAttribute(new Attribute("xml:id", XML_NAMESPACE, unitId));
                    if (u.hasDefinition() && u.getUnitDefinition().getType() != null) {
                        unitElem.addAttribute(new Attribute("type", u.getUnitDefinition().getType().getName()));
                    }
                    unitElem.appendChild(text.substring(u.getOffsetStart(), u.getOffsetEnd()));
                    spans.add(new InlineSpan(u.getOffsetStart(), u.getOffsetEnd(), unitElem));
                }
            }
        }
    }

    private static String getQuantityNumericOrRaw(Quantity q) {
        if (q.getParsedValue() != null && q.getParsedValue().getNumeric() != null) {
            return q.getParsedValue().getNumeric().toPlainString();
        }
        return q.getRawValue();
    }

    // ==========================================
    // Standoff annotations construction
    // ==========================================

    private static Element buildStandOff(MeasurementsResponse response, List<Measurement> measurements, boolean hasInline) {
        Element standOff = teiElement("standOff");

        if (response != null && response.getRuntime() > 0) {
            Element metaFs = createFs("metadata");
            metaFs.appendChild(createFeatureNumeric("runtime", String.valueOf(response.getRuntime())));
            standOff.appendChild(metaFs);
        }

        if (response != null && response.getPages() != null && !response.getPages().isEmpty()) {
            Element pagesFs = createFs("pages");
            for (Page page : response.getPages()) {
                Element pFs = createFs("page");
                pFs.appendChild(createFeatureNumeric("page_height", String.valueOf(page.getHeight())));
                pFs.appendChild(createFeatureNumeric("page_width", String.valueOf(page.getWidth())));
                pagesFs.appendChild(createFeatureFs("page", pFs));
            }
            standOff.appendChild(pagesFs);
        }

        if (!measurements.isEmpty()) {
            Element listAnnotation = teiElement("listAnnotation");
            listAnnotation.addAttribute(new Attribute("type", "measurements"));

            for (int i = 0; i < measurements.size(); i++) {
                Measurement m = measurements.get(i);
                Element annotation = teiElement("annotation");
                annotation.addAttribute(new Attribute("xml:id", XML_NAMESPACE, "ann-m" + i));
                if (hasInline) {
                    annotation.addAttribute(new Attribute("corresp", "#m" + i));
                }
                annotation.appendChild(createMeasurementFs(m));
                listAnnotation.appendChild(annotation);
            }
            standOff.appendChild(listAnnotation);
        }

        return standOff;
    }

    private static Element createMeasurementFs(Measurement m) {
        Element fs = createFs("measurement");

        if (m.getType() != null) {
            String typeName;
            if (m.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX ||
                    m.getType() == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
                typeName = "interval";
            } else {
                typeName = m.getType().getName();
            }
            fs.appendChild(createFeatureSymbol("type", typeName));
        }

        if (StringUtils.isNotBlank(m.getRawString())) {
            fs.appendChild(createFeatureString("measurementRaw", m.getRawString()));
        }

        if (m.getRawOffsets() != null && m.getRawOffsets().start > -1 && m.getRawOffsets().end > -1) {
            Element offsetsFs = createFs("offsets");
            offsetsFs.appendChild(createFeatureNumeric("start", String.valueOf(m.getRawOffsets().start)));
            offsetsFs.appendChild(createFeatureNumeric("end", String.valueOf(m.getRawOffsets().end)));
            fs.appendChild(createFeatureFs("measurementOffsets", offsetsFs));
        }

        if (m.getType() == UnitUtilities.Measurement_Type.VALUE) {
            if (m.getQuantityAtomic() != null) {
                fs.appendChild(createFeatureFs("quantity", createQuantityFs(m.getQuantityAtomic())));
            }
        } else if (m.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
            if (m.getQuantityLeast() != null) {
                fs.appendChild(createFeatureFs("quantityLeast", createQuantityFs(m.getQuantityLeast())));
            }
            if (m.getQuantityMost() != null) {
                fs.appendChild(createFeatureFs("quantityMost", createQuantityFs(m.getQuantityMost())));
            }
        } else if (m.getType() == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
            if (m.getQuantityBase() != null) {
                fs.appendChild(createFeatureFs("quantityBase", createQuantityFs(m.getQuantityBase())));
            }
            if (m.getQuantityRange() != null) {
                fs.appendChild(createFeatureFs("quantityRange", createQuantityFs(m.getQuantityRange())));
            }
            if (m.getQuantityLeast() != null) {
                fs.appendChild(createFeatureFs("quantityLeast", createQuantityFs(m.getQuantityLeast())));
            }
            if (m.getQuantityMost() != null) {
                fs.appendChild(createFeatureFs("quantityMost", createQuantityFs(m.getQuantityMost())));
            }
        } else if (m.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
            if (m.getQuantityList() != null && !m.getQuantityList().isEmpty()) {
                Element f = createFeature("quantities");
                for (Quantity q : m.getQuantityList()) {
                    if (q != null) {
                        f.appendChild(createQuantityFs(q));
                    }
                }
                fs.appendChild(f);
            }
        }

        if (m.getQuantifiedObject() != null) {
            fs.appendChild(createFeatureFs("quantified", createQuantifiedFs(m.getQuantifiedObject())));
        }

        if (m.getBoundingBoxes() != null && !m.getBoundingBoxes().isEmpty()) {
            Element f = createFeature("boundingBoxes");
            for (BoundingBox box : m.getBoundingBoxes()) {
                if (box != null) {
                    f.appendChild(createBoundingBoxFs(box));
                }
            }
            fs.appendChild(f);
        }

        return fs;
    }

    private static Element createQuantityFs(Quantity q) {
        Element fs = createFs("quantity");

        if (q.getType() != null) {
            fs.appendChild(createFeatureSymbol("type", q.getType().getName()));
        }

        if (StringUtils.isNotBlank(q.getRawValue())) {
            fs.appendChild(createFeatureString("rawValue", q.getRawValue()));
        }

        if (q.getRawUnit() != null) {
            fs.appendChild(createFeatureFs("rawUnit", createUnitFs(q.getRawUnit())));
        }

        if (q.getParsedUnit() != null) {
            fs.appendChild(createFeatureFs("parsedUnit", createUnitFs(q.getParsedUnit())));
        }

        if (q.getParsedValue() != null) {
            fs.appendChild(createFeatureFs("parsedValue", createValueFs(q.getParsedValue())));
        }

        if (q.isNormalized()) {
            Quantity.Normalized norm = q.getNormalizedQuantity();
            if (norm.getValue() != null) {
                fs.appendChild(createFeatureNumeric("normalizedQuantity", norm.getValue().toPlainString()));
            }
            if (norm.getUnit() != null) {
                fs.appendChild(createFeatureFs("normalizedUnit", createUnitFs(norm.getUnit())));
            }
        }

        if (q.getOffsetStart() != -1) {
            fs.appendChild(createFeatureNumeric("offsetStart", String.valueOf(q.getOffsetStart())));
        }
        if (q.getOffsetEnd() != -1) {
            fs.appendChild(createFeatureNumeric("offsetEnd", String.valueOf(q.getOffsetEnd())));
        }

        return fs;
    }

    private static Element createUnitFs(Unit unit) {
        Element fs = createFs("unit");

        if (StringUtils.isNotBlank(unit.getRawName())) {
            fs.appendChild(createFeatureString("name", unit.getRawName()));
        }

        if (unit.hasDefinition()) {
            UnitDefinition def = unit.getUnitDefinition();
            if (def.getType() != null) {
                fs.appendChild(createFeatureSymbol("type", def.getType().getName()));
            }
            if (def.getSystem() != null) {
                fs.appendChild(createFeatureSymbol("system", def.getSystem().getName()));
            }
        }

        if (unit.getOffsetStart() != -1) {
            fs.appendChild(createFeatureNumeric("offsetStart", String.valueOf(unit.getOffsetStart())));
        }
        if (unit.getOffsetEnd() != -1) {
            fs.appendChild(createFeatureNumeric("offsetEnd", String.valueOf(unit.getOffsetEnd())));
        }

        return fs;
    }

    private static Element createValueFs(Value val) {
        Element fs = createFs("parsedValue");

        if (StringUtils.isNotBlank(val.getRawValue())) {
            fs.appendChild(createFeatureString("name", val.getRawValue()));
        }

        if (val.getNumeric() != null) {
            fs.appendChild(createFeatureNumeric("numeric", val.getNumeric().toPlainString()));
        }

        if (val.getStructure() != null) {
            Element structFs = createFs("valueStructure");
            structFs.appendChild(createFeatureSymbol("type", val.getStructure().getType().name()));
            if (val.getStructure().getType() != ValueBlock.Type.UNKNOWN && val.getStructure().toString() != null) {
                structFs.appendChild(createFeatureString("formatted", val.getStructure().toString()));
            }
            fs.appendChild(createFeatureFs("structure", structFs));
        }

        if (val.getOffsetStart() != -1) {
            fs.appendChild(createFeatureNumeric("offsetStart", String.valueOf(val.getOffsetStart())));
        }
        if (val.getOffsetEnd() != -1) {
            fs.appendChild(createFeatureNumeric("offsetEnd", String.valueOf(val.getOffsetEnd())));
        }

        return fs;
    }

    private static Element createQuantifiedFs(QuantifiedObject qo) {
        Element fs = createFs("quantified");

        if (StringUtils.isNotBlank(qo.getRawName())) {
            fs.appendChild(createFeatureString("rawName", qo.getRawName()));
        }
        if (StringUtils.isNotBlank(qo.getNormalizedName())) {
            fs.appendChild(createFeatureString("normalizedName", qo.getNormalizedName()));
        }
        if (qo.getOffsetStart() != -1) {
            fs.appendChild(createFeatureNumeric("offsetStart", String.valueOf(qo.getOffsetStart())));
        }
        if (qo.getOffsetEnd() != -1) {
            fs.appendChild(createFeatureNumeric("offsetEnd", String.valueOf(qo.getOffsetEnd())));
        }

        return fs;
    }

    private static Element createBoundingBoxFs(BoundingBox box) {
        Element fs = createFs("boundingBox");
        fs.appendChild(createFeatureNumeric("page", String.valueOf(box.getPage())));
        fs.appendChild(createFeatureNumeric("x", String.valueOf(box.getX())));
        fs.appendChild(createFeatureNumeric("y", String.valueOf(box.getY())));
        fs.appendChild(createFeatureNumeric("width", String.valueOf(box.getWidth())));
        fs.appendChild(createFeatureNumeric("height", String.valueOf(box.getHeight())));
        return fs;
    }

    // ==========================================
    // Feature structure helpers
    // ==========================================

    private static Element createFs(String type) {
        Element fs = teiElement("fs");
        fs.addAttribute(new Attribute("type", type));
        return fs;
    }

    private static Element createFeature(String name) {
        Element f = teiElement("f");
        f.addAttribute(new Attribute("name", name));
        return f;
    }

    private static Element createFeatureString(String name, String value) {
        Element f = createFeature(name);
        Element s = teiElement("string");
        s.appendChild(value);
        f.appendChild(s);
        return f;
    }

    private static Element createFeatureNumeric(String name, String value) {
        Element f = createFeature(name);
        Element n = teiElement("numeric");
        n.addAttribute(new Attribute("value", value));
        f.appendChild(n);
        return f;
    }

    private static Element createFeatureSymbol(String name, String value) {
        Element f = createFeature(name);
        Element s = teiElement("symbol");
        s.addAttribute(new Attribute("value", value));
        f.appendChild(s);
        return f;
    }

    private static Element createFeatureFs(String name, Element childFs) {
        Element f = createFeature(name);
        f.appendChild(childFs);
        return f;
    }
}

