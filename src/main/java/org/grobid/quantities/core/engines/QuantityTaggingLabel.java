package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.utilities.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Representing labels that can be tagged.
 *
 * To be review with actual TaggingLabel class, as an enum class cannot be extended in Java.
 */
public enum QuantityTaggingLabel {

    // labels for quantities/measurements
    VALUE_ATOMIC(GrobidModels.QUANTITIES, "<valueAtomic>"),
    VALUE_LEAST(GrobidModels.QUANTITIES, "<valueLeast>"),
    VALUE_MOST(GrobidModels.QUANTITIES, "<valueMost>"),
    VALUE_LIST(GrobidModels.QUANTITIES, "<valueList>"),
    UNIT_LEFT(GrobidModels.QUANTITIES, "<unitLeft>"),
    UNIT_RIGHT(GrobidModels.QUANTITIES, "<unitRight>"),
    SUBSTANCE(GrobidModels.QUANTITIES, "<substance>")
    ;

    private final GrobidModels grobidModel;
    private final String label;

    private static Map<Pair<GrobidModels, String>, QuantityTaggingLabel> cache = new HashMap<>();

    static {
        for (QuantityTaggingLabel l : values()) {
            cache.put(new Pair<>(l.grobidModel, l.label), l);
        }
    }

    QuantityTaggingLabel(GrobidModels grobidModel, String label) {
        this.grobidModel = grobidModel;
        this.label = label;
    }

    public GrobidModels getGrobidModel() {
        return grobidModel;
    }

    public String getLabel() {
        return label;
    }

    public static QuantityTaggingLabel getLabel(GrobidModels model, String tag) {
        String plainLabel = GenericTaggerUtils.getPlainLabel(tag);
        QuantityTaggingLabel l = cache.get(new Pair<>(model, plainLabel));
        if (l == null) {
            throw new IllegalArgumentException("Label " + plainLabel + " not found for model " + model);
        }
        return l;
    }

}
