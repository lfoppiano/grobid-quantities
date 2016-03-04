package org.grobid.core.engines;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.main.LibraryLoader;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * @author Patrice Lopez
 */
public class QuantityParserTest {

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
    }



}