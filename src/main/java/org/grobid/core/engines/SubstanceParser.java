package org.grobid.core.engines;

import org.grobid.core.data.Measurement;
import org.grobid.core.layout.LayoutToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public List<Measurement> parseSubstance(List<LayoutToken> tokens, List<Measurement> measurements) {
        return measurements;
    }

}