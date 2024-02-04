package de.cybine.ssh.exception.handler;

import de.cybine.ssh.exception.*;
import lombok.extern.slf4j.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.server.*;

@Slf4j
@SuppressWarnings("unused")
public class CertificateGenerationExceptionHandler
{
    @ServerExceptionMapper(CertificateGenerationException.class)
    public RestResponse<Void> toResponse(CertificateGenerationException exception)
    {
        log.debug("A handled exception was thrown during api-request", exception);
        return RestResponse.serverError();
    }
}
