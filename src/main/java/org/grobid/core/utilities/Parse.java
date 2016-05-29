package org.grobid.core.utilities;

import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.analyzers.QuantityAnalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import com.googlecode.clearnlp.conversion.AbstractC2DConverter;
import com.googlecode.clearnlp.conversion.EnglishC2DConverter;
import com.googlecode.clearnlp.constituent.CTLibEn;
import com.googlecode.clearnlp.constituent.CTReader;
import com.googlecode.clearnlp.constituent.CTTree;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.headrule.HeadRuleMap;
import com.googlecode.clearnlp.morphology.AbstractMPAnalyzer;
import com.googlecode.clearnlp.morphology.EnglishMPAnalyzer;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.UTOutput;
import com.googlecode.clearnlp.dependency.DEPFeat;
import com.googlecode.clearnlp.dependency.DEPNode;

/**
 *  Class for representing a complete sentence parse. The representation covers a single parse  
 *  including syntactic and semantic dependencies as well as predication and semantic roles
 *  labelling. 
 * 
 *  Two scores per parse are provided, first the original log (-log(p) where p is the 
 *  inside prob. of the parse candidate) from the dry probabilistic parser 
 *  (e.g. Charniak or ClearParser) and second, the log score from a re-ranking step (e.g.
 *  taking into account outside probabilities). 
 * 
 *  n-best parse is then a ranked List of Parse object according to one of these scores.
 *  The class contains a variety of format converter. 
 *
 *  Preferred format is the simple ClearNLP Semantic role format  
 *  (http://code.google.com/p/clearnlp/wiki/DataFormat#Semantic_role_format_(srl))
 *  which includes all the required information.
 *  
 * * @author Patrice Lopez
 */

public class Parse {
    private static final Logger logger = LoggerFactory.getLogger(Parse.class);
    
    private double scoreLogInside = 0.0;
    private double scoreLogInsideOutside = 0.0;
    
    // KISS principle: The representation for a parse is simply the ClearNLP Semantic 
    // role format (http://code.google.com/p/clearnlp/wiki/DataFormat#Semantic_role_format_(srl)) 
    private String tabulatedRep = null;
    
    // more structured representation - map a token position to its syntactic 
    // information as a line of the ClearNLP Semantic role format
    private Map<Integer, String> tokenStructures = null;
    
    // map structure for a token by its index following the ClearNLP Semantic role format
    private Map<String, String> tokenIndex = null;
    
    // map an index to all its child structure indices
    private Map<String,List<String>> childIndex = null;

    // map an index to the start offset of its corresponding token
    private Map<String, Integer> index2OffsetStart = null;

    // map an index to the end offset of its corresponding token
    private Map<String, Integer> index2OffsetEnd = null;

    public Parse() {}
    
    /**
     *  String representation given here must be according to the ClearNLP Semantic 
     *  role format (http://code.google.com/p/clearnlp/wiki/DataFormat#Semantic_role_format_(srl))
     */
    public Parse(String rep, double score1, double score2) {
        tabulatedRep = rep;
        scoreLogInside = score1;
        scoreLogInsideOutside = score2;
    }   
    
    public double getScoreLogInside() {
        return scoreLogInside;
    }
    
    public void setScoreLogInside(double score) {
        scoreLogInside = score;
    }
    
    public double getScoreLogInsideOutside() {
        return scoreLogInside;
    }
    
    public void setScoreLogInsideOutside(double score) {
        scoreLogInsideOutside = score;
    }   
    
    /**
     *  String representation given here must be set according to the ClearNLP Semantic 
     *  role format (http://code.google.com/p/clearnlp/wiki/DataFormat#Semantic_role_format_(srl))
     */
    public void setParseRepresentation(String rep) {
        tabulatedRep = rep;
    }
    
    public void createMap(String text) {
//System.out.println("createMap: " + tabulatedRep + "\n" + text);
        // creating the basic token map
        int position = 0;
        String[] lines = tabulatedRep.split("\n");
        for(int i=0; i<lines.length;i++) {
            if (lines[i].trim().length() == 0) 
                continue;
            String[] pieces = lines[i].split("\t");
            if (pieces.length < 2)
                continue;
            if (pieces[0].trim().length() == 0)
                continue;
            if (pieces[1].trim().length() == 0)
                continue;
            String token = pieces[1].trim();
            // find the start offset of this token in the text
            int endPosition = position;
            int startPosition = text.indexOf(token, endPosition);
            if (startPosition == -1) {
                logger.debug("unmatche token: " + token + " for text: " + text + " from position: " + endPosition);
            }
            else {
                //logger.debug("matched token: " + tokens.get(i) + " at position " + startPosition);
                endPosition = startPosition + token.length();
                if (tokenStructures == null)
                    tokenStructures = new HashMap<Integer, String>();
//System.out.println("put: " + startPosition + "\n" + lines[i]);                
                tokenStructures.put(new Integer(startPosition), lines[i]);
                if (tokenIndex == null)
                    tokenIndex = new HashMap<String, String>();
                tokenIndex.put(pieces[0].trim(), lines[i]);

                // check subtokenization (grobid-quantities tokenizes more than ClearNLP)
                int oldStartPosition = startPosition;
                List<String> subtokens = QuantityAnalyzer.tokenize(token);
                if (subtokens.size()>1) {
                    boolean start = true;
                    for(String subtoken : subtokens) {
                        if (start) {
                            start = false;
                            continue;
                        }
                        startPosition = text.indexOf(subtoken, startPosition);
                        if (tokenStructures == null)
                            tokenStructures = new HashMap<Integer, String>();
                        tokenStructures.put(new Integer(startPosition), lines[i]);
                    }
                }

                startPosition = oldStartPosition;
                // index offset positions in the offset maps
                if (index2OffsetStart == null)
                    index2OffsetStart = new HashMap<String, Integer>();
                index2OffsetStart.put(pieces[0].trim(), new Integer(startPosition));
                if (index2OffsetEnd == null)
                    index2OffsetEnd = new HashMap<String, Integer>();
                index2OffsetEnd.put(pieces[0].trim(), new Integer(endPosition));
                //System.out.println(pieces[0].trim() + " " + startPosition + " " + endPosition + " / " + text.substring(startPosition, endPosition) );
                // parent -> children information
                if (childIndex == null)
                    childIndex = new HashMap<String,List<String>>();
                if (pieces[5].trim().length() != 0) {
                    List<String> children = childIndex.get(pieces[5].trim());
                    if (children == null)
                        children = new ArrayList<String>();
                    if (!children.contains(pieces[0].trim()))
                        children.add(pieces[0].trim());
                    childIndex.put(pieces[5].trim(), children);
                }
            }
            position = endPosition;
        }
    }

    /**
     *  Give the String representation according to the ClearNLP Semantic 
     *  role format (http://code.google.com/p/clearnlp/wiki/DataFormat#Semantic_role_format_(srl))
     */
    public String getParseRepresentation() {
        return tabulatedRep;
    }
    
    /**
     *  Convert a Penn Treebank style constituent tree into the tabulated ClearNLP Semantic role format.
     *  Only English language Treebank style constituent trees are supported. 
     */
    public String convertConstituentToDependency(String rep) {
        AbstractC2DConverter c2d = null;
        AbstractMPAnalyzer morph = null;
        
        String result = null;
        
        String modelPath = "resources/clearnlp/models/";
        String dictionaryFile = modelPath + "/" + "dictionary-1.2.0.zip";  
        String headruleFile = modelPath + "../config/headrule_en_stanford.txt";
        
        c2d = new EnglishC2DConverter(new HeadRuleMap(UTInput.createBufferedFileReader(headruleFile)), null);
        if (dictionaryFile != null) 
            morph = new EnglishMPAnalyzer(dictionaryFile);
        
        CTTree cTree; 
        DEPTree dTree;
        
        // convert String into InputStream
        InputStream is = new ByteArrayInputStream(rep.getBytes());

        // read it with BufferedReader
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        
        CTReader reader = new CTReader(br);
        cTree = reader.nextTree();
        
        if (cTree != null) {
            CTLibEn.preprocessTree(cTree);
            
            dTree = c2d.toDEPTree(cTree);

            if (dTree == null) {
                result = getNullTree()+"\n";
            }
            else {
                if (morph != null)
                    morph.lemmatize(dTree);
                result = dTree.toStringSRL()+"\n";
            }
        }
        return result;
    }
    
    private DEPTree getNullTree() {
        DEPTree tree = new DEPTree();
        
        DEPNode dummy = new DEPNode(1, "NULL", "NULL", "NULL", new DEPFeat());
        dummy.setHead(tree.get(0), "NULL");

        tree.add(dummy);
        tree.initXHeads();
        tree.initSHeads();

        return tree;
    }
    
    /**
     *  The standard tabulated String representation following the ClearNLP Semantic 
     *  role format (http://code.google.com/p/clearnlp/wiki/DataFormat#Semantic_role_format_(srl))
     *  with as prefix the two corresponding log scores.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (scoreLogInside != 0.0) {
            sb.append(scoreLogInside + "\t");
        }
        if (scoreLogInsideOutside != 0.0) {
            sb.append(scoreLogInsideOutside);
        }
        sb.append("\n");
        sb.append(tabulatedRep);
        return sb.toString();
    }
 
    public String getTokenStructureByPosition(int position) {
        if (tokenStructures == null)
            return null;
        return tokenStructures.get(new Integer(position));
    }

    public String getTokenStructureByIndex(String index) {
        if (tokenIndex == null)
            return null;
        if ( (index == null) || (index.length() == 0) )
            return null;
        return tokenIndex.get(index);
    }

    public List<String> getTokenChildStructureByHeadIndex(String index) {
        if (childIndex == null)
            return null;
        if ( (index == null) || (index.length() == 0) )
            return null;
        return childIndex.get(index);
    }

    public int getOffsetStartIndex(String index) {
        if (index2OffsetStart == null)
            return -1;
        if ( (index == null) || (index.length() == 0) )
            return -1;
        if (index2OffsetStart.get(index) != null)
            return index2OffsetStart.get(index).intValue();
        else
            return -1;
    }

    public int getOffsetEndIndex(String index) {
        if (index2OffsetEnd == null)
            return -1;
        if ( (index == null) || (index.length() == 0) )
            return -1;
        if (index2OffsetEnd.get(index) != null)
            return index2OffsetEnd.get(index).intValue();
        else
            return -1;
    }

}