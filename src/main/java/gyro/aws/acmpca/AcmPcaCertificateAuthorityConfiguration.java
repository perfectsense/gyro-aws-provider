package gyro.aws.acmpca;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.acmpca.model.CertificateAuthorityConfiguration;
import software.amazon.awssdk.services.acmpca.model.KeyAlgorithm;
import software.amazon.awssdk.services.acmpca.model.SigningAlgorithm;

public class AcmPcaCertificateAuthorityConfiguration extends Diffable implements Copyable<CertificateAuthorityConfiguration> {
    private KeyAlgorithm keyAlgorithm;
    private SigningAlgorithm signingAlgorithm;
    private AcmPcaAsn1Subject subject;

    /**
     * The type of the key algorithm. Valid values are ``RSA_2048`` or ``RSA_4096`` or ``EC_prime256v1`` or ``EC_secp384r1``. (Required)
     */
    public KeyAlgorithm getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(KeyAlgorithm keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    /**
     * The type of signing algorithm. Valid values are ``SHA256WITHECDSA`` or ``SHA384WITHECDSA`` or ``SHA512WITHECDSA`` or ``SHA256WITHRSA`` or ``SHA384WITHRSA`` or ``SHA512WITHRSA``. (Required)
     */
    public SigningAlgorithm getSigningAlgorithm() {
        return signingAlgorithm;
    }

    public void setSigningAlgorithm(SigningAlgorithm signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    /**
     * The subject configuration. (Required)
     */
    public AcmPcaAsn1Subject getSubject() {
        return subject;
    }

    public void setSubject(AcmPcaAsn1Subject subject) {
        this.subject = subject;
    }

    @Override
    public void copyFrom(CertificateAuthorityConfiguration certificateAuthorityConfiguration) {
        setKeyAlgorithm(certificateAuthorityConfiguration.keyAlgorithm());
        setSigningAlgorithm(certificateAuthorityConfiguration.signingAlgorithm());

        AcmPcaAsn1Subject subject = newSubresource(AcmPcaAsn1Subject.class);
        subject.copyFrom(certificateAuthorityConfiguration.subject());
        setSubject(subject);
    }

    @Override
    public String primaryKey() {
        return "certificate authority configuration";
    }

    CertificateAuthorityConfiguration toCertificateAuthorityConfiguration() {
        return CertificateAuthorityConfiguration.builder()
            .keyAlgorithm(getKeyAlgorithm())
            .signingAlgorithm(getSigningAlgorithm())
            .subject(getSubject().toAsn1Subject())
            .build();
    }
}