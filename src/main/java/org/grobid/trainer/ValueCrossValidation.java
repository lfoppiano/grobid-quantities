package org.grobid.trainer;

import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ValueCrossValidation extends ValueTrainer {

    /**
     * Command line execution. Assuming grobid-home is in ../grobid-home.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        LibraryLoader.load();
        GrobidProperties.getInstance();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        Trainer trainer = new ValueTrainer();
        String report = AbstractTrainer.runNFoldEvaluation(trainer, 10);

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("logs/value-10fold-cross-validation-" + formatter.format(date) + ".txt"))) {
            writer.write(report);
            writer.write("\n");
        } catch (IOException e) {
            throw new GrobidException("Error when dumping n-fold training data into files. ", e);
        }
    }
}
