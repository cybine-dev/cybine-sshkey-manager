package de.cybine.ssh.api.v1.key;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignagePayload
{
    @NotNull
    @NotBlank
    @JsonProperty("pubkey")
    private final String pubkey;

    @JsonProperty("comment")
    private final String comment;

    @Builder.Default
    @JsonProperty("serial")
    private final int serial = 0;

    @JsonProperty("valid_for")
    private final Integer validFor;

    @JsonProperty("principals")
    private final List<String> principals;

    public Optional<String> getComment( )
    {
        return Optional.ofNullable(this.comment);
    }

    public Optional<Integer> getValidFor( )
    {
        return Optional.ofNullable(this.validFor);
    }

    public Optional<List<String>> getPrincipals( )
    {
        return Optional.ofNullable(this.principals);
    }
}
