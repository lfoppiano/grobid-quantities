package org.grobid.core.lexicon;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.engines.QuantityParser;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.OffsetPosition;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *  @author Patrice Lopez
 */
//@Ignore
public class TestQuantityLexicon {
	
	private QuantityLexicon lexicon = null;

	@Before
	public void setUp() {
		lexicon = QuantityLexicon.getInstance();
	}

	@Test
	public void testInUnitDictionary() throws Exception {
		String input = "meter";
		boolean test = lexicon.inUnitDictionary(input);
		assertEquals("Problem with inUnitDictionary", test, true);

		input = "m";
		test = lexicon.inUnitDictionary(input);
		assertEquals("Problem with inUnitDictionary", test, true);
	}
	
	@Test
	public void testInUnitNamesSimple() throws Exception {
		String input = "1 meter";
		List<OffsetPosition> unitPositions = lexicon.inUnitNames(input);
		assertNotNull(unitPositions);
		if (unitPositions != null) {
			assertEquals("Problem with unit matcher", unitPositions.size(), 1);
			if (unitPositions.size() > 0) {
				assertEquals("Problem with unit matching position", unitPositions.get(0).start, 1);
			}
		}

		input = "10 meters";
		unitPositions = lexicon.inUnitNames(input);
		/*assertNotNull(unitPositions);
		if (unitPositions != null) {
			assertEquals("Problem with unit matcher", unitPositions.size(), 1);
			if (unitPositions.size() > 0) {
				assertEquals("Problem with unit matching position", unitPositions.get(0).start, 1);
			}
		}*/
	}

	@Test
	public void testinUnitNamesComplex() throws Exception {
		String input = "kilometer";
		List<OffsetPosition> unitPositions = lexicon.inUnitNames(input);
		/*assertNotNull(unitPositions);
		if (unitPositions != null) {
			assertEquals("Problem with unit matcher", unitPositions.size(), 1);
			if (unitPositions.size() > 0) {
				assertEquals("Problem with unit matching position", unitPositions.get(0).start, 0);
			}
		}*/

		input = "kilometer per second";

		input = "10 kg / s";
	}

	@Test
	public void testinUnitNamesText() throws Exception {
		
	}
}