/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.apigatewayv2;

import gyro.aws.Copyable;
import gyro.aws.acm.AcmCertificateResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.DescribeCertificateResponse;
import software.amazon.awssdk.services.apigatewayv2.model.DomainNameConfiguration;
import software.amazon.awssdk.services.apigatewayv2.model.EndpointType;
import software.amazon.awssdk.services.apigatewayv2.model.SecurityPolicy;

public class ApiDomainNameConfiguration extends Diffable implements Copyable<DomainNameConfiguration> {

    private AcmCertificateResource certificate;
    private EndpointType endpointType;
    private SecurityPolicy securityPolicy;

    /**
     * An AWS-managed certificate that will be used by the edge-optimized endpoint for this domain name.
     */
    @Required
    public AcmCertificateResource getCertificate() {
        return certificate;
    }

    public void setCertificate(AcmCertificateResource certificate) {
        this.certificate = certificate;
    }

    /**
     * The endpoint type.
     */
    @ValidStrings({ "REGIONAL", "EDGE" })
    public EndpointType getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(EndpointType endpointType) {
        this.endpointType = endpointType;
    }

    /**
     * The Transport Layer Security (TLS) version of the security policy for this domain name.
     */
    @ValidStrings({ "TLS_1_0", "TLS_1_2" })
    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }

    public void setSecurityPolicy(SecurityPolicy securityPolicy) {
        this.securityPolicy = securityPolicy;
    }

    @Override
    public String primaryKey() {
        StringBuilder sb = new StringBuilder("Api DomainName Configuration - ");

        sb.append("Certificate: ").append(getCertificate().getArn()).append(" ");

        if (getEndpointType() != null) {
            sb.append("Endpoint: ").append(getEndpointType()).append(" ");
        }

        return sb.toString();
    }

    @Override
    public void copyFrom(DomainNameConfiguration model) {
        setCertificate(findById(AcmCertificateResource.class, model.certificateArn()));
        setEndpointType(model.endpointType());
        setSecurityPolicy(model.securityPolicy());
    }

    public DomainNameConfiguration toDomainNameConfiguration() {
        return DomainNameConfiguration.builder()
            .apiGatewayDomainName(((DomainNameResource) parent()).getName())
            .certificateArn(getCertificate().getArn())
            .endpointType(getEndpointType())
            .securityPolicy(getSecurityPolicy())
            .build();
    }
}
