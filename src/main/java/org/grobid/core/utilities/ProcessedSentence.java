package org.grobid.core.utilities;    

import org.grobid.core.utilities.OffsetPosition;
import java.util.*; 

/**
 *  Representation for a sentence processed by multiple components with associated list of structures 
 *  enriching the processed sentence. This class is intended to be immutable!
 * 
 *  @author Patrice Lopez
 *
 */
public class ProcessedSentence {

    private final String sentence;
    private final List<Parse> parses;
    private OffsetPosition offset;
 
    public ProcessedSentence(String theSentence, 
                       List<Parse> theParses, 
                       OffsetPosition theOffset) {
        this.sentence = theSentence;
        this.parses = theParses;
        this.offset = theOffset;
    }

    public String getSentence() { 
        return sentence; 
    }     

    public List<Parse> getParses() { 
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