package org.grobid.core.data.normalization;

import javax.measure.format.MeasurementParseException;

/**
 * Created by lfoppiano on 15.02.16.
 */
public class NormalisationException extends Exception {
    public NormalisationException(String s) {
        super(s);
    }

    public NormalisationException(String s, MeasurementParseException pe) {
        super(s, pe);
    }

    public NormalisationException(String s, Exception e) {
        super(s, e);
    }
}
