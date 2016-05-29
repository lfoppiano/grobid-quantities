package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.data.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for the value part of a recognized quantity. The goal of the present parser is
 * to recognize and distinguish numerical values, values expressed in letters ("twenty"), 
 * exponent of tens (1 x 107), exponent symbol (0.2E-4), and dates ("October 19, 2014 at 20:09 TDB").
 */
//public class ValueParser extends AbstractParser {
public class ValueParser {
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
        //super(GrobidModels.VALUE);
    }

    /** do nothing for the moment, use DefaultValueParser ;) */
    public void parseValue(Quantity quantity) {
    }

}