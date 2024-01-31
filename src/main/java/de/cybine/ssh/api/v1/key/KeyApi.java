package de.cybine.ssh.api.v1.key;

import io.quarkus.security.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.reactive.*;

@Authenticated
@Path("/key")
public interface KeyApi
{
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    RestResponse<String> generateCertificate(@QueryParam("public-key") String publicKey,
            @QueryParam("serial") @DefaultValue("0") int serial);
}
