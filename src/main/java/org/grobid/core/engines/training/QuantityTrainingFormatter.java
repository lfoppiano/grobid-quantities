package org.grobid.core.engines.training;

import nu.xom.Attribute;
import nu.xom.Element;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.utilities.UnitUtilities;

import java.util.List;

import static org.grobid.core.document.xml.XmlBuilderUtils.teiElement;

public class QuantityTrainingFormatter {

    protected Element trainingExtraction(List<Measurement> measurements, String text) {
        Element p = teiElement("p");

        int pos = 0;
        for (Measurement measurement : measurements) {
            Element measure = teiElement("measure");

            if (measurement.getType() == UnitUtilities.Measurement_Type.VALUE) {
                measure.addAttribute(new Attribute("type", "value"));
                Quantity quantity = measurement.getQuantityAtomic();
                if (quantity == null)
                    continue;

                int startQ = quantity.getOffsetStart();
                int endQ = quantity.getOffsetEnd();

                Element numNode = teiElement("num");
                numNode.appendChild(text.substring(startQ, endQ));

                Unit unit = quantity.getRawUnit();
                int startU = -1;
                int endU = -1;
                Element unitElement = null;
                if (unit != null) {
                    unitElement = unitToElement(text, unit);
                    startU = unit.getOffsetStart();
                    endU = unit.getOffsetEnd();
                }

                int initPos = pos;
                int firstPos = pos;
                while (pos < text.length()) {
                    if (pos == startQ) {
                        if (initPos == firstPos) {
                            p.appendChild(text.substring(firstPos, startQ));
                        } else {
                            measure.appendChild(text.substring(initPos, startQ));
                        }
                        measure.appendChild(numNode);
                        pos = endQ;
                        initPos = pos;
                    }
                    if (pos == startU) {
                        if (initPos == firstPos) {
                            p.appendChild(text.substring(firstPos, startU));
                        } else {
                            measure.appendChild(text.substring(initPos, startU));
                        }
                        measure.appendChild(unitElement);
                        pos = endU;
                        initPos = pos;
                    }

                    if ((pos >= endQ) && (pos >= endU))
                        break;
                    pos++;
                }
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
                measure.addAttribute(new Attribute("type", "interval"));
                Quantity quantityLeast = measurement.getQuantityLeast();
                Quantity quantityMost = measurement.getQuantityMost();

                if ((quantityLeast == null) || (quantityMost == null))
                    continue;

                int startQL = quantityLeast.getOffsetStart();
                int endQL = quantityLeast.getOffsetEnd();

                Element numNodeL = teiElement("num");

                if (quantityLeast.getRawValue() != null)
                    numNodeL.addAttribute(new Attribute("atLeast", quantityLeast.getRawValue().trim()));
                else
                    numNodeL.addAttribute(new Attribute("atLeast", "?"));
                numNodeL.appendChild(text.substring(startQL, endQL));

                Unit unitL = quantityLeast.getRawUnit();
                int startUL = -1;
                int endUL = -1;

                Element unitElement = null;
                if (unitL != null) {
                    unitElement = unitToElement(text, unitL);
                    startUL = unitL.getOffsetStart();
                    endUL = unitL.getOffsetEnd();
                }

                int startQM = quantityMost.getOffsetStart();
                int endQM = quantityMost.getOffsetEnd();
                Unit unitM = quantityMost.getRawUnit();

                Element numNodeM = teiElement("num");
                if (quantityMost.getRawValue() != null)
                    numNodeM.addAttribute(new Attribute("atMost", quantityMost.getRawValue().trim()));
                else
                    numNodeM.addAttribute(new Attribute("atMost", "?"));
                numNodeM.appendChild(text.substring(startQM, endQM));

                int startUM = -1;
                int endUM = -1;
                Element unitElementM = null;
                if (unitM != null) {
                    unitElementM = unitToElement(text, unitM);
                    startUM = unitM.getOffsetStart();
                    endUM = unitM.getOffsetEnd();
                }

                int initPos = pos;
                int firstPos = pos;
                while (pos < text.length()) {
                    if (pos == startQL) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startQL));
                        else
                            measure.appendChild(text.substring(initPos, startQL));
                        measure.appendChild(numNodeL);
                        pos = endQL;
                        initPos = pos;
                    }
                    if (pos == startQM) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startQM));
                        else
                            measure.appendChild(text.substring(initPos, startQM));
                        measure.appendChild(numNodeM);
                        pos = endQM;
                        initPos = pos;
                    }
                    if (pos == startUL) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startUL));
                        else
                            measure.appendChild(text.substring(initPos, startUL));
                        measure.appendChild(unitElement);
                        pos = endUL;
                        initPos = pos;
                    }
                    if ((pos == startUM) && (startUM != startUL)) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startUM));
                        else
                            measure.appendChild(text.substring(initPos, startUM));
                        measure.appendChild(unitElementM);
                        pos = endUM;
                        initPos = pos;
                    }

                    if ((pos >= endQL) && (pos >= endQM) && (pos >= endUL) && (pos >= endUM))
                        break;
                    pos++;
                }
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.INTERVAL_BASE_RANGE) {
                measure.addAttribute(new Attribute("type", "interval"));
                Quantity quantityBase = measurement.getQuantityBase();
                Quantity quantityRange = measurement.getQuantityRange();

                if ((quantityBase == null) || (quantityRange == null))
                    continue;

                int startQL = quantityBase.getOffsetStart();
                int endQL = quantityBase.getOffsetEnd();

                Element numNodeL = teiElement("num");

                numNodeL.addAttribute(new Attribute("type", "base"));
                numNodeL.appendChild(text.substring(startQL, endQL));

                Unit unitL = quantityBase.getRawUnit();
                int startUL = -1;
                int endUL = -1;

                Element unitElement = null;
                if (unitL != null) {
                    unitElement = unitToElement(text, unitL);
                    startUL = unitL.getOffsetStart();
                    endUL = unitL.getOffsetEnd();
                }

                int startQM = quantityRange.getOffsetStart();
                int endQM = quantityRange.getOffsetEnd();
                Unit unitM = quantityRange.getRawUnit();

                Element numNodeM = teiElement("num");
                numNodeM.addAttribute(new Attribute("type", "range"));
                numNodeM.appendChild(text.substring(startQM, endQM));

                int startUM = -1;
                int endUM = -1;
                Element unitElementM = null;
                if (unitM != null) {
                    unitElementM = unitToElement(text, unitM);
                    startUM = unitM.getOffsetStart();
                    endUM = unitM.getOffsetEnd();
                }

                int initPos = pos;
                int firstPos = pos;
                while (pos < text.length()) {
                    if (pos == startQL) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startQL));
                        else
                            measure.appendChild(text.substring(initPos, startQL));
                        measure.appendChild(numNodeL);
                        pos = endQL;
                        initPos = pos;
                    }
                    if (pos == startQM) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startQM));
                        else
                            measure.appendChild(text.substring(initPos, startQM));
                        measure.appendChild(numNodeM);
                        pos = endQM;
                        initPos = pos;
                    }
                    if (pos == startUL) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startUL));
                        else
                            measure.appendChild(text.substring(initPos, startUL));
                        measure.appendChild(unitElement);
                        pos = endUL;
                        initPos = pos;
                    }
                    if ((pos == startUM) && (startUM != startUL)) {
                        if (initPos == firstPos)
                            p.appendChild(text.substring(firstPos, startUM));
                        else
                            measure.appendChild(text.substring(initPos, startUM));
                        measure.appendChild(unitElementM);
                        pos = endUM;
                        initPos = pos;
                    }

                    if ((pos >= endQL) && (pos >= endQM) && (pos >= endUL) && (pos >= endUM))
                        break;
                    pos++;
                }
            } else if (measurement.getType() == UnitUtilities.Measurement_Type.CONJUNCTION) {
                measure.addAttribute(new Attribute("type", "list"));
                List<Quantity> quantities = measurement.getQuantityList();
                int initPos = pos;
                for (Quantity quantity : quantities) {
                    int startQ = quantity.getOffsetStart();
                    int endQ = quantity.getOffsetEnd();

                    Element numNode = teiElement("num");
                    numNode.appendChild(text.substring(startQ, endQ));

                    Unit unit = quantity.getRawUnit();
                    int startU = -1;
                    int endU = -1;
                    Element unitNode = null;
                    if (unit != null) {
                        unitNode = unitToElement(text, unit);
                        startU = unit.getOffsetStart();
                        endU = unit.getOffsetEnd();
                    }

                    int firstPos = pos;
                    while (pos < text.length()) {
                        if(pos > endQ && pos < startU) {
                            break;
                        }
                        if (pos == startQ) {
                            if (initPos == firstPos) {
                                p.appendChild(text.substring(firstPos, startQ));
                            } else {
                                measure.appendChild(text.substring(initPos, startQ));
                            }
                            measure.appendChild(numNode);
                            pos = endQ;
                            initPos = endQ;
                        }

                        if (pos == startU) {
                            if (initPos == firstPos) {
                                p.appendChild(text.substring(firstPos, startU));
                            } else {
                                measure.appendChild(text.substring(initPos, startU));
                            }
                            measure.appendChild(unitNode);
                            pos = endU;
                            initPos = endU;
                        }

                        if ((pos >= endQ) && (pos >= endU)) {
                            break;
                        }

                        pos++;
                    }
                }
            }
            p.appendChild(measure);
        }
        p.appendChild(text.substring(pos));

        return p;
    }

    private Element unitToElement(String text, Unit unit) {
        int startU = unit.getOffsetStart();
        int endU = unit.getOffsetEnd();

        Element unitNode = teiElement("measure");

        if ((unit.getUnitDefinition() != null) && (unit.getUnitDefinition().getType() != null)) {
            unitNode.addAttribute(new Attribute("type", unit.getUnitDefinition().getType().toString()));
        } else {
            unitNode.addAttribute(new Attribute("type", "?"));
        }

        if (unit.getRawName() != null) {
            unitNode.addAttribute(new Attribute("unit", unit.getRawName().trim()));
        } else {
            unitNode.addAttribute(new Attribute("unit", "?"));
        }

        unitNode.appendChild(text.substring(startU, endU));
        return unitNode;
    }
}
