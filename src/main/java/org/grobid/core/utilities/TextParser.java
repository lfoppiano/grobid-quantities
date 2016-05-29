package org.grobid.core.utilities;

import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.OffsetPosition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
  
import com.googlecode.clearnlp.component.srl.CRolesetClassifier;
import com.googlecode.clearnlp.dependency.DEPParser;
import com.googlecode.clearnlp.dependency.AbstractDEPParser;
import com.googlecode.clearnlp.predicate.AbstractPredIdentifier;
import com.googlecode.clearnlp.dependency.srl.AbstractSRLabeler;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.engine.EngineProcess;
import com.googlecode.clearnlp.morphology.AbstractMPAnalyzer;
import com.googlecode.clearnlp.pos.POSTagger;
import com.googlecode.clearnlp.reader.AbstractReader;
import com.googlecode.clearnlp.segmentation.AbstractSegmenter;
import com.googlecode.clearnlp.tokenization.AbstractTokenizer;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.pair.Pair;
import com.googlecode.clearnlp.reader.DEPReader;
     
import org.apache.commons.lang3.StringUtils;

// this is for version 1.3.0 of ClearNLP
import com.googlecode.clearnlp.nlp.NLPLib;

/**
 * Text parsing with ClearNPL, only the first best parse is provided. 
 * Only English is supported.
 * 
 * @author Patrice Lopez
 *
 */
public class TextParser {
    private static final Logger logger = LoggerFactory.getLogger(TextParser.class);
    
    public final String language = AbstractReader.LANG_EN;
    
    private static volatile TextParser instance;
    
    private String modelPath = null;
    
    // these are the ClearParser components
    private AbstractTokenizer tokenizer = null;
    private AbstractMPAnalyzer analyzer = null;
    private Pair<POSTagger[],Double> taggers = null;
    private AbstractDEPParser parser = null;
    private AbstractPredIdentifier predicater = null;
    private AbstractSRLabeler labeler = null;
    private DEPReader depReader = null;
    
    // this is for version 1.3.0 of ClearNLP
    private CRolesetClassifier roleClassifier = null;

    public static TextParser getInstance() throws Exception {
        if (instance == null) {
            //double check idiom
            // synchronized (instanceController) {
                if (instance == null)
                    getNewInstance();
            // }
        }
        return instance;
    }

    /**
     * Creates a new instance.
     */
    private static synchronized void getNewInstance() throws Exception {
        logger.debug("Get new instance of TextParser");
        instance = new TextParser();
    }
    
    /**
     * Hidden constructor
     */
    private TextParser() throws Exception { 
        modelPath = "resources/clearnlp/models/";
         
        // TBD: set up config profile for the ClearNLP models...
        String dictionaryFile = modelPath + "/" + "dictionary-1.2.0.zip";  
        //String posModelFile = modelPath + "/" + "mayo-en-pos-1.3.0.tgz";    
        //String posModelFile = modelPath + "/" + "ontonotes-en-pos-1.3.0.tgz"; 
        //String posModelFile = modelPath + "/" + "medical-en-pos-1.1.0g.jar";   
        String posModelFile = modelPath + "/" + "craft-en-pos-1.1.0g.jar"; 
        //String depModelFile = modelPath + "/" + "mayo-en-dep-1.3.0.tgz";
        String depModelFile = modelPath + "/" + "craft-en-dep-1.1.0b1.jar";
        //String depModelFile = modelPath + "/" + "ontonotes-en-dep-1.1.0b3.jar";
        //String predModelFile = modelPath + "/" + "mayo-en-pred-1.3.0.tgz";  
        String predModelFile = modelPath + "/" + "medical-en-pred-1.2.0.jar";   
        //String labelModelFile = modelPath + "/" + "mayo-en-srl-1.3.0.tgz"; 
        String labelModelFile = modelPath + "/" + "medical-en-srl-1.2.0b1.jar"; 
        String roleClassifierModelFile = modelPath + "/" + "mayo-en-role-1.3.0.tgz";  
        //String roleClassifierModelFile = modelPath + "/" + "ontonotes-en-roleset-1.2.2.tgz"; 
        
        tokenizer = EngineGetter.getTokenizer(language, dictionaryFile);
        analyzer = EngineGetter.getMPAnalyzer(language, dictionaryFile);
        taggers = EngineGetter.getPOSTaggers(posModelFile);
        parser = EngineGetter.getDEPParser(depModelFile);
        predicater = EngineGetter.getPredIdentifier(predModelFile);
        labeler = EngineGetter.getSRLabeler(labelModelFile);
        
        // this is for version 1.3.0 of ClearNLP   
        FileInputStream is = new FileInputStream(new File(roleClassifierModelFile));
        roleClassifier = (CRolesetClassifier)EngineGetter.getComponent(is, AbstractReader.LANG_EN, NLPLib.MODE_ROLE);
        
        depReader = new DEPReader(0, 1, 2, 3, 4, 5, 6);    
    }
    
    /**
     * Hidden constructor
     */
    private TextParser(String dictionaryFile, String posModelFile, String depModelFile, 
                String predModelFile, String labelModelFile, String roleClassifierModelFile) 
            throws Exception {  
        tokenizer = EngineGetter.getTokenizer(language, dictionaryFile);
        analyzer = EngineGetter.getMPAnalyzer(language, dictionaryFile);
        taggers = EngineGetter.getPOSTaggers(posModelFile);
        parser = EngineGetter.getDEPParser(depModelFile);
        predicater = EngineGetter.getPredIdentifier(predModelFile);
        labeler = EngineGetter.getSRLabeler(labelModelFile);
        
        // this is for version 1.3.0 of ClearNLP
        FileInputStream is = new FileInputStream(new File(roleClassifierModelFile));
        roleClassifier = (CRolesetClassifier)EngineGetter.getComponent(is, AbstractReader.LANG_EN, NLPLib.MODE_ROLE);
        
        depReader = new DEPReader(0, 1, 2, 3, 4, 5, 6);  
    }
    
    /**
     * Parsing of a sentence. 
     *
     * @param sentence 
     *            the sentence to be parsed
     * @return 
     *         the parsed sentence (including predicate identification and semantic role labeling) 
     *         as the n-best list of Parse object. If the CLEAR_PARSER is selected, only the 
     *         best parse is provided.  
     */
    public ProcessedSentence parse(String sentence) throws GrobidException {
        if (sentence == null) {
            throw new GrobidException("Sentence to be parsed is null.");
        }
        else if (sentence.length() == 0) {
            throw new GrobidException("Sentence to be parsed has a length of 0.");
        }
        
        ProcessedSentence result = null;
        
        DEPTree tree = EngineProcess.getDEPTree(tokenizer, taggers, 
            //analyzer, parser, predicater, labeler, sentence);  
            analyzer, parser, sentence);
        EngineProcess.predictSRL(predicater, roleClassifier, labeler, tree);                        
        // we only have the top parse with the ClearParser, no n-best ! and no score.
        Parse parse = new Parse();
        parse.setParseRepresentation(tree.toStringSRL());
        parse.createMap(sentence);
        List<Parse> theResult = new ArrayList<Parse>();
        theResult.add(parse);
        result = new ProcessedSentence(sentence, theResult, new OffsetPosition(0,sentence.length())); 
        
        return result;  
    }
    
    /**
     * Parsing of some raw text. 
     *
     * @param text 
     *            the raw text to be parsed
     * @return 
     *         the list of parses - one per sentence - (including predicate identification and 
     *         semantic role labeling) as the n-best list of Parse object. If the CLEAR_PARSER is 
     *         selected, only the best parse is provided in the list.
     */
    public List<ProcessedSentence> parseText(String text) throws GrobidException {
        if (text == null) {
            throw new GrobidException("Cannot parse the sentence, because it is null.");
        }
        else if (text.length() == 0) {
            System.out.println("The length of the text to be parsed is 0.");
            logger.error("The length of the text to be parsed is 0.");
            return null;
        }
        
        List<ProcessedSentence> results = null;
        
        AbstractSegmenter segmenter = EngineGetter.getSegmenter(language, tokenizer);
        
        // convert String into InputStream
        InputStream is = new ByteArrayInputStream(text.getBytes());

        // read it with BufferedReader
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<List<String>> sentences = segmenter.getSentences(br);
        
        if ( (sentences == null) || (sentences.size() == 0) ) {
            // there is some text but not in a state so that a sentence at least can be
            // identified by the sentence segmenter, so we parse it as a single sentence
            ProcessedSentence pack = parse(text);
            //ProcessedSentence pack = new ProcessedSentence(text, null, null, theResult);
            results = new ArrayList<ProcessedSentence>(); 
            results.add(pack);
            return results; 
        }
        
        results = new ArrayList<ProcessedSentence>();
        int position = 0;
        for (List<String> tokens : sentences) {
            //DEPTree tree = EngineProcess.getDEPTree(taggers, analyzer, parser, predicater, labeler, tokens);                  
            DEPTree tree = EngineProcess.getDEPTree(taggers, analyzer, parser, tokens);                     
            EngineProcess.predictSRL(predicater, roleClassifier, labeler, tree);                
            // we only have the top parse with the ClearParser, no n-best !
            Parse parse = new Parse();
            parse.setParseRepresentation(tree.toStringSRL());    
//System.out.println(tree.toStringSRL());
            List<Parse> parses = new ArrayList<Parse>();
            parses.add(parse); 
            // To be reviewed! we want offsets, not the modified sentence provided by clearnlp
            int endPosition = position;
            for(int i=0; i<tokens.size(); i++) {
                int startPosition = text.indexOf(tokens.get(i), endPosition);
                if (i == 0)
                    position = startPosition;
                if (startPosition == -1) {
                    logger.debug("unmatche token: " + tokens.get(i) + " for text: " + text + " from position: " + endPosition);
                }
                else {
                    //logger.debug("matched token: " + tokens.get(i) + " at position " + startPosition);
                    endPosition = startPosition + tokens.get(i).length();
                }
            }
            parse.createMap(text.substring(position, endPosition));
            ProcessedSentence pack = new ProcessedSentence(text.substring(position, endPosition), parses, new OffsetPosition(position, endPosition));
            position = endPosition;
            results.add(pack);
        }
        
        return results;
    }

    /**
     * Parsing text coming from a BufferedReader object. 
     *
     * @param reader 
     *            BufferedReader (to a file) containing the text to be parsed
     * @return 
     *         the list of parses - one per sentence - in the ClearNLP Semantic role format  
     * (http://code.google.com/p/clearnlp/wiki/DataFormat#Semantic_role_format_(srl)).
     */
    public List<ProcessedSentence> parse(BufferedReader reader) throws GrobidException {
        List<ProcessedSentence> results = null;
        
        AbstractSegmenter segmenter = EngineGetter.getSegmenter(language, tokenizer);
        List<List<String>> sentences = segmenter.getSentences(reader);
    
        results = new ArrayList<ProcessedSentence>();
        for (List<String> tokens : sentences) {         
            //DEPTree tree = EngineProcess.getDEPTree(taggers, analyzer, parser, predicater, labeler, tokens);
            DEPTree tree = EngineProcess.getDEPTree(taggers, analyzer, parser, tokens);                     
            EngineProcess.predictSRL(predicater, roleClassifier, labeler, tree);
            // we only have the top parse with the ClearParser, no n-best !
            Parse parse = new Parse();
            parse.setParseRepresentation(tree.toStringSRL());
            parse.createMap(StringUtils.join(tokens," "));
            List<Parse> parses = new ArrayList<Parse>();
            parses.add(parse); 
            // To be reviewed! this is not exactly the original sentence, but not so important for the moment
            ProcessedSentence pack = new ProcessedSentence(StringUtils.join(tokens," "), parses, null);
            results.add(pack);
        }
    
        return results;
    }

    /**
     * Parsing from a file. 
     *
     * @param inputFile 
     *            path to the file containing the text to be parsed
     * @return 
     *         the list of parses - one per sentence - in the ClearNLP Semantic role format  
     * (http://code.google.com/p/clearnlp/wiki/DataFormat#Semantic_role_format_(srl)).
     */
    public List<ProcessedSentence> parseFile(String inputFile) throws GrobidException { 
        return parse(UTInput.createBufferedFileReader(inputFile));
    }
}