package org.grobid.trainer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import org.apache.commons.io.IOUtils;
import org.grobid.core.engines.QuantitiesModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorQuantities;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.grobid.trainer.sax.MeasureAnnotationSaxHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Patrice Lopez
 */
public class QuantitiesTrainer extends AbstractTrainer {

    private QuantityLexicon quantityLexicon = null;

    public QuantitiesTrainer() {
        super(QuantitiesModels.QUANTITIES);
        // adjusting CRF training parameters for this model
        epsilon = 0.000001;
        window = 20;

        quantityLexicon = QuantityLexicon.getInstance();
    }

    /**
     * Add the selected features to the model training
     */
    public int createCRFPPData(final File corpusDir,
                               final File trainingOutputPath,
                               final File evalOutputPath,
                               double splitRatio) {
        int totalExamples = 0;
        Writer trainingOutputWriter = null;
        Writer evaluationOutputWriter = null;

        try {

            File adaptedCorpusDir = new File(corpusDir.getAbsolutePath());
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
            File[] refFiles = adaptedCorpusDir.listFiles(new FilenameFilter() {
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
                File thefile = refFiles[n];
                name = thefile.getName();
                LOGGER.info(name);

                Writer writer = dispatchExample(trainingOutputWriter, evaluationOutputWriter, splitRatio);

                MeasureAnnotationSaxHandler handler = new MeasureAnnotationSaxHandler();

                //get a new instance of parser
                SAXParser p = spf.newSAXParser();
                p.parse(thefile, handler);

                List<Pair<String, String>> labeled = handler.getLabeledResult();

                // we need to add now the features to the labeled tokens
                List<Pair<String, String>> bufferLabeled = null;
                int pos = 0;

                // let's iterate by defined CRF input (separated by new line)
                while (pos < labeled.size()) {
                    bufferLabeled = new ArrayList<>();
                    while (pos < labeled.size()) {
                        if (labeled.get(pos).getA().equals("\n")) {
                            pos++;
                            break;
                        }
                        bufferLabeled.add(labeled.get(pos));
                        pos++;
                    }

                    if (bufferLabeled.size() == 0)
                        continue;

                    List<OffsetPosition> unitTokenPositions = quantityLexicon.inUnitNamesPairs(bufferLabeled);

                    addFeatures(bufferLabeled, writer, unitTokenPositions);
                    writer.write("\n");
                    writer.flush();
                    writer = dispatchExample(trainingOutputWriter, evaluationOutputWriter, splitRatio);
                }
                writer.write("\n");
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            IOUtils.closeQuietly(evaluationOutputWriter, trainingOutputWriter);
        }
        return totalExamples;
    }

    /**
     * Add the selected features to the model training for bio entities
     */
    public int createCRFPPData(File sourcePathLabel,
                               File outputPath) {

        return createCRFPPData(sourcePathLabel, outputPath, null, 1.0);

    }

    @SuppressWarnings({"UnusedParameters"})
    private void addFeatures(List<Pair<String, String>> texts,
                             Writer writer,
                             List<OffsetPosition> unitTokenPositions) {
        int totalLine = texts.size();
        int posit = 0;
        int currentQuantityIndex = 0;
        List<OffsetPosition> localPositions = unitTokenPositions;
        boolean isUnitPattern = false;
        try {
            for (Pair<String, String> lineP : texts) {
                String token = lineP.getA();
                if (token.trim().equals("@newline")) {
                    writer.write("\n");
                    writer.flush();
                }

                String label = lineP.getB();

                //Unicode normalisation
                token = UnicodeUtil.normaliseTextAndRemoveSpaces(token);

                /*if (label != null) {
                    isUnitPattern = true;
                }*/

                // do we have a unit at position posit?
                if ((localPositions != null) && (localPositions.size() > 0)) {
                    for (int mm = currentQuantityIndex; mm < localPositions.size(); mm++) {
                        if ((posit >= localPositions.get(mm).start) && (posit <= localPositions.get(mm).end)) {
                            isUnitPattern = true;
                            currentQuantityIndex = mm;
                            break;
                        } else if (posit < localPositions.get(mm).start) {
                            isUnitPattern = false;
                            break;
                        } else if (posit > localPositions.get(mm).end) {
                            continue;
                        }
                    }
                }

                FeaturesVectorQuantities featuresVector =
                        FeaturesVectorQuantities.addFeaturesQuantities(token, label,
                                quantityLexicon.inUnitDictionary(token), isUnitPattern, quantityLexicon.isNumberToken(token));
                if (featuresVector.label == null)
                    continue;
                writer.write(featuresVector.printVector());
                writer.write("\n");
                writer.flush();
                posit++;
                isUnitPattern = false;
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
    }

    /**
     * Command line execution. Assuming grobid-home is in ../grobid-home.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        GrobidProperties.getInstance();
        loadConfigurationFromDefaultPath();

        Trainer trainer = new QuantitiesTrainer();
        AbstractTrainer.runTraining(trainer);
    }

    public static void loadConfigurationFromDefaultPath() {
        Path configFilePath = Paths.get("resources", "config", "config.yml");
        ObjectMapper mapper = Jackson.newObjectMapper(new YAMLFactory());
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        try {
            System.out.println("Reading configuration from default location: "+ configFilePath.toAbsolutePath());
            GrobidQuantitiesConfiguration configuration = mapper.readValue(configFilePath.toFile(), GrobidQuantitiesConfiguration.class);
            configuration.getModels().stream().forEach(GrobidProperties::addModel);
        } catch (Exception e) {
            System.out.println("Cannot read configuration. To specify the configuration to a custom path, run the training via the JAR file. See java -jar build/lib/*.onjar.jar --help for information.");
            System.out.println("Exception: " + e);
            System.exit(-1);
        }
    }
}