package gyro.aws.globalaccelerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.globalaccelerator.GlobalAcceleratorClient;
import software.amazon.awssdk.services.globalaccelerator.model.Accelerator;
import software.amazon.awssdk.services.globalaccelerator.model.AcceleratorNotFoundException;
import software.amazon.awssdk.services.globalaccelerator.model.AcceleratorStatus;
import software.amazon.awssdk.services.globalaccelerator.model.CreateAcceleratorResponse;
import software.amazon.awssdk.services.globalaccelerator.model.DescribeAcceleratorAttributesResponse;
import software.amazon.awssdk.services.globalaccelerator.model.DescribeAcceleratorResponse;
import software.amazon.awssdk.services.globalaccelerator.model.IpAddressType;
import software.amazon.awssdk.services.globalaccelerator.model.Tag;

import static software.amazon.awssdk.services.globalaccelerator.model.AcceleratorStatus.*;

@Type("global-accelerator")
public class AcceleratorResource extends AwsResource implements Copyable<Accelerator> {

    private Boolean enabled;
    private String name;
    private List<String> ipAddresses;
    private IpAddressType ipAddressType;
    private Map<String, String> tags;

    private AcceleratorAttributes attributes;

    // -- Output fields
    private String arn;
    private String dnsName;
    private AcceleratorStatus status;
    private List<AcceleratorIpSet> ipSets;

    @Override
    public boolean refresh() {
        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        try {
            DescribeAcceleratorResponse response = client.describeAccelerator(r -> r.acceleratorArn(getArn()));
            copyFrom(response.accelerator());
        } catch (AcceleratorNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        CreateAcceleratorResponse response = client.createAccelerator(r -> r
            .enabled(getEnabled())
            .ipAddresses(getIpAddresses())
            .ipAddressType(getIpAddressType())
            .name(getName())
        );

        setArn(response.accelerator().acceleratorArn());
        setDnsName(response.accelerator().dnsName());
        setStatus(response.accelerator().status());

        if (response.accelerator().hasIpSets()) {
            setIpSets_(response.accelerator().ipSets());
        }

        state.save();

        Wait.atMost(20, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .until(() -> {
                Accelerator accelerator = accelerator(client);
                return accelerator != null && accelerator.status() == DEPLOYED;
            });

        if (getAttributes() != null) {
            client.updateAcceleratorAttributes(r -> r
                .acceleratorArn(getArn())
                .flowLogsEnabled(getAttributes().getFlowLogsEnabled())
                .flowLogsS3Bucket(getAttributes().getFlowLogsS3Bucket())
                .flowLogsS3Prefix(getAttributes().getFlowLogsS3Prefix())
            );
        }

        client.tagResource(r -> r
            .resourceArn(getArn())
            .tags(tagsToAdd())
        );
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        client.updateAccelerator(r -> r.acceleratorArn(getArn())
            .enabled(getEnabled())
            .name(getName())
            .ipAddressType(getIpAddressType())
        );

        if (changedFieldNames.contains("attributes")) {
            client.updateAcceleratorAttributes(r -> r
                .acceleratorArn(getArn())
                .flowLogsEnabled(getAttributes().getFlowLogsEnabled())
                .flowLogsS3Bucket(getAttributes().getFlowLogsS3Bucket())
                .flowLogsS3Prefix(getAttributes().getFlowLogsS3Prefix())
            );
        }

        client.tagResource(r -> r
            .resourceArn(getArn())
            .tags(tagsToAdd())
        );

        List<String> tagKeysToRemove = tagsToRemove((AcceleratorResource) current);
        if (!tagKeysToRemove.isEmpty()) {
            client.untagResource(r -> r
                .resourceArn(getArn())
                .tagKeys(tagKeysToRemove)
            );
        }

        Wait.atMost(20, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .until(() -> {
                Accelerator accelerator = accelerator(client);
                return accelerator != null && accelerator.status() == DEPLOYED;
            });
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        client.updateAccelerator(r -> r.acceleratorArn(getArn()).enabled(false));

        Wait.atMost(20, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .until(() -> {
                Accelerator accelerator = accelerator(client);
                return accelerator != null && accelerator.status() == DEPLOYED;
            });

        client.deleteAccelerator(r -> r.acceleratorArn(getArn()));
    }

    /**
     * Whether the accelerator is enabled.
     */
    @Updatable
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The name of the accelerator.
     */
    @Updatable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A list of ip addresses when using `BYOIP <https://docs.aws.amazon.com/global-accelerator/latest/dg/using-byoip.html>`_.
     */
    public List<String> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    /**
     * The type of IP addresses. Currently only ``IPV4`` is supported.
     */
    @Updatable
    public IpAddressType getIpAddressType() {
        if (ipAddressType == null) {
            ipAddressType = IpAddressType.IPV4;
        }

        return ipAddressType;
    }

    public void setIpAddressType(IpAddressType ipAddressType) {
        this.ipAddressType = ipAddressType;
    }

    /**
     * Tags for the accelerator.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * Attributes for this accelerator. Current supports enabling flow logs.
     *
     * @subresource gyro.aws.globalaccelerator.AcceleratorAttributes
     */
    public AcceleratorAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(AcceleratorAttributes attributes) {
        this.attributes = attributes;
    }

    /**
     * The Amazon Resource Name (ARN) of the accelerator.
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
     * The DNS name that points to your accelerator.
     */
    @Output
    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    /**
     * The deployment status of the accelerator.
     */
    @Output
    public AcceleratorStatus getStatus() {
        return status;
    }

    public void setStatus(AcceleratorStatus status) {
        this.status = status;
    }

    /**
     * The static IPs assigned to the accelerator.
     *
     * @subresource gyro.aws.globalaccelerator.AcceleratorIpSet
     */
    @Output
    public List<AcceleratorIpSet> getIpSets() {
        if (ipSets == null) {
            ipSets = new ArrayList<>();
        }

        return ipSets;
    }

    public void setIpSets(List<AcceleratorIpSet> ipSets) {
        this.ipSets = ipSets;
    }

    public void setIpSets_(List<software.amazon.awssdk.services.globalaccelerator.model.IpSet> ipSets_) {
        if (!ipSets_.isEmpty()) {
            for (software.amazon.awssdk.services.globalaccelerator.model.IpSet ipSet_ : ipSets_) {
                AcceleratorIpSet ipSet = newSubresource(AcceleratorIpSet.class);
                ipSet.copyFrom(ipSet_);

                getIpSets().add(ipSet);
            }
        }
    }

    @Override
    public void copyFrom(Accelerator accelerator) {
        setArn(accelerator.acceleratorArn());
        setStatus(accelerator.status());
        setDnsName(accelerator.dnsName());
        setEnabled(accelerator.enabled());
        setName(accelerator.name());
        setIpSets_(accelerator.ipSets());

        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        AcceleratorAttributes attributes = newSubresource(AcceleratorAttributes.class);
        DescribeAcceleratorAttributesResponse response = client.describeAcceleratorAttributes(r -> r.acceleratorArn(getArn()));
        attributes.setFlowLogsEnabled(response.acceleratorAttributes().flowLogsEnabled());
        attributes.setFlowLogsS3Bucket(response.acceleratorAttributes().flowLogsS3Bucket());
        attributes.setFlowLogsS3Prefix(response.acceleratorAttributes().flowLogsS3Prefix());

        setAttributes(attributes);
    }

    private Accelerator accelerator(GlobalAcceleratorClient client) {
        return client.describeAccelerator(r -> r.acceleratorArn(getArn())).accelerator();
    }

    private List<Tag> tagsToAdd() {
        return getTags().entrySet().stream()
            .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
            .collect(Collectors.toList());
    }

    private List<String> tagsToRemove(AcceleratorResource current) {
        Set<String> currentKeys = new HashSet<>(current.getTags().keySet());
        currentKeys.removeAll(getTags().keySet());

        return new ArrayList<>(currentKeys);
    }
}
