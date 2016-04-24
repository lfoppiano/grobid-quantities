package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorUnit;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.Pair;
import org.grobid.trainer.evaluation.EvaluationUtilities;
import org.grobid.trainer.sax.UnitAnnotationSaxHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lfoppiano on 21.02.16.
 */
public class UnitTrainer extends AbstractTrainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnitTrainer.class);

    private final QuantityLexicon quantityLexicon;

    public UnitTrainer() {
        super(GrobidModels.UNITS);
        epsilon = 0.0000001;
        window = 20;

        quantityLexicon = QuantityLexicon.getInstance();
    }

    @Override
    public int createCRFPPData(File sourcePathLabel,
                               File outputPath) {
        int totalExamples = 0;
        try {
            LOGGER.info("sourcePathLabel: " + sourcePathLabel);
            LOGGER.info("outputPath: " + outputPath);

            // then we convert the tei files into the usual CRF label format
            // we process all tei files in the output directory
            File[] refFiles = sourcePathLabel.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".tei") || name.toLowerCase().endsWith(".tei.xml");
                }
            });

            if (refFiles == null) {
                return 0;
            }

            LOGGER.info(refFiles.length + " files");

            // the file for writing the training data
            Writer writer = new OutputStreamWriter(new FileOutputStream(outputPath), "UTF8");

            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();

            String name;

            for (int n = 0; n < refFiles.length; n++) {
                File thefile = refFiles[n];
                name = thefile.getName();
                System.out.println(name);

                UnitAnnotationSaxHandler handler = new UnitAnnotationSaxHandler();

                SAXParser p = spf.newSAXParser();
                p.parse(thefile, handler);

                List<UnitLabeled> labeledUnits = handler.getLabeledResult();

                // we need to add now the features to the labeled tokens
                int pos = 0;

                for (UnitLabeled labeledUnit : labeledUnits) {
                    List<Pair<String, String>> labels = labeledUnit.getLabels();
                    List<OffsetPosition> unitTokenPositions = new ArrayList<>();
                    OffsetPosition offsetPosition = new OffsetPosition();
                    offsetPosition.start = pos;
                    offsetPosition.end = pos + 1;
                    unitTokenPositions.add(offsetPosition);

                    addFeatures(labels, writer, unitTokenPositions, labeledUnit.isUnitLeft());
                    writer.write("\n");
                    pos++;
                }
            }

            writer.close();
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return totalExamples;
    }

    @SuppressWarnings({"UnusedParameters"})
    private void addFeatures(List<Pair<String, String>> texts,
                             Writer writer,
                             List<OffsetPosition> unitTokenPositions, boolean isUnitLeft) {

        int posit = 0;
        List<OffsetPosition> localPositions = unitTokenPositions;

        try {
            for (Pair<String, String> text : texts) {
                String token = text.getA();
                if (token.trim().equals("@newline")) {
                    writer.write("\n");
                    writer.flush();
                }

                String label = text.getB();

                FeaturesVectorUnit featuresVector =
                        FeaturesVectorUnit.addFeaturesUnit(token, label,
                                quantityLexicon.inUnitDictionary(token), quantityLexicon.inPrefixDictionary(token),
                                isUnitLeft);

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
    public int createCRFPPData(File file, File file1, File file2, double v) {
        return 0;
    }

    /**
     * Standard evaluation via the the usual Grobid evaluation framework.
     */
    public String evaluate() {
        File evalDataF = GrobidProperties.getInstance().getEvalCorpusPath(
                new File(new File("resources").getAbsolutePath()), model);

        File tmpEvalPath = getTempEvaluationDataPath();
        createCRFPPData(evalDataF, tmpEvalPath);

        return EvaluationUtilities.evaluateStandard(tmpEvalPath.getAbsolutePath(), getTagger());
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        try {
            String pGrobidHome = "../grobid-home";
            String pGrobidProperties = "../grobid-home/config/grobid.properties";

            MockContext.setInitialContext(pGrobidHome, pGrobidProperties);
            GrobidProperties.getInstance();

            Trainer trainer = new UnitTrainer();
            AbstractTrainer.runTraining(trainer);
            AbstractTrainer.runEvaluation(trainer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                MockContext.destroyInitialContext();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
