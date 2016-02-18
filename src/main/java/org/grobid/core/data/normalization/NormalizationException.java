package org.grobid.core.data.normalization;

import javax.measure.format.ParserException;

/**
 * Created by lfoppiano on 15.02.16.
 */
public class NormalizationException extends Exception {
    public NormalizationException(String s) {
        super(s);
    }

    public NormalizationException(String s, ParserException pe) {
        super(s, pe);
    }
}
