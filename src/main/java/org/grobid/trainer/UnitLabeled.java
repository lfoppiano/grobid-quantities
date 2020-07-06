package org.grobid.trainer;

import org.grobid.core.utilities.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lfoppiano on 24/04/16.
 */
public class UnitLabeled {

    //Indicate whether the unit has a quantity attached to the right. Units have usually left attachment (they come
    // after the quantity. However for special cases, such as pH, they are placed before.
    private boolean hasRightAttachment = false;
    private List<Pair<String, String>> labels = new ArrayList<>();

    public boolean hasRightAttachment() {
        return hasRightAttachment;
    }

    public void setHasRightAttachment(boolean hasRightAttachment) {
        this.hasRightAttachment = hasRightAttachment;
    }

    public boolean addLabel(Pair<String, String> label) {
        return labels.add(label);
    }

    public List<Pair<String, String>> getLabels() {
        return labels;
    }

    public void setLabels(List<Pair<String, String>> labels) {
        this.labels = labels;
    }
}
