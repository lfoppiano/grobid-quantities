package org.grobid.core.data.normalization;

import com.google.common.collect.ImmutableList;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.UnitDefinition;
import org.grobid.core.utilities.MeasurementOperations;
import org.grobid.core.utilities.UnitUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.uom.seshat.ConventionalUnit;
import tec.uom.se.unit.ProductUnit;
import tec.uom.se.unit.TransformedUnit;

import javax.measure.UnitConverter;
import javax.measure.format.ParserException;
import javax.measure.format.UnitFormat;
import javax.measure.spi.ServiceProvider;
import javax.measure.spi.UnitFormatService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * This class is responsible for the normalization of the quantity.
 * The quantity normalization requires:
 * - parsed value
 * - parsed unit
 * <p>
 * Created by lfoppiano on 14.02.16.
 */
public class QuantityNormalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuantityNormalizer.class);
    private UnitFormat seshatUnitServices;
    private UnitFormat indyriaDefaultService;
    private UnitFormat commonService;
    private UnitFormat ucumFormatService;
    private UnitFormat defaultFormatService;
    private UnitFormat unicodeFormatService;
    private UnitFormat siFormatService;

    private MeasurementOperations measurementOperations;
    private UnitNormalizer unitNormalizer;

    public QuantityNormalizer() {
        ServiceProvider defaultProvider = ServiceProvider.current(); // just a fallback to avoid uninitialized variable
        for (ServiceProvider provider : ServiceProvider.available()) {
            UnitFormatService formatService = provider.getUnitFormatService();
            switch (provider.getClass().getName()) {
                case "tec.uom.se.spi.DefaultServiceProvider":
                    this.defaultFormatService = formatService.getUnitFormat();
                    break;

                case "systems.uom.ucum.internal.UCUMServiceProvider":
                    this.ucumFormatService = formatService.getUnitFormat();
                    break;

                case "systems.uom.unicode.internal.UnicodeServiceProvider":
                    this.unicodeFormatService = formatService.getUnitFormat();
                    break;

                case "si.uom.impl.SIServiceProvider":
                    this.siFormatService = formatService.getUnitFormat();
                    break;

                case "systems.uom.common.internal.CommonServiceProvider":
                    this.commonService = formatService.getUnitFormat();
                    break;

                case "tec.units.indriya.spi.DefaultServiceProvider":
                    this.indyriaDefaultService = formatService.getUnitFormat();
                    break;

                case "tech.uom.seshat.UnitServices":
                    this.seshatUnitServices = formatService.getUnitFormat();
                    break;
            }
        }

        measurementOperations = new MeasurementOperations();

        unitNormalizer = new UnitNormalizer();

    }

    public Quantity.Normalized normalizeQuantity(Quantity quantity) throws NormalizationException {
        if (quantity.isEmpty() || quantity.getRawUnit() == null || isEmpty(quantity.getRawUnit().getRawName())) {
            return null;    //or throw new NormalizationException() :-)
        }

        Unit parsedUnit = unitNormalizer.parseUnit(quantity.getRawUnit());
        quantity.setParsedUnit(parsedUnit);

        //The unit cannot be found between the known units - we should try to decompose it
        if (parsedUnit.getUnitDefinition() == null) {
            //not sure this is working
            return normalizeUnknownUnitQuantity(quantity);
        } else if (((parsedUnit.getUnitDefinition().getSystem() == UnitUtilities.System_Type.SI_BASE) ||
                (parsedUnit.getUnitDefinition().getSystem() == UnitUtilities.System_Type.SI_DERIVED))) {

            //I normalize SI and SI_DERIVED units
            return normalizeSIQuantities(quantity);
        } else {
            return normalizeNonSIQuantities(quantity);
        }
    }

    private Quantity.Normalized normalizeUnknownUnitQuantity(Quantity quantity) throws NormalizationException {
//        Map<String, Integer> wrappedUnitProducts = new HashMap<>();
        Quantity.Normalized normalizedQuantity = new Quantity().new Normalized();
        final String unitRawName = quantity.getParsedUnit().getRawName();

        List<UnitFormat> formatServices = ImmutableList.of(unicodeFormatService, seshatUnitServices);

        javax.measure.Unit unit = tryParsing(unitRawName, formatServices);

        if (unit == null) {
            throw new NormalizationException("Cannot parse " + unitRawName + " using "
                    + Arrays.toString(formatServices.toArray()));
        }

        composeUnit(quantity, normalizedQuantity, unit);

        if (quantity.isNormalized()) {
            UnitDefinition definition = unitNormalizer.findDefinition(quantity.getNormalizedQuantity().getUnit());
            if (definition != null) {
                quantity.getNormalizedQuantity().getUnit().setUnitDefinition(definition);
            }
        }

        return normalizedQuantity;
    }

    protected Quantity.Normalized normalizeNonSIQuantities(Quantity quantity) throws NormalizationException {
//        Map<String, Integer> wrappedUnitProducts = new HashMap<>();
        Quantity.Normalized normalizedQuantity = new Quantity().new Normalized();

        final String unitRawName = quantity.getParsedUnit().getRawName();

        List<UnitFormat> formatServices = Arrays.asList(ucumFormatService, commonService, indyriaDefaultService);

        javax.measure.Unit unit = tryParsing(unitRawName, formatServices);

        if (unit == null) {
            throw new NormalizationException("Cannot parse " + unitRawName + " using "
                    + Arrays.toString(formatServices.toArray()));
        }

        composeUnit(quantity, normalizedQuantity, unit);

        if (quantity.isNormalized()) {
            UnitDefinition definition = unitNormalizer.findDefinition(quantity.getNormalizedQuantity().getUnit());
            if (definition != null) {
                quantity.getNormalizedQuantity().getUnit().setUnitDefinition(definition);
            }
        }

        return normalizedQuantity;
    }

    private javax.measure.Unit tryParsing(String unitRawName, List<UnitFormat> formatServices) {
        javax.measure.Unit unit = null;
        for (UnitFormat formatService : formatServices) {
            try {
                unit = formatService.parse(unitRawName);
                break;
            } catch (Throwable tr) {
                LOGGER.warn("Cannot parse " + unitRawName + " with " + formatService.getClass().getName(), tr);
            }
        }
        return unit;
    }


    /**
     * Normalise SI quantities. It tries with the SI parser and if it's failing it's backing off
     * using the default format service.
     */
    protected Quantity.Normalized normalizeSIQuantities(Quantity quantity) throws NormalizationException {
//        Map<String, Integer> wrappedUnitProducts = new HashMap<>();
        Quantity.Normalized normalizedQuantity = new Quantity().new Normalized();

        final String unitRawName = quantity.getParsedUnit().getRawName();

        List<UnitFormat> formatServices = Arrays.asList(siFormatService, defaultFormatService);

        javax.measure.Unit unit = tryParsing(unitRawName, formatServices);

        if (unit == null) {
            throw new NormalizationException("Cannot parse " + unitRawName + " using "
                    + Arrays.toString(formatServices.toArray()));
        }
        composeUnit(quantity, normalizedQuantity, unit);

        if (quantity.isNormalized()) {
            UnitDefinition definition = unitNormalizer.findDefinition(quantity.getNormalizedQuantity().getUnit());
            if (definition != null) {
                quantity.getNormalizedQuantity().getUnit().setUnitDefinition(definition);
            }
        }

        return normalizedQuantity;
    }

    

    private void composeUnit(Quantity quantity, Quantity.Normalized normalizedQuantity, javax.measure.Unit unit) throws NormalizationException {

        normalizedQuantity.setUnit(new Unit(unit.getSystemUnit().toString()));
        try {
            BigDecimal converted = new BigDecimal(unit.getConverterTo(unit.getSystemUnit()).convert(quantity.getParsedValue().getNumeric()).toString());
            normalizedQuantity.setValue(converted);
        } catch (Exception e) {
            throw new NormalizationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
                    "or it is not recognized from the available parsers.", new ParserException(new RuntimeException()));
        }
        quantity.setNormalizedQuantity(normalizedQuantity);

        /*if (unit instanceof TransformedUnit) {
            TransformedUnit transformedUnit = (TransformedUnit) unit;
            normalizedQuantity.setUnit(new Unit(transformedUnit.getParentUnit().toString()));
            try {
                normalizedQuantity.setValue(new BigDecimal(transformedUnit.getSystemConverter().convert(quantity.getParsedValue().getNumeric()).toString()));
            } catch (Exception e) {
                throw new NormalizationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
                        "or it is not recognized from the available parsers.", new ParserException(new RuntimeException()));
            }
            quantity.setNormalizedQuantity(normalizedQuantity);
//            wrappedUnitProducts.put(transformedUnit.getSymbol(), 1);

        } else if (unit instanceof ProductUnit) {
            ProductUnit productUnit = (ProductUnit) unit;
            //Map<String, Integer> products = extractProduct(productUnit);
            normalizedQuantity.setUnit(new Unit(productUnit.toSystemUnit().toString()));

            quantity.setNormalizedQuantity(normalizedQuantity);
            try {
                normalizedQuantity.setValue(new BigDecimal(productUnit.getSystemConverter().convert(quantity.getParsedValue().getNumeric()).toString()));
            } catch (Exception e) {
                throw new NormalizationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
                        "or it is not recognized from the available parsers.");
            }

        } else if (unit instanceof ConventionalUnit) {
            normalizedQuantity.setRawValue(unit.getSystemUnit().toString());
            normalizedQuantity.setUnit(new Unit(unit.getSystemUnit().toString()));
            
            try {
                if (quantity.getParsedValue() != null) {
                    final BigDecimal normalisedValue = new BigDecimal(unit.getConverterTo(unit.getSystemUnit())
                            .convert(quantity.getParsedValue().getNumeric()).toString());
                    normalizedQuantity.setValue(normalisedValue);
                } else {
                    normalizedQuantity.setValue(new BigDecimal(quantity.getRawValue()));
                }
            } catch (Exception e) {
                throw new NormalizationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
                        "or it is not recognized from the available parsers.");
            }
            quantity.setNormalizedQuantity(normalizedQuantity);
        } else {
            normalizedQuantity.setRawValue(unit.getSymbol());
            normalizedQuantity.setUnit(new Unit(unit.getSymbol()));
            try {
                if (quantity.getParsedValue() != null) {
                    normalizedQuantity.setValue(quantity.getParsedValue().getNumeric());
                } else {
                    normalizedQuantity.setValue(new BigDecimal(quantity.getRawValue()));
                }
            } catch (Exception e) {
                throw new NormalizationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
                        "or it is not recognized from the available parsers.");
            }
            quantity.setNormalizedQuantity(normalizedQuantity);
        }*/
    }

    protected Map<String, Integer> extractProduct(ProductUnit productUnit) {
        Map<javax.measure.Unit, Integer> products = productUnit.getBaseUnits();
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

    public void setUnitNormalizer(UnitNormalizer unitNormalizer) {
        this.unitNormalizer = unitNormalizer;
    }
}
