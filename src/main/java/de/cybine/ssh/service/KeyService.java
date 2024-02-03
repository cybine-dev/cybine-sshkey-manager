package de.cybine.ssh.service;

import com.sshtools.common.publickey.*;
import com.sshtools.common.ssh.*;
import com.sshtools.common.ssh.components.*;
import de.cybine.ssh.config.ApplicationConfig;
import de.cybine.ssh.exception.*;
import io.quarkus.mailer.*;
import io.quarkus.qute.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import jakarta.inject.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.eclipse.microprofile.jwt.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

@Slf4j
@Startup
@Singleton
@RequiredArgsConstructor
public class KeyService
{
    private final ApplicationConfig applicationConfig;

    private final JsonWebToken token;

    @Inject
    @Location("certificate-generation")
    MailTemplate certificateNotification;

    private SshKeyPair authorityKey;

    @PostConstruct
    void setup( ) throws InvalidPassphraseException, IOException
    {
        Path authorityKeyLocation = Path.of(this.applicationConfig.authorityKeyLocation());
        this.authorityKey = SshKeyUtils.getPrivateKey(authorityKeyLocation.toFile(),
                this.applicationConfig.authorityKeyPassphrase().orElse(""));
    }

    public String createCertificate(String publicKey, int serial)
    {
        String email = this.token.getClaim("email");
        List<String> principals = this.token.claim("ssh-principals")
                                            .map(String.class::cast)
                                            .map(item -> item.split(","))
                                            .map(Arrays::asList)
                                            .orElse(Collections.emptyList());

        if (principals.isEmpty())
            throw new MissingClaimException("The ssh-principals claim is required and must not be empty.");

        try
        {
            return this.createCertificate(SshKeyUtils.getPublicKey(publicKey), email, serial, principals);
        }
        catch (IOException | SshException exception)
        {
            throw new CertificateGenerationException("Could not generate certificate", exception);
        }
        finally
        {
            log.info("Generated new certificate with serial {} for user {}.", serial, email);

            String name = (String) this.token.claim("name").orElse(email);
            this.certificateNotification.to(String.format("%s <%s>", name, email))
                                        .subject("Cybine Media - A new ssh-certificate has been generated")
                                        .data("serial", serial)
                                        .data("host-authority-pubkey", this.applicationConfig.hostAuthorityPubkey())
                                        .data("principals", principals)
                                        .send()
                                        .await()
                                        .atMost(Duration.ofSeconds(10));
        }
    }

    public String createCertificate(SshPublicKey publicKey, String comment, int serial, List<String> principals)
            throws SshException, IOException
    {
        SshKeyPair keyPair = SshKeyPair.getKeyPair(null, publicKey);
        SshCertificate certificate = SshCertificateAuthority.generateCertificate(keyPair, serial,
                SshCertificate.SSH_CERT_TYPE_USER, comment, principals,
                this.applicationConfig.defaultCertificateValidity(), Collections.emptyList(),
                new CertificateExtension.Builder().defaultExtensions().build(), this.authorityKey);

        return SshKeyUtils.getFormattedKey(certificate.getCertificate(), comment);
    }
}
