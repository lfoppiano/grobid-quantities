package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for identifying and attaching the quantified "substance".
 */
//public class ValueParser extends AbstractParser {
public class SubstanceParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubstanceParser.class);

    private static volatile SubstanceParser instance;

    public static SubstanceParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new DefaultSubstanceParser();
    }

    protected SubstanceParser() {
        //super(GrobidModels.VALUE);
    }

    /** do nothing for the moment, use DefaultSubstanceParser ;) */
    public List<Measurement> parseSubstance(String text, List<Measurement> measurements) {
        return measurements;
    }

}