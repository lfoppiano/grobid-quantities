package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.WordsToNumber;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
    public void parseValue(Quantity quantity) {
        // default locale is English
        parseValue(quantity, Locale.ENGLISH);
    }

    public void parseValue(Quantity quantity, Locale locale) {
        String raw = quantity.getRawValue();

        // bad quick and dirty workaround!
        if(raw.startsWith("~")){
            raw = raw.substring(1);
        }

        // if we have alphabetical characters, we use the word to number parser
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher matcher = pattern.matcher(raw);
        if (matcher.find()) {
            WordsToNumber w2n = WordsToNumber.getInstance();
            quantity.setParsedValue(w2n.normalize(raw, locale));
        } else {
            // remove possible trailing punctuations (due to noisy PDF)
            raw = raw.replaceAll("[^\\d]+$", "");
            NumberFormat format = NumberFormat.getInstance(locale);
            try {
                Number number = format.parse(raw);
                quantity.setParsedValue(new BigDecimal(number.toString()));
            } catch (ParseException pe) {
                logger.error("Invalid value expression: " + raw + " , for LOCALE: " + locale);
            }
        }
    }

}