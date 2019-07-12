package org.grobid.trainer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.engines.QuantitiesModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorValues;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.trainer.sax.ValueAnnotationSaxHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by lfoppiano on 21.02.16.
 */
public class ValueTrainer extends AbstractTrainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueTrainer.class);

    public ValueTrainer() {
        super(QuantitiesModels.VALUES);
        epsilon = 0.0000001;
        window = 20;
    }
    
    @Override
    public int createCRFPPData(File sourcePathLabel,
                               File outputPath) {
        return createCRFPPData(sourcePathLabel, outputPath, null, 1.0);
    }

    @SuppressWarnings({"UnusedParameters"})
    private void addFeatures(List<Pair<String, String>> texts,
                             Writer writer) {

        int posit = 0;

        try {
            for (Pair<String, String> text : texts) {
                String token = text.getLeft();
                if (token.trim().equals("@newline")) {
                    writer.write("\n");
                    writer.flush();
                }

                //Unicode normalisation
                token = UnicodeUtil.normaliseTextAndRemoveSpaces(token);

                String label = text.getRight();

                FeaturesVectorValues featuresVector =
                        FeaturesVectorValues.addFeatures(token, label);

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
            LOGGER.info("sourcePathLabel: " + corpusDir);
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
            File[] refFiles = corpusDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".tei") || name.toLowerCase().endsWith(".tei.xml");
                }
            });

            if (refFiles == null) {
                return 0;
            }

            LOGGER.info(refFiles.length + " files");

            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();

            String name;

            for (int n = 0; n < refFiles.length; n++) {
                Writer writer = dispatchExample(trainingOutputWriter, evaluationOutputWriter, splitRatio);

                File thefile = refFiles[n];
                name = thefile.getName();
                LOGGER.info(name);

                ValueAnnotationSaxHandler handler = new ValueAnnotationSaxHandler();

                SAXParser p = spf.newSAXParser();
                p.parse(thefile, handler);

                List<ValueLabeled> labeledUnits = handler.getLabeledResult();

                // we need to add now the features to the labeled tokens
                int pos = 0;

                for (ValueLabeled labeledUnit : labeledUnits) {
                    List<Pair<String, String>> labels = labeledUnit.getLabels();
                    List<OffsetPosition> unitTokenPositions = new ArrayList<>();
                    OffsetPosition offsetPosition = new OffsetPosition();
                    offsetPosition.start = pos;
                    offsetPosition.end = pos + 1;
                    unitTokenPositions.add(offsetPosition);

                    addFeatures(labels, writer);
                    writer.write("\n");
                    writer = dispatchExample(trainingOutputWriter, evaluationOutputWriter, splitRatio);
                    pos++;
                }
            }


        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            IOUtils.closeQuietly(evaluationOutputWriter, trainingOutputWriter);
        }
        return totalExamples;
    }

    /**
     * Command line execution. Assuming grobid-home is in ../grobid-home
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        GrobidProperties.getInstance();

        Trainer trainer = new ValueTrainer();
        System.out.println(AbstractTrainer.runNFoldEvaluation(trainer, 10));
        AbstractTrainer.runTraining(trainer);
    }
}
