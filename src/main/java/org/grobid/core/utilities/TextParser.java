package org.grobid.core.utilities;

import com.googlecode.clearnlp.component.srl.CRolesetClassifier;
import com.googlecode.clearnlp.dependency.AbstractDEPParser;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.dependency.srl.AbstractSRLabeler;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.engine.EngineProcess;
import com.googlecode.clearnlp.morphology.AbstractMPAnalyzer;
import com.googlecode.clearnlp.nlp.NLPLib;
import com.googlecode.clearnlp.pos.POSTagger;
import com.googlecode.clearnlp.predicate.AbstractPredIdentifier;
import com.googlecode.clearnlp.reader.AbstractReader;
import com.googlecode.clearnlp.reader.DEPReader;
import com.googlecode.clearnlp.tokenization.AbstractTokenizer;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.pair.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.data.Sentence;
import org.grobid.core.data.SentenceParse;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.lang.SentenceDetector;
import org.grobid.core.lang.impl.OpenNLPSentenceDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// this is for version 1.3.0 of ClearNLP

/**
 * Text parsing with ClearNPL, only the first best parse is provided.
 * Only English is supported.
 *
 * @author Patrice Lopez
 */
public class TextParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextParser.class);

    public final String language = AbstractReader.LANG_EN;

    private static volatile TextParser instance;

    private String modelPath = null;

    // these are the ClearParser components
    private AbstractTokenizer tokenizer = null;
    private AbstractMPAnalyzer analyzer = null;
    private Pair<POSTagger[], Double> taggers = null;
    private AbstractDEPParser parser = null;
    private AbstractPredIdentifier predicater = null;
    private AbstractSRLabeler labeler = null;
    private DEPReader depReader = null;
    private SentenceUtilities segmenter;

    // this is for version 1.3.0 of ClearNLP
    private CRolesetClassifier roleClassifier = null;

    public static TextParser getInstance() throws Exception {
        if (instance == null)
            getNewInstance();
        return instance;
    }

    private static synchronized void getNewInstance() throws Exception {
        LOGGER.debug("Get new instance of TextParser");
        instance = new TextParser();
    }

    /**
     * Hidden constructor
     */
    private TextParser() throws Exception {
        modelPath = Paths.get("resources/clearnlp/models").toAbsolutePath().toString();

        // TBD: set up config profile for the ClearNLP models...
        String dictionaryFile = modelPath + File.separator + "dictionary-1.2.0.zip";
        //String posModelFile = modelPath + File.separator + "mayo-en-pos-1.3.0.tgz";
        //String posModelFile = modelPath + File.separator + "ontonotes-en-pos-1.3.0.tgz";
        //String posModelFile = modelPath + File.separator + "medical-en-pos-1.1.0g.jar";
        String posModelFile = modelPath + File.separator + "craft-en-pos-1.1.0g.jar";
        //String depModelFile = modelPath + File.separator + "mayo-en-dep-1.3.0.tgz";
        String depModelFile = modelPath + File.separator + "craft-en-dep-1.1.0b1.jar";
        //String depModelFile = modelPath + "/" + "ontonotes-en-dep-1.1.0b3.jar";
        //String predModelFile = modelPath + "/" + "mayo-en-pred-1.3.0.tgz";  
        String predModelFile = modelPath + File.separator + "medical-en-pred-1.2.0.jar";
        //String labelModelFile = modelPath + "/" + "mayo-en-srl-1.3.0.tgz"; 
        String labelModelFile = modelPath + File.separator + "medical-en-srl-1.2.0b1.jar";
        String roleClassifierModelFile = modelPath + File.separator + "mayo-en-role-1.3.0.tgz";
        //String roleClassifierModelFile = modelPath + "/" + "ontonotes-en-roleset-1.2.2.tgz"; 

        this.init(dictionaryFile, posModelFile, depModelFile, predModelFile, labelModelFile, roleClassifierModelFile);
    }

    private TextParser(String dictionaryFile, String posModelFile, String depModelFile,
                       String predModelFile, String labelModelFile, String roleClassifierModelFile) throws Exception {
        this.init(dictionaryFile, posModelFile, depModelFile, predModelFile, labelModelFile, roleClassifierModelFile);
    }


    private void init(String dictionaryFile, String posModelFile, String depModelFile,
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
        roleClassifier = (CRolesetClassifier) EngineGetter.getComponent(is, AbstractReader.LANG_EN, NLPLib.MODE_ROLE);

        depReader = new DEPReader(0, 1, 2, 3, 4, 5, 6);

        segmenter = SentenceUtilities.getInstance();
    }

    /**
     * Parsing of a sentence.
     *
     * @param sentence the sentence to be parsed
     * @return the parsed sentence (including predicate identification and semantic role labeling)
     * as the n-best list of Parse object. If the CLEAR_PARSER is selected, only the
     * best parse is provided.
     */
    public synchronized Sentence parse(String sentence) throws GrobidException {
        if (sentence == null) {
            throw new GrobidException("Sentence to be parsed is null.");
        } else if (sentence.length() == 0) {
            throw new GrobidException("Sentence to be parsed has a length of 0.");
        }

        Sentence result = null;

        List<SentenceParse> theResult = getSentenceParses(sentence);
        result = new Sentence(sentence, theResult, new OffsetPosition(0, sentence.length()));

        return result;
    }

    private List<SentenceParse> getSentenceParses(String sentence) {
        DEPTree tree = EngineProcess.getDEPTree(tokenizer, taggers,
            //analyzer, parser, predicater, labeler, sentence);
            analyzer, parser, sentence);
        EngineProcess.predictSRL(predicater, roleClassifier, labeler, tree);
        // we only have the top parse with the ClearParser, no n-best ! and no score.
        SentenceParse parse = new SentenceParse();
        parse.setParseRepresentation(tree.toStringSRL());
        parse.createMap(sentence);
        List<SentenceParse> theResult = new ArrayList<>();
        theResult.add(parse);
        return theResult;
    }

    /**
     * Parsing of some raw text.
     *
     * @param text the raw text to be parsed
     * @param measurementOffsets cancel the segmentation if sentence boundaries are falling on a measurement 
     *              
     * @return the list of parses - one per sentence - (including predicate identification and
     * semantic role labeling) as the n-best list of Parse object. If the CLEAR_PARSER is
     * selected, only the best parse is provided in the list.
     */
    public synchronized List<Sentence> parseText(String text, List<OffsetPosition> measurementOffsets) throws GrobidException {
        if (text == null) {
            throw new GrobidException("Cannot parse the sentence, because it is null.");
        } else if (StringUtils.isEmpty(text)) {
            LOGGER.error("The length of the text to be parsed is 0.");
            return null;
        }

        List<Sentence> results = new ArrayList<>();
        List<OffsetPosition> sentences = this.segmenter.runSentenceDetection(text, measurementOffsets);

        if (CollectionUtils.isEmpty(sentences)) {
            // there is some text but not in a state so that a sentence at least can be
            // identified by the sentence segmenter, so we parse it as a single sentence
            Sentence pack = parse(text);
            //ProcessedSentence pack = new ProcessedSentence(text, null, null, theResult);
            results.add(pack);
            return results;
        }

        for (OffsetPosition sentencePosition : sentences) {
            String sentence = text.substring(sentencePosition.start, sentencePosition.end);
            //DEPTree tree = EngineProcess.getDEPTree(taggers, analyzer, parser, predicater, labeler, tokens);                  
            DEPTree tree = EngineProcess.getDEPTree(tokenizer, taggers, analyzer, parser, sentence);
            EngineProcess.predictSRL(predicater, roleClassifier, labeler, tree);
            // we only have the top parse with the ClearParser, no n-best !
            SentenceParse parse = new SentenceParse();
            parse.setParseRepresentation(tree.toStringSRL());
            //System.out.println(tree.toStringSRL());
            List<SentenceParse> parses = new ArrayList<>();
            parses.add(parse);
            parse.createMap(sentence);
            Sentence pack = new Sentence(sentence, parses, sentencePosition);
            results.add(pack);
        }

        return results;
    }

    /**
     * Parsing text coming from a BufferedReader object.
     *
     * @param reader BufferedReader (to a file) containing the text to be parsed
     * @return the list of parses - one per sentence - in the ClearNLP Semantic role format
     * (http://code.google.com/p/clearnlp/wiki/DataFormat#Semantic_role_format_(srl)).
     */
    public List<Sentence> parse(BufferedReader reader) throws GrobidException {
        List<Sentence> results = new ArrayList<>();

        String text = reader.lines().collect(Collectors.joining());

        List<OffsetPosition> sentences = segmenter.runSentenceDetection(text);

        for (OffsetPosition sentencePosition : sentences) {
            String sentence = text.substring(sentencePosition.start, sentencePosition.end);
            //DEPTree tree = EngineProcess.getDEPTree(taggers, analyzer, parser, predicater, labeler, tokens);
            List<SentenceParse> parses = getSentenceParses(sentence);
            // To be reviewed! this is not exactly the original sentence, but not so important for the moment
            Sentence pack = new Sentence(sentence, parses, null);
            results.add(pack);
        }

        return results;
    }

    /**
     * Parsing from a file.
     *
     * @param inputFile path to the file containing the text to be parsed
     * @return the list of parses - one per sentence - in the ClearNLP Semantic role format
     * (http://code.google.com/p/clearnlp/wiki/DataFormat#Semantic_role_format_(srl)).
     */
    public List<Sentence> parseFile(String inputFile) throws GrobidException {
        return parse(UTInput.createBufferedFileReader(inputFile));
    }
}