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

package gyro.aws.route53;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.CreateTrafficPolicyInstanceResponse;
import software.amazon.awssdk.services.route53.model.GetTrafficPolicyInstanceResponse;
import software.amazon.awssdk.services.route53.model.NoSuchTrafficPolicyInstanceException;
import software.amazon.awssdk.services.route53.model.TrafficPolicyInstance;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Creates a Traffic Policy Instance resource.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::route53-traffic-policy-instance traffic-policy-instance-example
 *         name: "traffic-policy-instance-example"
 *         comment: "traffic-policy-instance-example Comment"
 *         version: 1
 *         ttl: 900
 *         hosted-zone: $(aws::route53-hosted-zone hosted-zone-record-set-example)
 *         traffic-policy: $(aws::route53-traffic-policy traffic-policy-example-instance)
 *     end
 *
 */
@Type("route53-traffic-policy-instance")
public class TrafficPolicyInstanceResource extends AwsResource implements Copyable<TrafficPolicyInstance> {
    private String name;
    private String message;
    private HostedZoneResource hostedZone;
    private TrafficPolicyResource trafficPolicy;
    private String type;
    private Long ttl;
    private String state;
    private String id;

    /**
     * Name of the Traffic Policy Instance.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Message for the Traffic Policy Instance.
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * The associated Hosted Zone.
     */
    @Required
    public HostedZoneResource getHostedZone() {
        return hostedZone;
    }

    public void setHostedZone(HostedZoneResource hostedZone) {
        this.hostedZone = hostedZone;
    }

    /**
     * The Traffic Policy to be associated.
     */
    @Required
    @Updatable
    public TrafficPolicyResource getTrafficPolicy() {
        return trafficPolicy;
    }

    public void setTrafficPolicy(TrafficPolicyResource trafficPolicy) {
        this.trafficPolicy = trafficPolicy;
    }

    /**
     *  The type of the Traffic Policy Instance.
     */
    @Output
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The TTL that you want to assign to all of the resource Record Sets that the Traffic Policy Instance creates in the specified hosted zone.
     */
    @Updatable
    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    /**
     * The state of the Traffic Policy Instance.
     */
    @Output
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The ID of the Traffic Policy Instance.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(TrafficPolicyInstance trafficPolicyInstance) {
        setId(trafficPolicyInstance.id());
        setMessage(trafficPolicyInstance.message());
        setHostedZone(findById(HostedZoneResource.class, trafficPolicyInstance.hostedZoneId()));
        setType(trafficPolicyInstance.trafficPolicyTypeAsString());
        setTtl(trafficPolicyInstance.ttl());
        setState(trafficPolicyInstance.state());
        setTrafficPolicy(findById(TrafficPolicyResource.class, trafficPolicyInstance.trafficPolicyId()));
    }

    @Override
    public boolean refresh() {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        TrafficPolicyInstance trafficPolicyInstance = getTrafficPolicyInstance(client);

        if (trafficPolicyInstance == null) {
            return false;
        }

        copyFrom(trafficPolicyInstance);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        CreateTrafficPolicyInstanceResponse response = client.createTrafficPolicyInstance(
            r -> r.name(getName() + getHostedZone().getName())
                .hostedZoneId(getHostedZone().getId())
                .trafficPolicyId(getTrafficPolicy().getId())
                .trafficPolicyVersion(getTrafficPolicy().getVersion())
                .ttl(getTtl())
        );

        TrafficPolicyInstance trafficPolicyInstance = response.trafficPolicyInstance();
        setId(trafficPolicyInstance.id());

        state.save();

        boolean waitResult = Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .prompt(false)
            .until(() -> isTrafficPolicyInstanceReady(client));

        if (!waitResult) {
            throw new GyroException("Unable to reach 'Applied' state for route53 traffic policy - " + getName());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        client.updateTrafficPolicyInstance(
            r -> r.id(getId())
                .trafficPolicyId(getTrafficPolicy().getId())
                .trafficPolicyVersion(getTrafficPolicy().getVersion())
                .ttl(getTtl())
        );

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(3, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.UPDATE)
            .prompt(true)
            .until(() -> isTrafficPolicyInstanceReady(client));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteTrafficPolicyInstance(
            r -> r.id(getId())
        );

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(5, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .prompt(true)
            .until(() -> getTrafficPolicyInstance(client) == null);
    }

    private TrafficPolicyInstance getTrafficPolicyInstance(Route53Client client) {
        TrafficPolicyInstance trafficPolicyInstance = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load traffic policy instance.");
        }

        try {
            GetTrafficPolicyInstanceResponse response = client.getTrafficPolicyInstance(
                r -> r.id(getId())
            );

            trafficPolicyInstance = response.trafficPolicyInstance();
        } catch (NoSuchTrafficPolicyInstanceException ignore) {
            // ignore
        }

        return trafficPolicyInstance;
    }

    private boolean isTrafficPolicyInstanceReady(Route53Client client) {
        TrafficPolicyInstance trafficPolicyInstance = getTrafficPolicyInstance(client);

        return trafficPolicyInstance != null && trafficPolicyInstance.state().equals("Applied");
    }
}
