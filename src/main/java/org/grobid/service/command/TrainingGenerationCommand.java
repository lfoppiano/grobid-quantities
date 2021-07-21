package org.grobid.service.command;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.training.QuantityTrainingData;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

public class TrainingGenerationCommand extends ConfiguredCommand<GrobidQuantitiesConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingGenerationCommand.class);
    public final static String INPUT_DIRECTORY = "Input directory";
    public final static String OUTPUT_DIRECTORY = "Output directory";
    public final static String GROBID_HOME_DIRECTORY = "Grobid Home";
    public final static String RECURSIVE = "recursive";


    public TrainingGenerationCommand() {
        super("trainingGeneration", "Generate training data ");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-dIn")
            .dest(INPUT_DIRECTORY)
            .type(Arguments.fileType().verifyCanRead().verifyIsDirectory())
            .required(true)
            .help("Input directory");

        subparser.addArgument("-dOut")
            .dest(OUTPUT_DIRECTORY)
            .type(Arguments.fileType().verifyIsDirectory().verifyCanWrite().or().verifyNotExists().verifyCanCreate())
            .required(true)
            .help("Output directory");

        subparser.addArgument("-gH")
            .dest(GROBID_HOME_DIRECTORY)
            .type(Arguments.fileType().verifyExists().verifyCanRead().verifyIsDirectory())
            .required(false)
            .help("Override the grobid-home directory from the configuration. ");

        subparser.addArgument("-r")
            .dest(RECURSIVE)
            .type(Boolean.class)
            .required(false)
            .setDefault(false)
            .help("Process recursively")
            .action(Arguments.storeTrue());
    }

    @Override
    protected void run(Bootstrap bootstrap, Namespace namespace, GrobidQuantitiesConfiguration configuration) throws Exception {
        File grobidHomeOverride = namespace.get(GROBID_HOME_DIRECTORY);
        String grobidHome = configuration.getGrobidHome();

        initGrobidHome(grobidHome, grobidHomeOverride);

        File inputDirectory = namespace.get(INPUT_DIRECTORY);
        File outputDirectory = namespace.get(OUTPUT_DIRECTORY);
        Boolean recursive = namespace.get(RECURSIVE);

        new QuantityTrainingData().createTrainingBatch(inputDirectory.getAbsolutePath(), outputDirectory.getAbsolutePath(), recursive);
    }

    protected static void initGrobidHome(String configurationGrobidHomeAsString, File commandLineGrobidHome) {
        try {
            File configurationGrobidHome = new File(configurationGrobidHomeAsString);
            if (commandLineGrobidHome != null) {
                configurationGrobidHome = commandLineGrobidHome;
            }

            GrobidProperties.set_GROBID_HOME_PATH(configurationGrobidHome.getAbsolutePath());
            GrobidProperties.setGrobidPropertiesPath(new File(configurationGrobidHome, "/config/grobid.properties").getAbsolutePath());

            GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Collections.singletonList(configurationGrobidHome.getAbsolutePath()));
            GrobidProperties.getInstance(grobidHomeFinder);
            LibraryLoader.load();
        } catch (final Exception exp) {
            System.err.println("Grobid initialisation failed. Maybe you forget to specify the location of the config.yml in the command launch as final argument?");
            LOGGER.debug("Grobid initialisation error. ", exp);

            System.exit(-1);
        }
    }
}