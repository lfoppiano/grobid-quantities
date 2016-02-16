package org.grobid.core.data.normalization;

import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import tec.units.ri.unit.TransformedUnit;

import javax.measure.format.ParserException;
import javax.measure.format.UnitFormat;
import javax.measure.spi.Bootstrap;
import javax.measure.spi.UnitFormatService;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.collections4.MapUtils.isEmpty;

/**
 * Created by lfoppiano on 14.02.16.
 */
public class NormalizationWrapper {


    private final UnitFormat format;

    public NormalizationWrapper() {
        UnitFormatService formatService = Bootstrap.getService(UnitFormatService.class);
        format = formatService.getUnitFormat();
    }


    public Map<String, Integer> extractUnitProducts(String rawUnit) throws NormalizationException {
        javax.measure.Unit parsedUnit = null;

        try {
            parsedUnit = format.parse(rawUnit);
        } catch (ParserException pe) {
            throw new NormalizationException("The value " + rawUnit + "cannot be normalized. It is either not a valid unit" +
                    "or it is not recognized from the available parsers.", pe);
        }

        /**
         * workaround to avoid passing througt with a null parsedUnit - in unit-ri version 0.9 the parse() method is
         *  (more correctly, IMHO) throwing a ParserException (see above)
         */
        if (parsedUnit == null) {
            throw new NormalizationException("The value " + rawUnit + "cannot be normalized. It is either not a valid unit" +
                    "or it is not recognized from the available parsers.", new ParserException(new RuntimeException()));
        }

        Map<javax.measure.Unit, Integer> products = parsedUnit.getProductUnits();
        Map<String, Integer> wrappedUnitProducts = new HashMap<>();

        if (isEmpty(products)) {
            wrappedUnitProducts.put(parsedUnit.getSymbol(), 1);
        } else {
            for (Map.Entry<javax.measure.Unit, Integer> unit : products.entrySet()) {
                if (unit.getKey().getSymbol() != null) {
                    wrappedUnitProducts.put(unit.getKey().getSymbol(), unit.getValue());
                } else {
                    String unitName = this.format.format(unit.getKey());
                    wrappedUnitProducts.put(unitName, unit.getValue());
                }
            }
        }

        return wrappedUnitProducts;
    }


    public Quantity normalizeQuantity(Quantity quantity) throws NormalizationException {
        if (quantity == null) {
            return null;    //or throw new NormalizationException() :-)
        }
        else if (quantity.getRawUnit() == null) {
            // unit not yet extracted for this quantity
            return quantity;    //or throw new NormalizationException() :-)
        }
        else if (quantity.getRawUnit().getRawName() == null) {
            // unknown unit for the quantity
            return quantity;    //or throw new NormalizationException() :-)
        }

        String rawUnit = quantity.getRawUnit().getRawName();

        javax.measure.Unit parsedUnit = null;
        try {
            parsedUnit = format.parse(rawUnit);
        } catch (ParserException pe) {
            throw new NormalizationException("The value " + rawUnit + "cannot be normalized. It is either not a valid unit" +
                    "or it is not recognized from the available parsers.", pe);
        }

        Map<javax.measure.Unit, Integer> products = parsedUnit.getProductUnits();
        Map<String, Integer> wrappedUnitProducts = new HashMap<>();

        if (isEmpty(products)) {    //basic unit m, km and so on
            populateNormalizedQuantityValue(quantity, parsedUnit);
            wrappedUnitProducts.put(parsedUnit.getSymbol(), 1);
        } else {
            for (Map.Entry<javax.measure.Unit, Integer> unit : products.entrySet()) {
                if (unit.getKey().getSymbol() != null) {
                    wrappedUnitProducts.put(unit.getKey().getSymbol(), unit.getValue());
                } else {    //symbol = null in non basic unit.
                    populateNormalizedQuantityValue(quantity, unit.getKey());

                    String unitName = this.format.format(unit.getKey());
                    wrappedUnitProducts.put(unitName, unit.getValue());
                }
            }
        }
        quantity.setProductForm(wrappedUnitProducts);

        return quantity;
    }

    private Quantity populateNormalizedQuantityValue(Quantity quantity, javax.measure.Unit unit) {
        Unit normalizedUnit = new Unit();
        if (unit instanceof TransformedUnit) {
            TransformedUnit transformedUnit = (TransformedUnit) unit;
            normalizedUnit.setRawName(transformedUnit.getParentUnit().toString());
            quantity.setNormalizedUnit(normalizedUnit);
            quantity.setNormalizedValue(transformedUnit.getConverter().convert(Double.parseDouble(quantity.getRawValue())));
        } else {
            normalizedUnit.setRawName(unit.getSymbol());
            quantity.setNormalizedUnit(normalizedUnit);
            quantity.setNormalizedValue(Double.parseDouble(quantity.getRawValue()));
        }

        return quantity;
    }
}
