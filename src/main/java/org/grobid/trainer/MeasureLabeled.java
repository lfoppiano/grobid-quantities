package org.grobid.trainer;

import org.grobid.core.utilities.OffsetPosition;

public class MeasureLabeled {

    private String id;
    private String rawName;
    private OffsetPosition offsetPosition;

    public MeasureLabeled(String id) {

    }

    public MeasureLabeled() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String rawName) {
        this.rawName = rawName;
    }

    public OffsetPosition getOffsetPosition() {
        return offsetPosition;
    }

    public void setOffsetPosition(OffsetPosition offsetPosition) {
        this.offsetPosition = offsetPosition;
    }

    public void setStartPosition(int startPosition) {
        this.offsetPosition.start = startPosition;
    }

    public int getStartPosition() {
        return this.offsetPosition.start;
    }

    public void setEndPosition(int endPosition) {
        this.offsetPosition.end = endPosition;
    }

    public int getEndPosition() {
        return this.offsetPosition.end;
    }
}
