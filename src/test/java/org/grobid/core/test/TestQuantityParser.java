package org.grobid.core.test;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.engines.QuantityParser;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.main.LibraryLoader;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *  @author Patrice Lopez
 */
public class TestQuantityParser {

	@BeforeClass
    public static void init() {
        LibraryLoader.load();
    }

	public File getResourceDir(String resourceDir) {
		File file = new File(resourceDir);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new GrobidException("Cannot start test, because test resource folder is not correctly set.");
			}
		}
		return(file);
	}
	
	@Test
	public void testQuantityParser() throws Exception {
		File textFile = 
			new File(this.getResourceDir("./src/test/resources/").getAbsoluteFile()+"/test.txt");
		if (!textFile.exists()) {
			throw new GrobidException("Cannot start test, because test resource folder is not correctly set.");
		}
		String text = FileUtils.readFileToString(textFile);	
		
		QuantityParser parser = new QuantityParser();
		
		List<Quantity> quantities = parser.extractQuantities(text); 
		if (quantities != null) {
			for(Quantity quantity : quantities) {
				System.out.println(quantity.toString());
			}
		}
		else {
			System.out.println("No quantity found.");
		}
	}
	
}