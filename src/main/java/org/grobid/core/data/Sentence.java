package org.grobid.core.data;

import org.grobid.core.utilities.OffsetPosition;

import java.util.List;

/**
 *  Representation for a sentence processed by multiple components with associated list of structures 
 *  enriching the processed sentence. This class is intended to be immutable!
 * 
 *  @author Patrice Lopez
 *
 */
public class Sentence {

    private final String sentence;
    private final List<SentenceParse> parses;
    private OffsetPosition offset;
 
    public Sentence(String theSentence,
                    List<SentenceParse> theParses,
                    OffsetPosition theOffset) {
        this.sentence = theSentence;
        this.parses = theParses;
        this.offset = theOffset;
    }

    public String getSentence() { 
        return sentence; 
    }     

    public List<SentenceParse> getParses() {
        return parses; 
    }

    public OffsetPosition getOffset() {
        return offset;
    }

    public int getOffsetStart() {
        return offset.start;
    }

    public int getOffsetEnd() {
        return offset.end;
    }

}