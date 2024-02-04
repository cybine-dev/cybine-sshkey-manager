package de.cybine.ssh.api.v1.key;

import io.quarkus.security.*;
import jakarta.validation.*;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.reactive.*;

@Authenticated
@Path("/key")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface KeyApi
{
    @POST
    RestResponse<SignageResponse> generateCertificate(@Valid SignagePayload payload);
}
