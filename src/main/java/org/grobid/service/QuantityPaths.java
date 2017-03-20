package org.grobid.service;

/**
 * This interface only contains the path extensions for accessing the nerd service.
 *
 * @author Patrice Lopez
 *
 */
public interface QuantityPaths {
    /**
     * path extension for Nerd service.
     */
    String PATH_QUANTITY = "/";

    /**
     * path extension for extracting quantities from a text.
     */
    String PATH_QUANTITY_TEXT= "processQuantityText";

    /**
     * path extension for extracting quantities from an XML file.
     */
    String PATH_QUANTITY_XML= "processQuantityXML";


    /**
     * path extension for annotating a PDF file with quantities.
     */
    String PATH_ANNOTATE_QUANTITY_PDF= "annotateQuantityPDF";

    /**
     * path extension for parsing quantities from a non or partially structured measure.
     */
    String PATH_PARSE_MEASURE = "parseMeasure";

    /**
	 * path extension for is alive request.
	 */
    String PATH_IS_ALIVE = "isalive";
}
