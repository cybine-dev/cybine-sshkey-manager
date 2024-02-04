package de.cybine.ssh.api.v1.key;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import lombok.extern.jackson.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignageResponse
{
    @JsonProperty("certificate")
    private final String certificate;
}
