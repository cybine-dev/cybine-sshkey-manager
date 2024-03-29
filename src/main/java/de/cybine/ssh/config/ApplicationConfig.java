package de.cybine.ssh.config;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;
import jakarta.validation.constraints.*;

import java.util.*;

@StaticInitSafe
@SuppressWarnings("unused")
@ConfigMapping(prefix = "application")
public interface ApplicationConfig
{
    @NotNull @NotBlank
    @WithName("authority-key-location")
    String authorityKeyLocation( );

    @WithName("authority-key-passphrase")
    Optional<String> authorityKeyPassphrase( );

    @WithDefault("14")
    @WithName("default-certificate-validity")
    int defaultCertificateValidity( );

    @WithDefault("30")
    @WithName("max-certificate-validity")
    int maxCertificateValidity( );

    @NotNull @NotBlank
    @WithName("host-authority-pubkey")
    String hostAuthorityPubkey( );
}
