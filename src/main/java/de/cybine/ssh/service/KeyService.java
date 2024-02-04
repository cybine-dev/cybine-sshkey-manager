package de.cybine.ssh.service;

import com.sshtools.common.publickey.*;
import com.sshtools.common.ssh.*;
import com.sshtools.common.ssh.components.*;
import de.cybine.ssh.api.v1.key.*;
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
import java.time.format.*;
import java.util.*;
import java.util.regex.*;

@Slf4j
@Startup
@Singleton
@RequiredArgsConstructor
public class KeyService
{
    private final ApplicationConfig applicationConfig;

    private final JsonWebToken token;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

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

    public String createCertificate(SignagePayload payload)
    {
        int validFor = payload.getValidFor().orElse(this.applicationConfig.defaultCertificateValidity());
        if (validFor > this.applicationConfig.maxCertificateValidity())
            throw new InvalidValidityException(
                    String.format("The maximum validity is %s days.", this.applicationConfig.maxCertificateValidity()));

        String email = this.token.getClaim("email");
        List<String> principals = this.getPrincipals(payload);
        if (principals.isEmpty())
            throw new MissingClaimException("The principals claim is required and must not be empty.");

        try
        {

            String certificate = this.createCertificate(SshKeyUtils.getPublicKey(payload.getPubkey()),
                    payload.getComment().orElse(email), payload.getSerial(), validFor, principals);

            log.info("Generated new certificate with serial {} for user {}.", payload.getSerial(), email);

            String name = (String) this.token.claim("name").orElse(email);
            this.certificateNotification.to(String.format("%s <%s>", name, email))
                                        .subject("Cybine Media - A new ssh-certificate has been generated")
                                        .data("serial", payload.getSerial())
                                        .data("valid-until",
                                                ZonedDateTime.now().plusDays(validFor).format(this.formatter))
                                        .data("host-authority-pubkey", this.applicationConfig.hostAuthorityPubkey())
                                        .data("principals", principals)
                                        .send()
                                        .await()
                                        .atMost(Duration.ofSeconds(10));

            return certificate;
        }
        catch (IOException | SshException exception)
        {
            throw new CertificateGenerationException("Could not generate certificate", exception);
        }
    }

    public String createCertificate(SshPublicKey publicKey, String comment, int serial, int validFor,
            List<String> principals) throws SshException, IOException
    {
        SshKeyPair keyPair = SshKeyPair.getKeyPair(null, publicKey);
        SshCertificate certificate = SshCertificateAuthority.generateCertificate(keyPair, serial,
                SshCertificate.SSH_CERT_TYPE_USER, comment, principals, validFor, Collections.emptyList(),
                new CertificateExtension.Builder().defaultExtensions().build(), this.authorityKey);

        return SshKeyUtils.getFormattedKey(certificate.getCertificate(), comment);
    }

    private List<String> getPrincipals(SignagePayload payload)
    {
        List<String> validPrincipals = this.token.claim("ssh-principals")
                                                 .map(String.class::cast)
                                                 .map(item -> item.split(","))
                                                 .map(Arrays::asList)
                                                 .orElse(Collections.emptyList())
                                                 .stream()
                                                 .map(String::trim)
                                                 .toList();

        List<String> principals = payload.getPrincipals().orElse(null);
        if (principals == null)
            return validPrincipals;

        List<Pattern> patterns = validPrincipals.stream().map(this::generatePrincipalPattern).toList();
        if (!principals.stream()
                       .allMatch(item -> patterns.stream().anyMatch(pattern -> pattern.matcher(item).matches())))
            throw new MissingClaimException("Request contains unauthorized principals.");

        return principals;
    }

    private Pattern generatePrincipalPattern(String principalTemplate)
    {
        String principal = principalTemplate.replace(".", "\\.").replace("*", ".+");
        if (!principal.contains("@"))
            principal = principal + "(@.+)?";

        return Pattern.compile(principal, Pattern.CASE_INSENSITIVE);
    }
}
