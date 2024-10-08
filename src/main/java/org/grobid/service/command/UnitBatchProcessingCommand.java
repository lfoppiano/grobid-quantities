package org.grobid.service.command;

import io.dropwizard.core.cli.ConfiguredCommand;
import io.dropwizard.core.setup.Bootstrap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.grobid.core.engines.QuantitiesEngine;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.grobid.service.command.TrainingGenerationCommand.*;

public class UnitBatchProcessingCommand extends ConfiguredCommand<GrobidQuantitiesConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnitBatchProcessingCommand.class);


    public UnitBatchProcessingCommand() {
        super("batchUnits", "Process units files in batch");
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
            .type(Arguments.fileType()
                .verifyNotExists().verifyCanCreate()
                .or()
                .verifyIsDirectory().verifyCanWrite())
            .required(true)
            .help("Output directory");

        subparser.addArgument("-gH")
            .dest(GROBID_HOME_DIRECTORY)
            .type(Arguments.fileType().verifyExists().verifyCanRead().verifyIsDirectory())
            .required(false)
            .help("Override the grobid-home directory from the configuration. ");


//        subparser.addArgument("-r")
//            .dest(RECURSIVE)
//            .type(Boolean.class)
//            .setDefault(false)
//            .required(false)
//            .help("Recursive processing");

    }

    @Override
    protected void run(Bootstrap bootstrap, Namespace namespace, GrobidQuantitiesConfiguration configuration) throws Exception {
        File grobidHomeOverride = namespace.get(GROBID_HOME_DIRECTORY);
        String grobidHome = configuration.getGrobidHome();
        initGrobidHome(grobidHome, grobidHomeOverride);
        configuration.getModels().stream().forEach(GrobidProperties::addModel);

        File inputDirectory = namespace.get(INPUT_DIRECTORY);
        File outputDirectory = namespace.get(OUTPUT_DIRECTORY);
//        boolean isRecursive = namespace.get(RECURSIVE);

        new QuantitiesEngine().unitBatchProcess(inputDirectory.getAbsolutePath(), outputDirectory.getAbsolutePath());
    }
}