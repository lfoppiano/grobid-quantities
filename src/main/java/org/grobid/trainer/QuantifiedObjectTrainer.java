package org.grobid.trainer;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.stax2.XMLStreamReader2;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorQuantifiedObjects;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.trainer.stax.QuantifiedObjectAnnotationStaxHandler;
import org.grobid.trainer.stax.StaxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.grobid.core.engines.QuantitiesModels.QUANTIFIED_OBJECT;

public class QuantifiedObjectTrainer extends AbstractTrainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuantifiedObjectTrainer.class);

    private WstxInputFactory inputFactory = new WstxInputFactory();

    public QuantifiedObjectTrainer() {
        super(QUANTIFIED_OBJECT);
        epsilon = 0.0000001;
        window = 20;
    }

    @Override
    public int createCRFPPData(File sourcePathLabel,
                               File outputPath) {
        return createCRFPPData(sourcePathLabel, outputPath, null, 1.0);
    }

    @SuppressWarnings({"UnusedParameters"})
    private void addFeatures(List<Pair<String, String>> texts, Writer writer) {

        int posit = 0;
        // PL: to be reviewed, unitTokenPositions not used !
        try {
            for (Pair<String, String> text : texts) {
                String token = text.getKey();

                //Unicode normalisation
                token = UnicodeUtil.normaliseTextAndRemoveSpaces(token);

                String label = text.getValue();
                if (token.trim().equals("@newline") || isBlank(label)) {
                    writer.write("\n");
                    writer.flush();
                    continue;
                }

                // If there is a measure, we use the information as additional feature
                boolean measure = false;
                if (label.contains("measure")) {
                    label = "<other>";
                    measure = true;
                }

                FeaturesVectorQuantifiedObjects featuresVector =
                        FeaturesVectorQuantifiedObjects.addFeatures(token, label, measure);

                if (featuresVector.label == null) {
                    continue;
                }

                writer.write(featuresVector.printVector());
                writer.write("\n");
                writer.flush();

                posit++;
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    @Override
    public int createCRFPPData(File corpusDir, File trainingOutputPath, File evalOutputPath, double splitRatio) {
        int totalExamples = 0;
        Writer trainingOutputWriter = null;
        Writer evaluationOutputWriter = null;

        try {
            File adaptedCorpusDir = new File(corpusDir.getAbsolutePath() + File.separator + "staging");

            LOGGER.info("sourcePathLabel: " + adaptedCorpusDir);
            if (trainingOutputPath != null)
                LOGGER.info("outputPath for training data: " + trainingOutputPath);
            if (evalOutputPath != null)
                LOGGER.info("outputPath for evaluation data: " + evalOutputPath);

            // the file for writing the training data
            OutputStream os2 = null;

            if (trainingOutputPath != null) {
                os2 = new FileOutputStream(trainingOutputPath);
                trainingOutputWriter = new OutputStreamWriter(os2, UTF_8);
            }

            // the file for writing the evaluation data
            OutputStream os3 = null;

            if (evalOutputPath != null) {
                os3 = new FileOutputStream(evalOutputPath);
                evaluationOutputWriter = new OutputStreamWriter(os3, UTF_8);
            }

            // then we convert the tei files into the usual CRF label format
            // we process all tei files in the output directory
            File[] refFiles = adaptedCorpusDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".tei") || name.toLowerCase().endsWith(".tei.xml"));

            if (refFiles == null) {
                return 0;
            }

            LOGGER.info(refFiles.length + " files");

            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();

            String name;

            for (int n = 0; n < refFiles.length; n++) {
                Writer writer = dispatchExample(trainingOutputWriter, evaluationOutputWriter, splitRatio);

                File inputFile = refFiles[n];
                name = inputFile.getName();
                LOGGER.info(name);

                QuantifiedObjectAnnotationStaxHandler parser = new QuantifiedObjectAnnotationStaxHandler();
                XMLStreamReader2 reader = inputFactory.createXMLStreamReader(inputFile);

                StaxUtils.traverse(reader, parser);

                List<Pair<String, String>> labels = parser.getLabeled();

                addFeatures(labels, writer);
                writer.write("\n");
            }


        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        } finally {
            IOUtils.closeQuietly(evaluationOutputWriter, trainingOutputWriter);
        }
        return totalExamples;
    }

    /**
     * Dispatch the example to the training or test data, based on the split ration and the drawing of
     * a random number
     */
    private Writer dispatchExample(Writer writerTraining, Writer writerEvaluation, double splitRatio) {
        Writer writer = null;
        if ((writerTraining == null) && (writerEvaluation != null)) {
            writer = writerEvaluation;
        } else if ((writerTraining != null) && (writerEvaluation == null)) {
            writer = writerTraining;
        } else {
            if (Math.random() <= splitRatio)
                writer = writerTraining;
            else
                writer = writerEvaluation;
        }
        return writer;
    }

    /**
     * Command line execution. Assuming grobid-home is in ../grobid-home
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        GrobidProperties.getInstance();

        Trainer trainer = new QuantifiedObjectTrainer();
        AbstractTrainer.runSplitTrainingEvaluation(trainer, 0.5);
    }
}
