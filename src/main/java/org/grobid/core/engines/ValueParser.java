package org.grobid.core.engines;

import org.grobid.core.data.ValueBlock;
import org.grobid.core.engines.label.QuantitiesTaggingLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorValue;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.OffsetPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.grobid.core.engines.label.QuantitiesTaggingLabels.*;

/**
 * Parser for the value part of a recognized quantity. The goal of the present parser is
 * to recognize and distinguish numerical values, values expressed in letters ("twenty"),
 * exponent of tens (1 x 107), exponent symbol (0.2E-4), and dates ("October 19, 2014 at 20:09 TDB").
 */
public class ValueParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueParser.class);

    private static volatile ValueParser instance;

    public static ValueParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new DefaultValueParser();
    }

    protected ValueParser() {
        super(QuantitiesModels.VALUE);
    }

    public BigDecimal parseValue(String rawValue) {
        return parseValue(rawValue, Locale.ENGLISH);
    }

    public BigDecimal parseValue(String rawValue, Locale locale) {
        NumberFormat format = NumberFormat.getInstance(locale);
        List<ValueBlock> values = tagValue(rawValue);

        ValueBlock block = values.get(0);
        final String value1 = block.getValue();
        Number number = null;
        BigDecimal result = null;
        try {
            number = format.parse(value1);
            result = new BigDecimal(number.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (block.hasBaseAndPow() && result != null) {
            try {
                BigDecimal baseBd = BigDecimal.ZERO;
                if (equalsIgnoreCase(block.getBase(), "e")) {
                    baseBd = new BigDecimal(Math.E);
                } else {
                    Number base = format.parse(block.getBase());
                    baseBd = new BigDecimal(base.toString());
                }

                final Number pow = format.parse(block.getPow());
                final int intPower = pow.intValue();
                BigDecimal secondPart = BigDecimal.ONE;
                if(intPower < 0) {
                    final BigDecimal powBd = baseBd.pow(-intPower);
                    secondPart = BigDecimal.ONE.divide(powBd, 10, RoundingMode.HALF_UP);
                } else {
                    secondPart = baseBd.pow(intPower);
                }

                return result.multiply(secondPart);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public List<ValueBlock> tagValue(String text) {
        if (isBlank(text)) {
            return null;
        }

        //Remove spaces. It's a workaround (to be check whether it is working) because spaces are causing troubles
        text = text.replaceAll(" ", "");
        List<ValueBlock> values = new ArrayList<>();

        try {
            text = text.replace("\n", "");
            List<LayoutToken> tokenizations = new ArrayList<>();

            String ress = null;
            List<String> characters = new ArrayList<>();
            for (char character : text.toCharArray()) {
                characters.add(String.valueOf(character));
                OffsetPosition position = new OffsetPosition();
                position.start = text.indexOf(character);
                position.end = text.indexOf(character) + 1;
                LayoutToken lt = new LayoutToken(String.valueOf(character));
                tokenizations.add(lt);
            }

            ress = addFeatures(characters);
            String res;
            try {
                res = label(ress);
            } catch (Exception e) {
                throw new GrobidException("CRF labeling for quantity parsing failed.", e);
            }
            values = resultExtraction(res, tokenizations);
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }

        return values;
    }

    /**
     * Extract identified quantities from a labelled text.
     */
    public List<ValueBlock> resultExtraction(String result, List<LayoutToken> tokenizations) {
        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(QuantitiesModels.VALUE, result, tokenizations);
        List<TaggingTokenCluster> clusters = clusteror.cluster();

        boolean denominator = false;
        int currentPow = 1;
        boolean startUnit = false;
        TaggingLabel previousTag = null;

        List<ValueBlock> values = new ArrayList<>();
        ValueBlock valueBlock = new ValueBlock();

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            String clusterContent = LayoutTokensUtil.toText(cluster.concatTokens());

            if (clusterLabel.equals(QuantitiesTaggingLabels.VALUE_VALUE_VALUE)) {
                valueBlock.setValue(clusterContent);
                LOGGER.debug(clusterContent + "(V)");
            } else if (clusterLabel.equals(QuantitiesTaggingLabels.VALUE_VALUE_BASE)) {
                valueBlock.setBase(clusterContent);
                LOGGER.debug(clusterContent + "(B)");
            } else if (clusterLabel.equals(VALUE_VALUE_OTHER)) {
                LOGGER.debug(clusterContent + "(O)");
            } else if (clusterLabel.equals(VALUE_VALUE_POW)) {
                valueBlock.setPow(clusterContent);
                LOGGER.debug(clusterContent + "(P)");
            } else if (clusterLabel.equals(VALUE_VALUE_OPERATION)) {
                valueBlock.setPow(clusterContent);
                LOGGER.debug(clusterContent + "(Op)");
            }
            previousTag = clusterLabel;
        }
        values.add(valueBlock);
        LOGGER.debug("--");

        return values;
    }


    @SuppressWarnings({"UnusedParameters"})
    private String addFeatures(List<String> characters) {

        StringBuilder result = new StringBuilder();
        try {
            for (String character : characters) {
                FeaturesVectorValue featuresVector =
                        FeaturesVectorValue.addFeatures(character, null);

                result.append(featuresVector.printVector())
                        .append("\n");
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return result.toString();
    }

}