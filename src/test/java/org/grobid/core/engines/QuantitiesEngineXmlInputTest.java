package org.grobid.core.engines;

import jakarta.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.grobid.service.exceptions.GrobidServiceException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The text extraction behind {@code POST /service/processQuantityXML}.
 * See https://github.com/lfoppiano/grobid-quantities/issues/3
 */
class QuantitiesEngineXmlInputTest {

    private String extract(String xml) {
        try (InputStream is = IOUtils.toInputStream(xml, UTF_8)) {
            return QuantitiesEngine.extractText(is);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void paragraphs_shouldBeExtractedAndJoined() {
        String text = extract("<tei><text>"
            + "<p>We lost 10 kg.</p>"
            + "<p>Then 20 more.</p>"
            + "</text></tei>");

        assertEquals("We lost 10 kg.\nThen 20 more.", text);
    }

    @Test
    void markupInsideAParagraph_shouldBeDiscardedButItsTextKept() {
        String text = extract("<tei><text><p>We lost <hi rend=\"bold\">10</hi> kg.</p></text></tei>");

        assertEquals("We lost 10 kg.", text);
    }

    /**
     * The handler is meant to cope with the usual formats, ST.36 patents included, where the
     * paragraph element is spelled out.
     */
    @Test
    void st36Paragraphs_shouldBeExtractedToo() {
        String text = extract("<patent-document><description>"
            + "<paragraph>A pressure of 10 bar.</paragraph>"
            + "</description></patent-document>");

        assertEquals("A pressure of 10 bar.", text);
    }

    @Test
    void aDocumentWithoutParagraphs_shouldYieldNoText() {
        assertEquals("", extract("<tei><text><div>loose text</div></text></tei>"));
    }

    @Test
    void malformedXml_shouldBeReportedAsABadRequest() {
        GrobidServiceException e = assertThrows(GrobidServiceException.class,
            () -> extract("<tei><text><p>unclosed"));

        assertEquals(Response.Status.BAD_REQUEST, e.getResponseCode());
    }

    /**
     * The input comes from the network. A DOCTYPE declaration must not be honoured, otherwise the
     * document can read local files (XXE) or blow up the parser through nested entities.
     */
    @Test
    void anExternalEntity_shouldNotBeResolved() throws Exception {
        File secret = File.createTempFile("quantities-xxe-", ".txt");
        secret.deleteOnExit();
        Files.write(Path.of(secret.getAbsolutePath()), "TOP-SECRET".getBytes(UTF_8));

        String xml = "<?xml version=\"1.0\"?>"
            + "<!DOCTYPE tei [<!ENTITY xxe SYSTEM \"file://" + secret.getAbsolutePath() + "\">]>"
            + "<tei><text><p>&xxe;</p></text></tei>";

        GrobidServiceException e = assertThrows(GrobidServiceException.class, () -> extract(xml));

        assertEquals(Response.Status.BAD_REQUEST, e.getResponseCode());
        assertFalse(String.valueOf(e.getMessage()).contains("TOP-SECRET"));
    }

    @Test
    void aBillionLaughs_shouldNotBeExpanded() {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?><!DOCTYPE lolz [");
        xml.append("<!ENTITY lol \"lol\">");
        for (int i = 1; i <= 9; i++) {
            xml.append("<!ENTITY lol").append(i).append(" \"");
            for (int j = 0; j < 10; j++) {
                xml.append("&lol").append(i == 1 ? "" : String.valueOf(i - 1)).append(";");
            }
            xml.append("\">");
        }
        xml.append("]><tei><text><p>&lol9;</p></text></tei>");

        GrobidServiceException e = assertThrows(GrobidServiceException.class, () -> extract(xml.toString()));

        assertEquals(Response.Status.BAD_REQUEST, e.getResponseCode());
    }

    @Test
    void aDoctypeAlone_shouldAlreadyBeRejected() {
        GrobidServiceException e = assertThrows(GrobidServiceException.class,
            () -> extract("<!DOCTYPE tei SYSTEM \"tei.dtd\"><tei><text><p>10 kg</p></text></tei>"));

        assertEquals(Response.Status.BAD_REQUEST, e.getResponseCode());
        assertTrue(String.valueOf(e.getMessage()).toLowerCase().contains("doctype"));
    }
}
