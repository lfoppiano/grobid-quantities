package org.grobid.service.exceptions.mapper;

import com.google.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.grobid.core.exceptions.GrobidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Provider
public class GrobidExceptionMapper implements ExceptionMapper<GrobidException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrobidExceptionMapper.class);

    @Context
    protected HttpHeaders headers;

    @Context
    private UriInfo uriInfo;

    @Inject
    private GrobidExceptionsTranslationUtility mapper;


    @Inject
    public GrobidExceptionMapper() {

    }

    @Override
    public Response toResponse(GrobidException exception) {
        LOGGER.error("An exception has occurred:", exception);
        return mapper.processException(exception, GrobidStatusToHttpStatusMapper.getStatusCode(exception.getStatus()));
    }
}
