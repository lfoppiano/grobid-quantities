package org.grobid.core.engines;

import org.grobid.core.data.Quantity;
import org.grobid.core.main.LibraryLoader;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@Ignore
public class DefaultValueParserTest {

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
    }

    @Test
    public void testDefaultValueParser() throws Exception {
        ValueParser parser = DefaultValueParser.getInstance();
        BigDecimal output = parser.parseValue("1.2");
        assertThat(output, is(new BigDecimal("1.2")));

        output = parser.parseValue("1.2.");
        assertThat(output, is(new BigDecimal("1.2")));
    }

}