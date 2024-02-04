package de.cybine.ssh.exception.handler;

import de.cybine.ssh.exception.*;
import lombok.extern.slf4j.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.server.*;

@Slf4j
@SuppressWarnings("unused")
public class InvalidValidityExceptionHandler
{
    @ServerExceptionMapper(InvalidValidityException.class)
    public RestResponse<Void> toResponse(InvalidValidityException exception)
    {
        log.debug("A handled exception was thrown during api-request", exception);
        return RestResponse.status(RestResponse.Status.BAD_REQUEST);
    }
}

