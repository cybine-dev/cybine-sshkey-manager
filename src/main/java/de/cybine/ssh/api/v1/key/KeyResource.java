package de.cybine.ssh.api.v1.key;

import de.cybine.ssh.service.*;
import io.quarkus.security.*;
import jakarta.inject.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;

@Singleton
@Authenticated
@RequiredArgsConstructor
public class KeyResource implements KeyApi
{
    private final KeyService keyService;

    public RestResponse<SignageResponse> generateCertificate(SignagePayload payload)
    {
        return RestResponse.ok(
                SignageResponse.builder().certificate(this.keyService.createCertificate(payload)).build());
    }
}
