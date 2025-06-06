package org.grobid.core.data.normalization;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.data.UnitBlock;
import org.grobid.core.data.UnitDefinition;
import org.grobid.core.utilities.UnitUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.uom.common.USCustomary;
import tech.units.indriya.format.SimpleUnitFormat;

import javax.measure.format.MeasurementParseException;
import javax.measure.format.UnitFormat;
import javax.measure.spi.FormatService;
import javax.measure.spi.ServiceProvider;
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
    protected static final String UOM_DEFAULT_PROVIDER = "Default";
    protected static final String UCUM_PROVIDER = "UCUMServiceProvider";
    protected static final String UNICODE_PROVIDER = "Unicode";
    protected static final String SI_PROVIDER = "SI";
    protected static final String COMMON_PROVIDER = "Common";
    //    protected static final String INDYRIA_PROVIDER = "DefaultServiceProvider";
    protected static final String SESHAT_PROVIDER = "UnitServices";

    private Map<String, UnitFormat> unitFormats = new HashMap<>();

//    private MeasurementOperations measurementOperations;

    private UnitNormalizer unitNormalizer;

    public QuantityNormalizer() {
        for (ServiceProvider provider : ServiceProvider.available()) {
            try {
                FormatService formatService = provider.getFormatService();

                unitFormats.put(provider.toString(), formatService.getUnitFormat());

                if (StringUtils.equals(provider.toString(), COMMON_PROVIDER)) {
                    SimpleUnitFormat.getInstance().alias(USCustomary.MILE, "mile");
                    SimpleUnitFormat.getInstance().alias(USCustomary.MILE, "mi");
                    SimpleUnitFormat.getInstance().alias(USCustomary.MILE, "miles");
                    SimpleUnitFormat.getInstance().alias(USCustomary.YARD, "yards");
                    SimpleUnitFormat.getInstance().alias(USCustomary.YARD, "yard");
                    SimpleUnitFormat.getInstance().alias(USCustomary.YARD, "yd");
                }
            } catch (Exception e) {
                LOGGER.warn("Exception when initialising the quantity normaliser. ", e);
            }
        }


//        measurementOperations = new MeasurementOperations();
        unitNormalizer = new UnitNormalizer();
    }


    public Quantity.Normalized normalizeQuantity(Quantity quantity) throws NormalizationException {
        if (quantity.isEmpty() || quantity.getRawUnit() == null || isEmpty(quantity.getRawUnit().getRawName())) {
            return null;    //or throw new NormalizationException() :-)
        }

        Unit parsedUnit = unitNormalizer.parseUnit(quantity.getRawUnit());
        quantity.setParsedUnit(parsedUnit);

        javax.measure.Unit unit = tryParsing(parsedUnit);

        Quantity.Normalized normalizedQuantity = new Quantity().new Normalized();

        normalizedQuantity.setRawValue(quantity.getRawValue());
        if (StringUtils.isNotBlank(unit.getSystemUnit().toString())) {
            normalizedQuantity.setUnit(new Unit(unit.getSystemUnit().toString()));
        }
        try {
            if (quantity.getParsedValue() != null) {
                BigDecimal converted = new BigDecimal(unit.getConverterTo(unit.getSystemUnit()).convert(quantity.getParsedValue().getNumeric()).toString());
                normalizedQuantity.setValue(converted);
            } else {
                throw new NormalizationException("Normalisation impossible without parsed value. ");
            }
        } catch (Exception e) {
            throw new NormalizationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
                "or it is not recognized from the available parsers.", e);
        }
        quantity.setNormalizedQuantity(normalizedQuantity);

//        composeUnit(quantity, normalizedQuantity, unit);

        if (quantity.isNormalized()) {
            UnitDefinition definition = unitNormalizer.findDefinition(quantity.getNormalizedQuantity().getUnit());
            if (definition == null) {
                String normalisedUnit = quantity.getNormalizedQuantity().getUnit().getRawName();
                String normalisedUnitWithoutSuperscripts = replaceSuperscripts(normalisedUnit);
                Unit newUnit = new Unit(normalisedUnitWithoutSuperscripts);

                definition = unitNormalizer.findDefinition(newUnit);
                if (definition != null) {
                    quantity.getNormalizedQuantity().getUnit().setUnitDefinition(definition);
                }
            } else {
                quantity.getNormalizedQuantity().getUnit().setUnitDefinition(definition);
            }
        }

        return normalizedQuantity;
    }

    private String replaceSuperscripts(String normalisedUnit) {
        String str = normalisedUnit.replaceAll("⁰", "0");
        str = str.replaceAll("¹", "1");
        str = str.replaceAll("²", "2");
        str = str.replaceAll("³", "3");
        str = str.replaceAll("⁴", "4");
        str = str.replaceAll("⁵", "5");
        str = str.replaceAll("⁶", "6");
        str = str.replaceAll("⁷", "7");
        str = str.replaceAll("⁸", "8");
        str = str.replaceAll("⁹", "9");

        return str;
    }

    protected javax.measure.Unit tryParsing(Unit parsedUnit) throws NormalizationException {
        List<UnitFormat> parsers = new ArrayList<>();

        if (parsedUnit.getUnitDefinition() == null) {
            parsers.add(unitFormats.get(UCUM_PROVIDER));
            parsers.add(unitFormats.get(UNICODE_PROVIDER));
        } else {
            if (!parsedUnit.getUnitDefinition().isSkipNormalisation()) {
                if (parsedUnit.getUnitDefinition().getSystem() == UnitUtilities.System_Type.SI_BASE) {
                    //I normalize SI units
                    parsers.add(unitFormats.get(SI_PROVIDER));
                    parsers.add(unitFormats.get(UOM_DEFAULT_PROVIDER));
                } else if (parsedUnit.getUnitDefinition().getSystem() == UnitUtilities.System_Type.SI_DERIVED) {
                    //I normalize SI derived units
                    parsers.add(unitFormats.get(UCUM_PROVIDER));
                    parsers.add(unitFormats.get(UOM_DEFAULT_PROVIDER));
                    parsers.add(unitFormats.get(UNICODE_PROVIDER));
                } else {
                    parsers.add(unitFormats.get(UCUM_PROVIDER));
                    parsers.add(unitFormats.get(COMMON_PROVIDER));
                }
            }
        }

        parsers = parsers.stream().filter(Objects::nonNull).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(parsers)) {
            throw new NormalizationException("Cannot find a parser for " + parsedUnit.getRawName()
                + ". Please check the dependencies of UOM or make sure the unit you're trying ot parse is supported. ");
        }

        javax.measure.Unit unit = null;
        for (UnitFormat formatService : parsers) {
            try {
                unit = formatService.parse(parsedUnit.getRawName());
                break;
            } catch (Throwable e) {
                LOGGER.debug("Cannot parse " + parsedUnit + " with " + formatService.getClass().getName(), e);
            }
        }

        if (unit == null) {
            List<javax.measure.Unit> unitList = new ArrayList<>();
            for (UnitBlock block : parsedUnit.getProductBlocks()) {

                for (UnitFormat formatService : parsers) {
                    try {
                        unitList.add(formatService.parse(UnitBlock.asString(block)));
                        break;
                    } catch (MeasurementParseException e) {

                        //handling 1/{unit}, processing just the unit
                        if (StringUtils.equalsAnyIgnoreCase(block.getPow(), "-1", "−1")) {
                            String onlyUnit = block.getPrefix() + block.getBase();
                            try {
                                javax.measure.Unit<?> onlyUnitParsed = formatService.parse(onlyUnit);
                                unitList.add(onlyUnitParsed.pow(-1));
                            } catch (Throwable e2) {
                                LOGGER.warn("Trying excluding the negative power. Cannot parse " + onlyUnit + " with " + formatService.getClass().getName());
                                LOGGER.debug("Exception", e2);
                            }
                            break;
                        }

                        LOGGER.debug("Cannot parse " + block.toString() + " with " + formatService.getClass().getName(), e);
                    } catch (Throwable t) {
                        LOGGER.debug("Cannot parse " + block.toString() + " with " + formatService.getClass().getName(), t);
                    }
                }
            }

            if (CollectionUtils.isEmpty(unitList) || unitList.size() != parsedUnit.getProductBlocks().size()) {
                throw new NormalizationException("Cannot parse " + parsedUnit.getRawName() + " using "
                    + Arrays.toString(parsers.toArray()));
            }

            javax.measure.Unit result = null;
            for (int i = 0; i < unitList.size(); i++) {
                if (i == 0) {
                    result = unitList.get(i);
                } else {
                    result = result.multiply(unitList.get(i));
                }
            }

            unit = result;

        }

        return unit;
    }

//    private void composeUnit(Quantity quantity, Quantity.Normalized normalizedQuantity, javax.measure.Unit unit) throws NormalisationException {

//        normalizedQuantity.setRawValue(quantity.getRawValue());
//        normalizedQuantity.setUnit(new Unit(unit.getSystemUnit().toString()));
//        try {
//            if (quantity.getParsedValue() != null) {
//                BigDecimal converted = new BigDecimal(unit.getConverterTo(unit.getSystemUnit()).convert(quantity.getParsedValue().getNumeric()).toString());
//                normalizedQuantity.setValue(converted);
//            } else {
//                normalizedQuantity.setValue(new BigDecimal(quantity.getRawValue()));
//            }
//        } catch (Exception e) {
//            throw new NormalisationException("The value " + quantity.getRawValue() + " cannot be normalized. It is either not a valid value " +
//                    "or it is not recognized from the available parsers.", e);
//        }
//        quantity.setNormalizedQuantity(normalizedQuantity);

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
//    }


    public void setUnitNormalizer(UnitNormalizer unitNormalizer) {
        this.unitNormalizer = unitNormalizer;
    }

    public Map<String, UnitFormat> getUnitFormats() {
        return unitFormats;
    }
}
