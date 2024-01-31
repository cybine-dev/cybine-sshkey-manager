package de.cybine.ssh.api.v1.key;

import de.cybine.ssh.service.*;
import io.quarkus.security.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;

@Singleton
@Authenticated
@RequiredArgsConstructor
public class KeyResource implements KeyApi
{
    private final KeyService keyService;

    public RestResponse<String> generateCertificate(@QueryParam("public-key") String publicKey,
            @QueryParam("serial") @DefaultValue("0") int serial)
    {
        return RestResponse.ok(this.keyService.createCertificate(publicKey, serial));
    }
}
