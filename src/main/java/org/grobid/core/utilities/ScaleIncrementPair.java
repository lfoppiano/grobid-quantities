package org.grobid.core.utilities;

import java.math.BigDecimal;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just an utility structure to be used for word to number conversion. 
 */
public class ScaleIncrementPair {

    public double scale;
    public int increment;

    public ScaleIncrementPair(double s, int i) {
        scale = s;
        increment = i;
    }
}