package org.grobid.trainer.stax;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class StackTags {

    private List<String> stackTags = new LinkedList<>();

    public void append(String tag) {
        stackTags.add(tag);
    }

    public String peek() {
        return stackTags.remove(stackTags.size() - 1);
    }

    public String toString() {
        return "/" + StringUtils.join(stackTags, "/");
    }

}