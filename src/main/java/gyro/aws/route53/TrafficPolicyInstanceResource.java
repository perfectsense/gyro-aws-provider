package gyro.aws.route53;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
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
 *     aws::traffic-policy-instance traffic-policy-instance-example
 *         name: "traffic-policy-instance-example"
 *         comment: "traffic-policy-instance-example Comment"
 *         version: 1
 *         ttl: 900
 *         hosted-zone: $(aws::hosted-zone hosted-zone-record-set-example)
 *         traffic-policy: $(aws::traffic-policy traffic-policy-example-instance)
 *     end
 *
 */
@Type("traffic-policy-instance")
public class TrafficPolicyInstanceResource extends AwsResource implements Copyable<TrafficPolicyInstance> {
    private String name;
    private String message;
    private HostedZoneResource hostedZone;
    private TrafficPolicyResource trafficPolicy;
    private String type;
    private Long ttl;
    private String state;
    private String trafficPolicyInstanceId;

    /**
     * Name of the traffic policy instance. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Message for the traffic policy instance.
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Id of the associated hosted zone. (Required)
     */
    public HostedZoneResource getHostedZone() {
        return hostedZone;
    }

    public void setHostedZone(HostedZoneResource hostedZone) {
        this.hostedZone = hostedZone;
    }

    /**
     * The traffic policy to be associated. (Required)
     */
    @Updatable
    public TrafficPolicyResource getTrafficPolicy() {
        return trafficPolicy;
    }

    public void setTrafficPolicy(TrafficPolicyResource trafficPolicy) {
        this.trafficPolicy = trafficPolicy;
    }

    /**
     *  The type of the traffic policy instance.
     */
    @Output
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The TTL that you want to assign to all of the resource record sets that the policy creates in the specified hosted zone.
     */
    @Updatable
    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    /**
     * The state of the traffic policy instance.
     */
    @Output
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The ID of the traffic policy instance.
     */
    @Id
    @Output
    public String getTrafficPolicyInstanceId() {
        return trafficPolicyInstanceId;
    }

    public void setTrafficPolicyInstanceId(String trafficPolicyInstanceId) {
        this.trafficPolicyInstanceId = trafficPolicyInstanceId;
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
    public void create() {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        CreateTrafficPolicyInstanceResponse response = client.createTrafficPolicyInstance(
            r -> r.name(getName() + getHostedZone().getHostedZoneName())
                .hostedZoneId(getHostedZone().getHostedZoneId())
                .trafficPolicyId(getTrafficPolicy().getTrafficPolicyId())
                .trafficPolicyVersion(getTrafficPolicy().getVersion())
                .ttl(getTtl())
        );

        TrafficPolicyInstance trafficPolicyInstance = response.trafficPolicyInstance();
        setTrafficPolicyInstanceId(trafficPolicyInstance.id());

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(3, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isTrafficPolicyInstanceReady(client));
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        client.updateTrafficPolicyInstance(
            r -> r.id(getTrafficPolicyInstanceId())
                .trafficPolicyId(getTrafficPolicy().getTrafficPolicyId())
                .trafficPolicyVersion(getTrafficPolicy().getVersion())
                .ttl(getTtl())
        );

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(3, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isTrafficPolicyInstanceReady(client));
    }

    @Override
    public void delete() {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteTrafficPolicyInstance(
            r -> r.id(getTrafficPolicyInstanceId())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("traffic policy instance - ");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(getName());
        }

        if (getHostedZone() != null) {
            sb.append(getHostedZone().getHostedZoneName());
        }

        if (!ObjectUtils.isBlank(getTrafficPolicyInstanceId())) {
            sb.append(" - ").append(getTrafficPolicyInstanceId());

        }

        return sb.toString();
    }

    @Override
    public void copyFrom(TrafficPolicyInstance trafficPolicyInstance) {
        setTrafficPolicyInstanceId(trafficPolicyInstance.id());
        setMessage(trafficPolicyInstance.message());
        setHostedZone(findById(HostedZoneResource.class, trafficPolicyInstance.hostedZoneId()));
        setType(trafficPolicyInstance.trafficPolicyTypeAsString());
        setTtl(trafficPolicyInstance.ttl());
        setState(trafficPolicyInstance.state());
        setTrafficPolicy(findById(TrafficPolicyResource.class, trafficPolicyInstance.trafficPolicyId()));
    }

    private TrafficPolicyInstance getTrafficPolicyInstance(Route53Client client) {
        TrafficPolicyInstance trafficPolicyInstance = null;

        if (ObjectUtils.isBlank(getTrafficPolicyInstanceId())) {
            throw new GyroException("traffic-policy-instance-id is missing, unable to load traffic policy instance.");
        }

        try {
            GetTrafficPolicyInstanceResponse response = client.getTrafficPolicyInstance(
                r -> r.id(getTrafficPolicyInstanceId())
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
