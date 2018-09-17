package org.grobid.core.engines;

import org.grobid.core.data.Quantity;
import org.grobid.core.utilities.WordsToNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic implementation of value parser without CRF. Thi could be used in the future to
 * bootstrap a more efficient ML model.
 */
public class DefaultValueParser extends ValueParser {
    private static final Logger logger = LoggerFactory.getLogger(DefaultValueParser.class);

    @Override
    public BigDecimal parseValue(String rawValue) {
        // default locale is English
        return parseValue(rawValue, Locale.ENGLISH);
    }

    @Override
    public BigDecimal parseValue(String rawValue, Locale locale) {

        // bad quick and dirty workaround!
        if (rawValue.startsWith("~")) {
            rawValue = rawValue.substring(1);
        }

        // if we have alphabetical characters, we use the word to number parser
        Pattern pattern = Pattern.compile("[a-zA-Z]{3,}");
        Matcher matcher = pattern.matcher(rawValue);
        if (matcher.find()) {
            WordsToNumber w2n = WordsToNumber.getInstance();
            return w2n.normalize(rawValue, locale);
        } else {
            // remove possible trailing punctuations (due to noisy PDF)
            rawValue = rawValue.replaceAll("[^\\d]+$", "");

            //check if there is any non-digit character
            pattern = Pattern.compile("[a-zA-Z]");
            matcher = pattern.matcher(rawValue);

            if (!matcher.find()) {
                try {
                    NumberFormat format = NumberFormat.getInstance(locale);
                    Number number = format.parse(rawValue);
                    return new BigDecimal(number.toString());
                } catch (ParseException pe) {
                    logger.warn("Invalid value expression: " + rawValue + " , for LOCALE: " + locale + ". Trying with CRF.");
                    return super.parseValue(rawValue, locale);
                }
            } else {
                //CRF
                return super.parseValue(rawValue, locale);
            }
        }
    }

}