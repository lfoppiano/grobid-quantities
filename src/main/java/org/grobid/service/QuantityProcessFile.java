package org.grobid.service;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Collections;
import java.io.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.HttpHeaders; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.annotation.*;

/**
 * 
 * @author Patrice
 * 
 */
public class QuantityProcessFile {

    /**
     * The class Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QuantityProcessFile.class);


    public static Response processPDF(String text) {
        LOGGER.debug(methodLogIn());
        Response response = null;


        LOGGER.debug(methodLogOut());
        return response;
    }

    /**
     * @return
     */
    private static String methodLogIn() {
        return ">> " + QuantityProcessFile.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    /**
     * @return
     */
    private static String methodLogOut() {
        return "<< " + QuantityProcessFile.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }
}