package org.grobid.service.command;

import io.dropwizard.core.cli.ConfiguredCommand;
import io.dropwizard.core.setup.Bootstrap;
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
import java.util.Collections;

public class TrainingGenerationCommand extends ConfiguredCommand<GrobidQuantitiesConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingGenerationCommand.class);
    public final static String INPUT_DIRECTORY = "Input directory";
    public final static String OUTPUT_DIRECTORY = "Output directory";
    public final static String GROBID_HOME_DIRECTORY = "Grobid Home";
    public final static String RECURSIVE = "recursive";
    private final static String MODEL_NAME = "model";


    public TrainingGenerationCommand() {
        super("create-training", "Generate training data ");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-dIn", "--input", "-i")
            .dest(INPUT_DIRECTORY)
            .type(Arguments.fileType().verifyCanRead().verifyIsDirectory())
            .required(true)
            .help("Input directory");

        subparser.addArgument("-dOut", "--output", "-o")
            .dest(OUTPUT_DIRECTORY)
            .type(Arguments.fileType()
                .verifyNotExists().verifyCanCreate()
                .or()
                .verifyIsDirectory().verifyCanWrite())
            .required(true)
            .help("Output directory");

        subparser.addArgument("-gH", "--grobid-home")
            .dest(GROBID_HOME_DIRECTORY)
            .type(Arguments.fileType()
                .verifyExists()
                .verifyCanRead()
                .verifyIsDirectory())
            .required(false)
            .help("Override the grobid-home directory from the configuration. ");

        subparser.addArgument("-r", "--recursive")
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

        initGrobidHome(configuration.getGrobidHome(), grobidHomeOverride);
        configuration.getModels().stream().forEach(GrobidProperties::addModel);

        File inputDirectory = namespace.get(INPUT_DIRECTORY);
        File outputDirectory = namespace.get(OUTPUT_DIRECTORY);
        Boolean recursive = namespace.get(RECURSIVE);

        new QuantityTrainingData().createTrainingBatch(inputDirectory.getAbsolutePath(), outputDirectory.getAbsolutePath(), recursive);
    }

    public static String initGrobidHome(String grobidHomeFromConfig, File grobidHomeOverride) {
        try {
            GrobidHomeFinder grobidHomeFinder = null;
            if (grobidHomeOverride != null) {
                grobidHomeFinder = new GrobidHomeFinder(Collections.singletonList(grobidHomeOverride.getAbsolutePath()));
            } else {
                grobidHomeFinder = new GrobidHomeFinder(Collections.singletonList(grobidHomeFromConfig));
            }
            GrobidProperties.getInstance(grobidHomeFinder);
            
            Engine.getEngine(true);
            LibraryLoader.load();
            return GrobidProperties.getGrobidHome().getAbsolutePath();
            
        } catch (final Exception exp) {
            System.err.println("Grobid initialisation failed. Maybe you forget to specify the location of the config.yml in the command launch as final argument?");
            LOGGER.debug("Grobid initialisation error. ", exp);

            System.exit(-1);
        }

        return GrobidProperties.getGrobidHome().getAbsolutePath();
    }
}