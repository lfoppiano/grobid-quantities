package org.grobid.core.engines;

import org.apache.commons.io.IOUtils;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.data.Quantity;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


/**
 * 
 */
@Ignore
public class DefaultValueParserTest {

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
    }

    @Test
    public void testDefaultValueParser() throws Exception {
        ValueParser parser = DefaultValueParser.getInstance();
		Quantity quantity = new Quantity();
		quantity.setRawValue("1.2");
        parser.parseValue(quantity);
        assertThat(quantity.getParsedValue(), is(new BigDecimal(1.2)));

        quantity.setRawValue("1.2.");
        parser.parseValue(quantity);
        assertThat(quantity.getParsedValue(), is(new BigDecimal(1.2)));
    }

}