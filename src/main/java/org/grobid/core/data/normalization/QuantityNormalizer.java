package org.grobid.core.data.normalization;

import com.google.common.collect.ImmutableList;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.UnitDefinition;
import org.grobid.core.utilities.MeasurementOperations;
import org.grobid.core.utilities.UnitUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.uom.common.USCustomary;
import tec.uom.se.format.SimpleUnitFormat;

import javax.measure.format.UnitFormat;
import javax.measure.spi.ServiceProvider;
import javax.measure.spi.UnitFormatService;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
    private final String UOM_DEFAULT_PROVIDER = "tec.uom.se.spi.DefaultServiceProvider";
    private final String UCUM_PROVIDER = "systems.uom.ucum.internal.UCUMServiceProvider";
    private final String UNICODE_PROVIDER = "systems.uom.unicode.internal.UnicodeServiceProvider";
    private final String SI_PROVIDER = "si.uom.impl.SIServiceProvider";
    private final String COMMON_PROVIDER = "systems.uom.common.internal.CommonServiceProvider";
    private final String INDYRIA_PROVIDER = "tech.units.indriya.internal.DefaultServiceProvider";
    private final String SESHAT_PROVIDER = "tech.uom.seshat.UnitServices";

    Map<String, UnitFormat> unitFormats = new HashMap<>();

    private MeasurementOperations measurementOperations;
    private UnitNormalizer unitNormalizer;

    public QuantityNormalizer() {
        for (ServiceProvider provider : ServiceProvider.available()) {
            UnitFormatService formatService = provider.getUnitFormatService();

            final String providerName = provider.getClass().getName();
            unitFormats.put(providerName, formatService.getUnitFormat());

            if (providerName.equals(COMMON_PROVIDER)) {
                SimpleUnitFormat.getInstance().alias(USCustomary.MILE, "mile");
                SimpleUnitFormat.getInstance().alias(USCustomary.MILE, "mi");
                SimpleUnitFormat.getInstance().alias(USCustomary.MILE, "miles");
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
            return normalizeUnknownUnitQuantity(quantity);
        } else if (parsedUnit.getUnitDefinition().getSystem() == UnitUtilities.System_Type.SI_BASE) {

            //I normalize SI units
            return normalizeSIQuantities(quantity);
        } else if (parsedUnit.getUnitDefinition().getSystem() == UnitUtilities.System_Type.SI_DERIVED) {

            //I normalize SI derived units
            return normalizeSIDerivedQuantities(quantity);

        } else {
            return normalizeNonSIQuantities(quantity);
        }
    }

    private Quantity.Normalized normalizeUnknownUnitQuantity(Quantity quantity) throws NormalizationException {
//        Map<String, Integer> wrappedUnitProducts = new HashMap<>();
        Quantity.Normalized normalizedQuantity = new Quantity().new Normalized();
        final String unitRawName = quantity.getParsedUnit().getRawName();

        List<UnitFormat> formatServices = Arrays.asList(unitFormats.get(UNICODE_PROVIDER), unitFormats.get(SESHAT_PROVIDER));

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

        List<UnitFormat> formatServices = Arrays.asList(unitFormats.get(UCUM_PROVIDER), unitFormats.get(COMMON_PROVIDER), unitFormats.get(INDYRIA_PROVIDER));

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
        for (UnitFormat formatService : formatServices.stream().filter(Objects::nonNull).collect(Collectors.toList())) {
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

        List<UnitFormat> formatServices = Arrays.asList(unitFormats.get(SI_PROVIDER), unitFormats.get(UOM_DEFAULT_PROVIDER));

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

    /**
     * Normalise SI derived quantities
     */
    protected Quantity.Normalized normalizeSIDerivedQuantities(Quantity quantity) throws NormalizationException {
        Quantity.Normalized normalizedQuantity = new Quantity().new Normalized();

        final String unitRawName = quantity.getParsedUnit().getRawName();

        List<UnitFormat> formatServices = Arrays.asList(unitFormats.get(UCUM_PROVIDER), unitFormats.get(UOM_DEFAULT_PROVIDER));

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

        normalizedQuantity.setRawValue(quantity.getRawValue());
        normalizedQuantity.setUnit(new Unit(unit.getSystemUnit().toString()));
        try {
            if (quantity.getParsedValue() != null) {
                BigDecimal converted = new BigDecimal(unit.getConverterTo(unit.getSystemUnit()).convert(quantity.getParsedValue().getNumeric()).toString());
                normalizedQuantity.setValue(converted);
            } else {
                normalizedQuantity.setValue(new BigDecimal(quantity.getRawValue()));
            }
        } catch (Exception e) {
            throw new NormalizationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
                    "or it is not recognized from the available parsers.", e);
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


    public void setUnitNormalizer(UnitNormalizer unitNormalizer) {
        this.unitNormalizer = unitNormalizer;
    }
}
