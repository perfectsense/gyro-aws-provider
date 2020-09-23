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

package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;

import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Certificate;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeListenersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ListenerNotFoundException;

import java.util.ArrayList;
import java.util.List;

public abstract class ListenerResource extends AwsResource implements Copyable<Listener> {

    private List<CertificateResource> certificate;
    private String defaultCertificate;
    private String arn;
    private Integer port;
    private String protocol;
    private String sslPolicy;

    /**
     *  List of certificates associated with the listener.
     *
     *  @subresource gyro.aws.elbv2.CertificateResource
     */
    @Updatable
    public List<CertificateResource> getCertificate() {
        if (certificate == null) {
            certificate = new ArrayList<>();
        }

        return certificate;
    }

    public void setCertificate(List<CertificateResource> certificate) {
        this.certificate = certificate;
    }

    /**
     *  The default certificate ARN associated with the listener. Required with ``HTTPS`` protocol.
     */
    @Updatable
    public String getDefaultCertificate() {
        return defaultCertificate;
    }

    public void setDefaultCertificate(String defaultCertificate) {
        this.defaultCertificate = defaultCertificate;
    }

    /**
     *  The arn of the listener.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     *  Connection port between client and the load balancer.
     */
    @Required
    @Updatable
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     *  Connection protocol between client and the load balancer. Valid values are ``HTTP`` and ``HTTPS`` for ALBs and ``TCP`` and ``TLS`` for NLBs.
     */
    @Required
    @Updatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     *  Security policy that defines supported protocols and ciphers.
     */
    @Updatable
    public String getSslPolicy() {
        return sslPolicy;
    }

    public void setSslPolicy(String sslPolicy) {
        this.sslPolicy = sslPolicy;
    }

    @Override
    public void copyFrom(Listener listener) {
        if (!listener.certificates().isEmpty()) {
            setDefaultCertificate(listener.certificates().get(0).certificateArn());
        }

        setArn(listener.listenerArn());
        setPort(listener.port());
        setProtocol(listener.protocolAsString());
        setSslPolicy(listener.sslPolicy());
    }

    public Listener internalRefresh() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        try {
            DescribeListenersResponse lstResponse = client.describeListeners(r -> r.listenerArns(getArn()));

            Listener listener = lstResponse.listeners().get(0);

            this.copyFrom(listener);

            return listener;

        } catch (ListenerNotFoundException ex) {
            return null;
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.deleteListener(r -> r.listenerArn(getArn()));
    }

    public List<Certificate> toCertificates() {
        List<Certificate> certificates = new ArrayList<>();
        for (CertificateResource cert : getCertificate()) {
            certificates.add(Certificate.builder().certificateArn(cert.getArn()).build());
        }
        return certificates;
    }
}
