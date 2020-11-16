/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.acmpca;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.acmpca.model.CertificateAuthorityConfiguration;
import software.amazon.awssdk.services.acmpca.model.KeyAlgorithm;
import software.amazon.awssdk.services.acmpca.model.SigningAlgorithm;

public class AcmPcaCertificateAuthorityConfiguration extends Diffable implements Copyable<CertificateAuthorityConfiguration> {
    private KeyAlgorithm keyAlgorithm;
    private SigningAlgorithm signingAlgorithm;
    private AcmPcaAsn1Subject subject;

    /**
     * The type of the key algorithm.
     */
    @ValidStrings({"RSA_2048", "RSA_4096", "EC_prime256v1", "EC_secp384r1"})
    public KeyAlgorithm getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(KeyAlgorithm keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    /**
     * The type of signing algorithm.
     */
    @ValidStrings({"SHA256WITHECDSA", "SHA384WITHECDSA", "SHA512WITHECDSA", "SHA256WITHRSA", "SHA384WITHRSA", "SHA512WITHRSA"})
    public SigningAlgorithm getSigningAlgorithm() {
        return signingAlgorithm;
    }

    public void setSigningAlgorithm(SigningAlgorithm signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    /**
     * The subject configuration.
     *
     * @subresource gyro.aws.acmpca.AcmPcaAsn1Subject
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
