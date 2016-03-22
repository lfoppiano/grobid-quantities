package org.grobid.core.data.normalization;

import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.UnitBlock;
import org.grobid.core.data.UnitDefinition;
import org.grobid.core.engines.UnitParser;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.utilities.MeasurementOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.uom.se.unit.ProductUnit;
import tec.uom.se.unit.TransformedUnit;

import javax.measure.format.ParserException;
import javax.measure.format.UnitFormat;
import javax.measure.spi.Bootstrap;
import javax.measure.spi.UnitFormatService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by lfoppiano on 14.02.16.
 */
public class NormalizationWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NormalizationWrapper.class);
    private final UnitFormat defaultFormatService;
    private MeasurementOperations measurementOperations;
    private UnitParser unitParser;
    private QuantityLexicon quantityLexicon;

    public NormalizationWrapper() {
        UnitFormatService formatService = Bootstrap.getService(UnitFormatService.class);
        unitParser = UnitParser.getInstance();
        defaultFormatService = formatService.getUnitFormat();
        measurementOperations = new MeasurementOperations();
        quantityLexicon = QuantityLexicon.getInstance();
    }

    public Quantity.Normalized normalizeQuantityToBaseUnits(Quantity quantity) throws NormalizationException {
        if (quantity.isEmpty() || quantity.getRawUnit() == null || isEmpty(quantity.getRawUnit().getRawName())) {
            return null;    //or throw new NormalizationException() :-)
        }
        quantity.setRawUnit(findDefinition(quantity.getRawUnit()));
        javax.measure.Unit parsedUnit = parseUnit(quantity.getRawUnit());

        return normalizeQuantityToBaseUnits(quantity, parsedUnit);
    }

    public Unit findDefinition(Unit unit) {
        UnitDefinition definition = measurementOperations.quantityLexicon.lookup(unit);

        if (definition != null) {
            unit.setUnitDefinition(definition);
        }
        return unit;
    }

    protected javax.measure.Unit parseUnit(String rawString) throws NormalizationException {
        return parseUnit(new Unit(rawString));
    }

    /**
     * Method to parse and recognize the unit.
     * <p/>
     * TODO: integrate additional parsers in case the default parser is not able to successfully recognize the unit.
     */
    protected javax.measure.Unit parseUnit(Unit rawUnit) throws NormalizationException {
        String unitName = quantityLexicon.getNameByInflection(rawUnit.getRawName());
        if (unitName == null) {
            List<UnitBlock> unitBlockList = unitParser.tagUnit(rawUnit.getRawName());
            unitName = UnitBlock.unitBlocksToString(unitBlockList);
        }

        javax.measure.Unit parsedUnit = null;

        try {
            parsedUnit = defaultFormatService.parse(unitName);
        } catch (ParserException pe) {
            try {
                parsedUnit = defaultFormatService.parse(rawUnit.getRawName());
            } catch (ParserException pep) {
                //buh
            }
        }

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
                normalizedQuantity.setValue(new BigDecimal(transformedUnit.getConverter().convert(quantity.getParsedValue()).toString()));
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
                normalizedQuantity.setValue(new BigDecimal(productUnit.getSystemConverter().convert(quantity.getParsedValue()).toString()));
            } catch (Exception e) {
                throw new NormalizationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
                        "or it is not recognized from the available parsers.");
            }

        } else {
            normalizedQuantity.setRawValue(unit.getSymbol());
            normalizedQuantity.setUnit(new Unit(unit.getSymbol()));
            try {
                normalizedQuantity.setValue(new BigDecimal(quantity.getRawValue()));
            } catch (Exception e) {
                throw new NormalizationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
                        "or it is not recognized from the available parsers.");
            }
            quantity.setNormalizedQuantity(normalizedQuantity);
        }

        if (quantity.isNormalized()) {
            UnitDefinition definition = measurementOperations.quantityLexicon.lookup(quantity.getNormalizedQuantity().getUnit());
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
                String unitName = this.defaultFormatService.format(productFactor.getKey());
                wrappedUnitProducts.put(unitName, productFactor.getValue());
            }
        }
        return wrappedUnitProducts;
    }

    public void setUnitParser(UnitParser unitParser) {
        this.unitParser = unitParser;
    }
}
