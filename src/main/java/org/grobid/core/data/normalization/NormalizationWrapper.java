package org.grobid.core.data.normalization;

import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.UnitDefinition;
import org.grobid.core.engines.FullTextParser;
import org.grobid.core.utilities.MeasurementUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.units.ri.internal.format.l10n.NumberFormat;
import tec.units.ri.unit.BaseUnit;
import tec.units.ri.unit.ProductUnit;
import tec.units.ri.unit.TransformedUnit;

import javax.measure.Dimension;
import javax.measure.format.ParserException;
import javax.measure.format.UnitFormat;
import javax.measure.spi.Bootstrap;
import javax.measure.spi.UnitFormatService;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by lfoppiano on 14.02.16.
 */
public class NormalizationWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NormalizationWrapper.class);
    private final UnitFormat format;
    private MeasurementUtilities measurementUtilities;

    public NormalizationWrapper() {
        UnitFormatService formatService = Bootstrap.getService(UnitFormatService.class);
        format = formatService.getUnitFormat();
        measurementUtilities = new MeasurementUtilities();
    }

    public Quantity.Normalized normalizeQuantityToBaseUnits(Quantity quantity) throws NormalizationException {
        if (quantity.isEmpty() || quantity.getRawUnit() == null || isEmpty(quantity.getRawUnit().getRawName())) {
            return null;    //or throw new NormalizationException() :-)
        }
        javax.measure.Unit parsedUnit = parseUnit(quantity.getRawUnit());

        return normalizeQuantityToBaseUnits(quantity, parsedUnit);
    }

    protected javax.measure.Unit parseUnit(String rawString) throws NormalizationException {
        return parseUnit(new Unit(rawString));
    }

    protected javax.measure.Unit parseUnit(Unit rawUnit) throws NormalizationException {
        javax.measure.Unit parsedUnit = null;
        UnitDefinition definition = measurementUtilities.lookup(rawUnit);

        try {
            parsedUnit = format.parse(rawUnit.getRawName());
        } catch (ParserException pe) {

            if (definition == null) {
                throw new NormalizationException("The unit " + rawUnit + " cannot be normalized. It is either not a valid unit " +
                        "or it is not recognized from the available parsers.", pe);
            }
        }
        rawUnit.setUnitDefinition(definition);

        /**
         * workaround to avoid passing througt with a null parsedUnit - in unit-ri version 0.9 the parse() method is
         *  (more correctly, IMHO) throwing a ParserException (see above)
         */
        if (parsedUnit == null) {
            throw new NormalizationException("The unit " + rawUnit + " cannot be normalized. It is either not a valid unit " +
                    "or it is not recognized from the available parsers.", new ParserException(new RuntimeException()));
        }

        return parsedUnit;
    }

    protected Quantity.Normalized normalizeQuantityToBaseUnits(Quantity quantity, javax.measure.Unit unit) throws NormalizationException {
        Map<String, Integer> wrappedUnitProducts = new HashMap<>();
        Quantity.Normalized normalizedQuantity = new Quantity().new Normalized();

        if (unit instanceof TransformedUnit) {

            TransformedUnit transformedUnit = (TransformedUnit) unit;
            normalizedQuantity.setUnit(new Unit(transformedUnit.getParentUnit().toString()));
            try {
                normalizedQuantity.setValue(transformedUnit.getConverter().convert(Double.parseDouble(quantity.getRawValue())));
            } catch (Exception e) {
                throw new NormalizationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
                        "or it is not recognized from the available parsers.", new ParserException(new RuntimeException()));
            }
            quantity.setNormalizedQuantity(normalizedQuantity);
            wrappedUnitProducts.put(transformedUnit.getSymbol(), 1);

        } else if (unit instanceof ProductUnit) {

            ProductUnit productUnit = (ProductUnit) unit;
            //Map<String, Integer> products = extractProduct(productUnit);
            normalizedQuantity.setUnit(new Unit(productUnit.toSystemUnit().toString()));


            quantity.setNormalizedQuantity(normalizedQuantity);
            try {
                normalizedQuantity.setValue(productUnit.getSystemConverter().convert(Double.parseDouble(quantity.getRawValue())));
            } catch (Exception e) {
                throw new NormalizationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
                        "or it is not recognized from the available parsers.");
            }

        } else {
            normalizedQuantity.setRawValue(unit.getSymbol());
            normalizedQuantity.setUnit(new Unit(unit.getSymbol()));
            try {
                normalizedQuantity.setValue(Double.parseDouble(quantity.getRawValue()));
            } catch (Exception e) {
                throw new NormalizationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
                        "or it is not recognized from the available parsers.");
            }
            quantity.setNormalizedQuantity(normalizedQuantity);
        }

        if (quantity.isNormalized()) {
            UnitDefinition definition = measurementUtilities.lookup(quantity.getNormalizedQuantity().getUnit());
            if (definition != null) {
                quantity.getNormalizedQuantity().getUnit().setUnitDefinition(definition);
            }
        }

        return normalizedQuantity;
    }

    protected Map<String, Integer> extractProduct(ProductUnit productUnit) {
        Map<javax.measure.Unit, Integer> products = productUnit.getProductUnits();
        Map<String, Integer> wrappedUnitProducts = new HashMap<>();

        for (Map.Entry<javax.measure.Unit, Integer> productFactor : products.entrySet()) {
            if (productFactor.getKey().getSymbol() != null) {
                wrappedUnitProducts.put(productFactor.getKey().getSymbol(), productFactor.getValue());
            } else {
                String unitName = this.format.format(productFactor.getKey());
                wrappedUnitProducts.put(unitName, productFactor.getValue());
            }
        }
        return wrappedUnitProducts;
    }

    public String[] parseRawString(String rawString) {
        /**
         * I'm not fond of this:
         *  - parsedString[0] -> rawValue
         *  - parsedString[1] -> rawUnit
         */

        String[] parsedString = new String[2];

        // Attempt 1: is just a number
        try {
            parsedString[0] = "" + Double.parseDouble(rawString);
            return parsedString;
        } catch (NumberFormatException nfe) {
            LOGGER.warn("Ignoring NumberFormatException.");
        }

        // Attempt 2: try to parse as a unit

        try {
            Unit parsed = (Unit) parseUnit(rawString);
        } catch (NormalizationException e) {
            LOGGER.warn("Ignoring NormalizationException.");
        }

        // Attempt 3: iterating through it - assuming value comes before the unit
        StringBuilder sb = new StringBuilder();
        boolean value = false;
        boolean unit = false;
        rawString = rawString.trim();
        for (int i = 0; i < rawString.length(); i++) {
            if (rawString.charAt(i) != ' ') {
                if (Character.isDigit(rawString.charAt(i)) && i == 0 && !value) {
                    sb.append(rawString.charAt(i));
                    value = true;
                } else if ((Character.isDigit(rawString.charAt(i)) || rawString.charAt(i) == '.' || rawString.charAt(i) == ',') && value) {
                    sb.append(rawString.charAt(i));
                } else if (Character.isLetter(rawString.charAt(i)) && value) {
                    value = false;
                    parsedString[0] = sb.toString();
                    sb = new StringBuilder();
                    sb.append(rawString.charAt(i));
                    unit = true;
                } else if (unit == true) {
                    sb.append(rawString.charAt(i));
                }
            }
        }
        parsedString[1] = sb.toString();


        return parsedString;
    }
}
