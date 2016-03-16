package org.grobid.core.data;

/**
 * Created by lfoppiano on 14/03/16.
 */
public class RegexValueHolder {
    private String value;
    private int start;
    private int end;

    public RegexValueHolder(String group, int start, int end) {
        this.value = group;
        this.start = start;
        this.end = end;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}