package org.grobid.core.engines;

import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Value;
import org.grobid.core.data.ValueBlock;
import org.grobid.core.data.normalization.NormalizationException;
import org.grobid.core.engines.label.QuantitiesTaggingLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorValues;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.core.utilities.WordsToNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.*;
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
        instance = new ValueParser();
    }

    protected ValueParser() {
        super(QuantitiesModels.VALUES);
    }

    public ValueParser(GrobidModel model) {
        super(model);
    }

    public Value parseValue(String rawValue) {
        return parseValue(rawValue, Locale.ENGLISH);
    }

    public Value parseValue(String rawValue, Locale locale) {
        ValueBlock block = tagValue(rawValue);

        BigDecimal numeric = parseValueBlock(block, locale);
        final Value resultValue = new Value();
        resultValue.setRawValue(rawValue);
        resultValue.setNumeric(numeric);
        resultValue.setStructure(block);

        return resultValue;
    }


    protected BigDecimal parseValueBlock(ValueBlock block, Locale locale) {
        NumberFormat format = NumberFormat.getInstance(locale);

        switch (block.getType()) {
            case NUMBER:
                try {
                    BigDecimal secondPart = null;
                    if (block.getPow() != null && block.getBase() != null) {
                        final Number pow = format.parse(block.getPowAsString());
                        String baseAsString = removeSpacesTabsAndBl(block.getBaseAsString());

                        final BigDecimal baseBd = new BigDecimal(format.parse(baseAsString).toString());
                        final int intPower = pow.intValue();

                        if (intPower < 0) {
                            final BigDecimal powBd = baseBd.pow(-intPower);
                            secondPart = BigDecimal.ONE.divide(powBd, 10, RoundingMode.HALF_UP);
                        } else {
                            secondPart = baseBd.pow(intPower);
                        }
                    }

                    if (block.getNumber() != null) {
                        String numberAsString = removeSpacesTabsAndBl(block.getNumberAsString());
                        final BigDecimal number = new BigDecimal(format.parse(numberAsString).toString());
                        if (secondPart != null) {
                            return number.multiply(secondPart);
                        }
                        return number;
                    } else {
                        return secondPart;
                    }

                } catch (ParseException | ArithmeticException | NumberFormatException e) {
                    LOGGER.error("Cannot parse " + block.toString() + " with Locale " + locale, e);
                }

                break;
            case EXPONENT:
                try {
                    BigDecimal secondPart = null;
                    if (block.getExp() != null) {
                        final Number exp = format.parse(block.getExpAsString());
                        final int intPower = exp.intValue();
                        final BigDecimal exponentialBase = new BigDecimal(Math.E);

                        if (intPower < 0) {
                            final BigDecimal powBd = exponentialBase.pow(-intPower);
                            secondPart = BigDecimal.ONE.divide(powBd, 10, RoundingMode.HALF_UP);
                        } else {
                            secondPart = exponentialBase.pow(intPower);
                        }
                    }

                    if (isNotEmpty(block.getNumberAsString())) {
                        final BigDecimal number = new BigDecimal(format.parse(block.getNumberAsString()).toString());
                        if (secondPart != null) {
                            return number.multiply(secondPart);
                        }
                    } else {
                        return secondPart;
                    }

                } catch (ParseException | ArithmeticException e) {
                    LOGGER.error("Cannot parse " + block.toString() + " with Locale " + locale, e);
                }

                break;

            case ALPHABETIC:
                WordsToNumber w2n = WordsToNumber.getInstance();
                try {
                    return w2n.normalize(block.getAlphaAsString(), locale);
                } catch (NormalizationException e) {
                    LOGGER.error("Cannot parse " + block.toString() + " with Locale " + locale, e);
                }
                break;

            case TIME:
                //we do not parse it for the moment
                break;

        }

        return null;
    }

    private String removeSpacesTabsAndBl(String block) {
        return UnicodeUtil.normaliseText(block)
            .replaceAll("\n", " ")
            .replaceAll("\t", " ")
            .replaceAll(" ", "");
    }


    public ValueBlock tagValue(String text) {
        if (isBlank(text)) {
            return null;
        }

        ValueBlock parsedValue = null;

        try {
            text = text.replace("\n\r", " ");

            QuantityAnalyzer analyzer = QuantityAnalyzer.getInstance();
            List<LayoutToken> layoutTokens = analyzer.tokenizeWithLayoutTokenByCharacter(text);

            String ress = addFeatures(layoutTokens);
            String res;
            try {
                res = label(ress);
            } catch (Exception e) {
                throw new GrobidException("CRF labeling for quantity parsing failed.", e);
            }
            parsedValue = resultExtraction(res, layoutTokens);
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }

        return parsedValue;
    }

    /**
     * Extract identified quantities from a labelled text.
     *  - if whatever contained into is numeric, it goes into <number>
     *  - if <number> comes after <pow> or <exp> it's probably the exponent or part of it and it's concatenated to the previous
     *  - if <base> contains e then the following <pow> should go into <exp>
     */
    public ValueBlock resultExtraction(String result, List<LayoutToken> tokenizations) {
        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(QuantitiesModels.VALUES, result, tokenizations);
        List<TaggingTokenCluster> clusters = clusteror.cluster();

        String rawValue = LayoutTokensUtil.toText(tokenizations);

        ValueBlock valueBlock = new ValueBlock();
        valueBlock.setRawValue(rawValue);

        int start = 0;
        int end = 0;

        StringBuilder rawTaggedValue = new StringBuilder();
        TaggingLabel previous = null;

        boolean forceExp = false;

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            String clusterContent = LayoutTokensUtil.toText(cluster.concatTokens());
            end = start + clusterContent.length();
            OffsetPosition offsets = new OffsetPosition(start, end);
            String trimmedClusterContent = trim(clusterContent);

            if (!clusterLabel.equals(VALUE_VALUE_OTHER)) {
                rawTaggedValue.append(clusterLabel.getLabel());
            }
            rawTaggedValue.append(trimmedClusterContent);
            if (!clusterLabel.equals(VALUE_VALUE_OTHER)) {
                rawTaggedValue.append(clusterLabel.getLabel().replace("<", "</"));
            }

            if (clusterLabel.equals(QuantitiesTaggingLabels.VALUE_VALUE_NUMBER)) {
                // If a number comes after an exponent, might be just wrongly recognised
                /*if (trimmedClusterContent.equals("0") && previous != null) {
                    appendToPrevious(valueBlock, trimmedClusterContent, previous);
                } else*/
                if (previous != null && (previous.equals(VALUE_VALUE_EXP) || previous.equals(VALUE_VALUE_POW))) {
                    appendToPrevious(valueBlock, trimmedClusterContent, previous);
                } else {
                    if (isNotEmpty(valueBlock.getNumberAsString())) {
                        valueBlock.setNumber(valueBlock.getNumberAsString() + trimmedClusterContent);
                        valueBlock.getNumber().getOffsets().end = offsets.end;
                        LOGGER.debug(clusterContent + "(appending N)");
                    } else {
                        valueBlock.setNumber(trimmedClusterContent);
                        valueBlock.getNumber().setOffsets(offsets);
                        LOGGER.debug(clusterContent + "(N)");
                    }
                }
            } else if (clusterLabel.equals(QuantitiesTaggingLabels.VALUE_VALUE_BASE)) {
                if (trimmedClusterContent.equalsIgnoreCase("e")) {
                    forceExp = true;
                } else {
                    valueBlock.setBase(trimmedClusterContent);
                    valueBlock.getBase().setOffsets(offsets);
                }
                LOGGER.debug(clusterContent + "(B)");
            } else if (clusterLabel.equals(VALUE_VALUE_OTHER)) {
                LOGGER.debug(clusterContent + "(O)");
            } else if (clusterLabel.equals(VALUE_VALUE_POW)) {
                if (forceExp) {
                    valueBlock.setExp(clusterContent);
                    valueBlock.getExp().setOffsets(offsets);
                    clusterLabel = VALUE_VALUE_EXP;
                    forceExp = false;
                } else {
                    valueBlock.setPow(clusterContent);
                    valueBlock.getPow().setOffsets(offsets);
                    LOGGER.debug(clusterContent + "(P)");
                }
            } else if (clusterLabel.equals(VALUE_VALUE_EXP)) {
                valueBlock.setExp(clusterContent);
                valueBlock.getExp().setOffsets(offsets);
                LOGGER.debug(clusterContent + "(E)");
            } else if (clusterLabel.equals(VALUE_VALUE_TIME)) {
                valueBlock.setTime(trimmedClusterContent);
                valueBlock.getTime().setOffsets(offsets);
                LOGGER.debug(clusterContent + "(T)");
            } else if (clusterLabel.equals(VALUE_VALUE_ALPHA)) {
                if (isNumeric(trimmedClusterContent)) {
                    valueBlock.setNumber(valueBlock.getNumberAsString() + trimmedClusterContent);
                    if (valueBlock.getNumber().getOffsets().start == -1 && valueBlock.getNumber().getOffsets().end == -1) {
                        valueBlock.getNumber().setOffsets(offsets);
                    } else {
                        valueBlock.getNumber().getOffsets().end = offsets.end;
                    }
                    LOGGER.debug(clusterContent + "(fake A) -> (N)");
                } else {
                    valueBlock.setAlpha(trimmedClusterContent);
                    valueBlock.getAlpha().setOffsets(offsets);
                    LOGGER.debug(clusterContent + "(A)");
                }
            }
            previous = clusterLabel;

            start = end;
        }

        valueBlock.setRawTaggedValue(rawTaggedValue.toString());

        return valueBlock;
    }

    private void appendToPrevious(ValueBlock valueBlock, String valueToBeAppended, TaggingLabel previous) {
        if (previous.equals(QuantitiesTaggingLabels.VALUE_VALUE_NUMBER)) {
            String previousValue = valueBlock.getNumberAsString();
            valueBlock.getNumber().setValue(previousValue + valueToBeAppended);
        } else if (previous.equals(QuantitiesTaggingLabels.VALUE_VALUE_BASE)) {
            String previousValue = valueBlock.getBaseAsString();
            valueBlock.setBase(previousValue + valueToBeAppended);
        } else if (previous.equals(VALUE_VALUE_POW)) {
            String previousValue = valueBlock.getPowAsString();
            valueBlock.setPow(previousValue + valueToBeAppended);
        } else if (previous.equals(VALUE_VALUE_EXP)) {
            String previousValue = valueBlock.getExpAsString();
            valueBlock.setExp(previousValue + valueToBeAppended);
        } else if (previous.equals(VALUE_VALUE_TIME)) {
            String previousValue = valueBlock.getTimeAsString();
            valueBlock.setTime(previousValue + valueToBeAppended);
        } else if (previous.equals(VALUE_VALUE_ALPHA)) {
            String previousValue = valueBlock.getAlphaAsString();
            valueBlock.setAlpha(previousValue + valueToBeAppended);
        }
    }


    @SuppressWarnings({"UnusedParameters"})
    private String addFeatures(List<LayoutToken> layoutTokens) {

        StringBuilder result = new StringBuilder();
        try {
            for (LayoutToken token : layoutTokens) {
                if (isBlank(token.getText())) {
                    continue;
                }

                FeaturesVectorValues featuresVector =
                    FeaturesVectorValues.addFeatures(trim(token.getText()), null);

                result.append(featuresVector.printVector())
                    .append("\n");
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return result.toString();
    }

}