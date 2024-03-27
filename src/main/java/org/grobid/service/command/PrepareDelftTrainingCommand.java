package org.grobid.service.command;

import io.dropwizard.core.cli.ConfiguredCommand;
import io.dropwizard.core.setup.Bootstrap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.grobid.core.GrobidModel;
import org.grobid.core.engines.QuantitiesModels;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.grobid.trainer.AbstractTrainer;
import org.grobid.trainer.QuantitiesTrainer;
import org.grobid.trainer.UnitTrainer;
import org.grobid.trainer.ValueTrainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Borrowed from https://github.com/lfoppiano/grobid-superconductors 
 */
public class PrepareDelftTrainingCommand extends ConfiguredCommand<GrobidQuantitiesConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareDelftTrainingCommand.class);

    private final static String MODEL_NAME = "model";
    private final static String DELFT_PATH = "delft_path";
    private final static String OUTPUT_PATH = "output_path";
    private final static String INPUT_PATH = "input_path";

    public PrepareDelftTrainingCommand() {
        super("prepare-delft-training", "Prepare training data for Delft.");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        MutuallyExclusiveGroup mutexGroup = subparser.addMutuallyExclusiveGroup("output");
        mutexGroup.required(true);

        mutexGroup
            .addArgument("-d", "--delft")
            .dest(DELFT_PATH)
            .type(Arguments.fileType().verifyCanRead())
            .help("Location of delft (the root directory is enough. If provided a value, the data will be saved in data/sequenceLabelling/grobid/{model_name}/{model_name}-{date}.train, else will be saved as {model_name}.train in the local directory. ");

        mutexGroup
            .addArgument("-o", "--output")
            .dest(OUTPUT_PATH)
            .type(Arguments.fileType().verifyNotExists().verifyCanCreate().or().verifyIsDirectory().verifyCanWrite())
            .help("Output path directory. ");

        subparser.addMutuallyExclusiveGroup()
            .addArgument("-i", "--input")
            .dest(INPUT_PATH)
            .type(Arguments.fileType().verifyCanRead().verifyIsDirectory())
            .required(false)
            .help("Input path directory. ");

        subparser.addMutuallyExclusiveGroup()
            .addArgument("-m", "--model")
            .dest(MODEL_NAME)
            .type(String.class)
            .choices(Arrays.asList("quantities",
                "units",
                "values"))
            .required(false)
            .setDefault("quantities")
            .help("Model data to use. ");

    }

    @Override
    protected void run(Bootstrap<GrobidQuantitiesConfiguration> bootstrap, Namespace namespace, GrobidQuantitiesConfiguration configuration) throws Exception {
        try {
            GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList(configuration.getGrobidHome()));
            GrobidProperties.getInstance(grobidHomeFinder);
            configuration.getModels().stream().forEach(GrobidProperties::addModel);
            LibraryLoader.load();
        } catch (final Exception exp) {
            System.err.println("Grobid initialisation failed, cannot find Grobid Home. Maybe you forget to specify the config.yml in the command launch?");
            System.err.println("Grobid initialisation error. " + exp);
            System.exit(-1);
        }

        File inputPath = namespace.get(INPUT_PATH);
        File delftPath = namespace.get(DELFT_PATH);
        File outputPath = namespace.get(OUTPUT_PATH);
        String modelName = namespace.get(MODEL_NAME);

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");

        GrobidModel model = QuantitiesModels.QUANTITIES;
        AbstractTrainer trainer = new QuantitiesTrainer();

        if (QuantitiesModels.UNITS.getModelName().equals(modelName)) {
            model = QuantitiesModels.UNITS;
            trainer = new UnitTrainer();
        } else if (QuantitiesModels.VALUES.getModelName().equals(modelName)) {
            model = QuantitiesModels.VALUES;
            trainer = new ValueTrainer();
        }

        String filename = File.separator + modelName + "-" + formatter.format(date) + ".train";
        if (inputPath != null && !inputPath.isDirectory()) {
            filename = File.separator + modelName + "-" + inputPath.getName().replaceAll(".tei.xml", "") + ".train";
        }

        Path destination = null;
        if (outputPath != null && Files.exists(Paths.get(outputPath.getAbsolutePath()))) {
            destination = Paths.get(outputPath.getAbsolutePath(), filename);
        } else if (delftPath != null && Files.exists(Paths.get(delftPath.getAbsolutePath()))) {
            destination = Paths.get(delftPath.getAbsolutePath(), "data", "sequenceLabelling",
                "grobid", modelName, filename);
        } else {
            System.out.println("Output or delft directory do not exists. ");
            System.exit(-1);
        }

        if (inputPath == null) {
            inputPath = GrobidProperties.getCorpusPath(new File("/"), model);
            System.out.println("Input directory was not provided, getting the training data from " + inputPath.getAbsolutePath());
        }
        trainer.createCRFPPData(inputPath, destination.toFile());

        System.out.println("Writing training data for delft to " + destination);
    }
}
