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

package gyro.aws.elb;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.diff.Create;
import gyro.core.diff.Delete;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancing.model.DescribeLoadBalancerPoliciesResponse;
import software.amazon.awssdk.services.elasticloadbalancing.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancing.model.ListenerDescription;
import software.amazon.awssdk.services.elasticloadbalancing.model.PolicyAttribute;
import software.amazon.awssdk.services.elasticloadbalancing.model.PolicyDescription;
import software.amazon.awssdk.services.elasticloadbalancing.model.PolicyNotFoundException;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     listener
 *        instance-port: "443"
 *        instance-protocol: "HTTPS"
 *        load-balancer-port: "443"
 *        protocol: "HTTPS"
 *     end
 */
public class ListenerResource extends AwsResource implements Copyable<ListenerDescription> {

    private Integer instancePort;
    private String instanceProtocol;
    private Integer loadBalancerPort;
    private String protocol;
    private String sslCertificateId;
    private LoadBalancerPolicy policy;

    /**
     * The port on which the instance is listening.
     */
    @Updatable
    public Integer getInstancePort() {
        return instancePort;
    }

    public void setInstancePort(Integer instancePort) {
        this.instancePort = instancePort;
    }

    /**
     * The protocol to use for routing traffic to instances : HTTP, HTTPS, TCP, SSL.
     */
    @Updatable
    public String getInstanceProtocol() {
        return instanceProtocol;
    }

    public void setInstanceProtocol(String instanceProtocol) {
        this.instanceProtocol = instanceProtocol;
    }

    /**
     * The port on which the load balancer is listening.
     */
    public Integer getLoadBalancerPort() {
        return loadBalancerPort;
    }

    public void setLoadBalancerPort(Integer loadBalancerPort) {
        this.loadBalancerPort = loadBalancerPort;
    }

    /**
     * The load balancer transport protocol to use for routing: HTTP, HTTPS, TCP, or SSL.
     */
    @Updatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * The Amazon Resource Name(ARN) of the server certificate.
     */
    @Updatable
    public String getSslCertificateId() {
        return sslCertificateId;
    }

    public void setSslCertificateId(String sslCertificateId) {
        this.sslCertificateId = sslCertificateId;
    }

    /**
     * The policy configuration for the listener.
     *
     * @subresource gyro.aws.elb.LoadBalancerPolicy
     */
    @Updatable
    public LoadBalancerPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(LoadBalancerPolicy policy) {
        this.policy = policy;
    }

    @Override
    public String primaryKey() {
        return String.format("%d", getLoadBalancerPort());
    }

    @Override
    public void copyFrom(ListenerDescription listenerDescription) {
        setInstancePort(listenerDescription.listener().instancePort());
        setInstanceProtocol(listenerDescription.listener().instanceProtocol());
        setLoadBalancerPort(listenerDescription.listener().loadBalancerPort());
        setProtocol(listenerDescription.listener().protocol());
        setSslCertificateId(listenerDescription.listener().sslCertificateId());

        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

        DescribeLoadBalancerPoliciesResponse response = client.describeLoadBalancerPolicies(
            r -> r.loadBalancerName(getLoadBalancer()).policyNames(listenerDescription.policyNames()));

        setPolicy(null);
        if (!response.policyDescriptions().isEmpty()) {
            PolicyDescription policyDescription = response.policyDescriptions().get(0);
            LoadBalancerPolicy policy = newSubresource(LoadBalancerPolicy.class);
            policy.copyFrom(policyDescription);
            setPolicy(policy);
        }
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        if (DiffableInternals.getChange(parent()) instanceof Create) {
            return;
        }

        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);
        client.createLoadBalancerListeners(r -> r.listeners(toListener())
            .loadBalancerName(getLoadBalancer()));

        if (getPolicy() != null) {
            state.save();

            savePolicy(client);
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        if (changedFieldNames.stream().anyMatch(o -> !o.equals("policy") && !o.equals("ssl-certificate-id"))) {
            delete(ui, state);
            create(ui, state);
        } else {
            ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

            if (changedFieldNames.contains("ssl-certificate-id")) {
                client.setLoadBalancerListenerSSLCertificate(r -> r.loadBalancerName(getLoadBalancer())
                    .loadBalancerPort(getLoadBalancerPort())
                    .sslCertificateId(getSslCertificateId()));
            }

            if (changedFieldNames.contains("policy")) {
                savePolicy(client);
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        if (DiffableInternals.getChange(parent()) instanceof Delete) {
            return;
        }

        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

        client.deleteLoadBalancerListeners(r -> r.loadBalancerName(getLoadBalancer())
            .loadBalancerPorts(getLoadBalancerPort()));
    }

    public String getLoadBalancer() {
        LoadBalancerResource parent = (LoadBalancerResource) parent();

        if (parent != null) {
            return parent.getName();
        }

        return null;
    }

    private Listener toListener() {
        Listener newListener = Listener.builder()
            .instancePort(getInstancePort())
            .instanceProtocol(getInstanceProtocol())
            .loadBalancerPort(getLoadBalancerPort())
            .protocol(getProtocol())
            .sslCertificateId(getSslCertificateId())
            .build();

        return newListener;
    }

    void savePolicy(ElasticLoadBalancingClient client) {
        List<PolicyAttribute> policyAttributes;
        String policyType;
        String policyName;

        if (!ObjectUtils.isBlank(getPolicy().getPredefinedPolicy())) {
            policyName = String.format(
                "gyro-elb-%s-listener-%s-policy-%s",
                getLoadBalancer(),
                getLoadBalancerPort(),
                getPolicy().getPredefinedPolicy());

            DescribeLoadBalancerPoliciesResponse res = client.describeLoadBalancerPolicies(
                r -> r.policyNames(getPolicy().getPredefinedPolicy()));

            policyType = res.policyDescriptions().get(0).policyTypeName();
            policyAttributes = res.policyDescriptions()
                .get(0)
                .policyAttributeDescriptions()
                .stream()
                .map(o -> PolicyAttribute.builder()
                    .attributeName(o.attributeName())
                    .attributeValue(o.attributeValue())
                    .build())
                .collect(
                    Collectors.toList());
        } else {
            policyName = String.format(
                "gyro-elb-%s-listener-%s-policy-%s",
                getLoadBalancer(),
                getLoadBalancerPort(),
                UUID.randomUUID().toString());
            policyType = getPolicy().getType();
            policyAttributes = getPolicy().getEnabledAttributes().stream()
                .map(o -> PolicyAttribute.builder()
                    .attributeName(o)
                    .attributeValue("true")
                    .build())
                .collect(Collectors.toList());
        }

        if (!policyExists(client, policyName)) {
            client.createLoadBalancerPolicy(r -> r.loadBalancerName(getLoadBalancer())
                .policyName(policyName)
                .policyTypeName(policyType)
                .policyAttributes(policyAttributes));
        }

        assignPolicy(client, policyName);
    }

    private void assignPolicy(ElasticLoadBalancingClient client, String policyName) {
        client.setLoadBalancerPoliciesOfListener(r -> r.loadBalancerName(getLoadBalancer())
            .loadBalancerPort(getLoadBalancerPort())
            .policyNames(policyName));
    }

    private boolean policyExists(ElasticLoadBalancingClient client, String policyName) {
        boolean policyExists = false;
        try {
            DescribeLoadBalancerPoliciesResponse response = client.describeLoadBalancerPolicies(
                r -> r.loadBalancerName(getLoadBalancer()).policyNames(policyName));

            policyExists = !response.policyDescriptions().isEmpty();
        } catch (PolicyNotFoundException ignore) {

        }

        return policyExists;
    }
}
