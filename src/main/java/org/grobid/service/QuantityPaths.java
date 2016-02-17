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
    public static final String PATH_QUANTITY = "/";
    
    /**
     * path extension for extracting quantities from a text.
     */
    public static final String PATH_QUANTITY_TEXT= "processQuantityText";

    /**
     * path extension for extracting quantities from an XML file.
     */
    public static final String PATH_QUANTITY_XML= "processQuantityXML";

    /**
     * path extension for extracting quantities from a PDF file.
     */
    public static final String PATH_QUANTITY_PDF= "processQuantityPDF";

    /**
     * path extension for annotating a PDF file with quantities.
     */
    public static final String PATH_ANNOTATE_QUANTITY_PDF= "annotateQuantityPDF";
}
