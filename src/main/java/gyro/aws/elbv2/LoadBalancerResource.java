package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import com.psddev.dari.util.CompactMap;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTagsResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerNotFoundException;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class LoadBalancerResource extends AwsResource {

    private String dnsName;
    private String ipAddressType;
    private String arn;
    private String name;
    private String scheme;
    private Map<String, String> tags;

    /**
     *  Public DNS name for the alb
     */
    public String getDnsName() {
        return ipAddressType;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    /**
     *  Type of IP address used by the subnets of the alb (Required)
     */
    public String getIpAddressType() {
        return ipAddressType;
    }

    public void setIpAddressType(String ipAddressType) {
        this.ipAddressType = ipAddressType;
    }

    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     *  The name of the load balancer (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *  Type of nodes used by the alb (Optional)
     */
    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     *  List of tags associated with the alb (Optional)
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new CompactMap<>();
        }
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        if (this.tags != null && tags != null) {
            this.tags.putAll(tags);

        } else {
            this.tags = tags;
        }
    }

    public LoadBalancer internalRefresh() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        try {
            DescribeLoadBalancersResponse lbResponse = client.describeLoadBalancers(r -> r.loadBalancerArns(getArn()));

            LoadBalancer lb = lbResponse.loadBalancers().get(0);
            setDnsName(lb.dnsName());
            setIpAddressType(lb.ipAddressTypeAsString());
            setArn(lb.loadBalancerArn());
            setName(lb.loadBalancerName());
            setScheme(lb.schemeAsString());

            getTags().clear();
            DescribeTagsResponse tagResponse = client.describeTags(r -> r.resourceArns(getArn()));
            if (tagResponse != null) {
                List<Tag> tags = tagResponse.tagDescriptions().get(0).tags();
                for (Tag tag : tags) {
                    getTags().put(tag.key(), tag.value());
                }
            }

            return lb;

        } catch (LoadBalancerNotFoundException ex) {
            return null;
        }
    }


    @Override
    public void create() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        if (!getTags().isEmpty()) {
            List<Tag> tag = new ArrayList<>();
            getTags().forEach((key, value) -> tag.add(Tag.builder().key(key).value(value).build()));
            client.addTags(r -> r.tags(tag)
                    .resourceArns(getArn()));
        }
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        LoadBalancerResource currentResource = (LoadBalancerResource) current;

        Map<String, String> tagAdditions = new HashMap<>(getTags());
        currentResource.getTags().forEach((key, value) -> tagAdditions.remove(key, value));

        Map<String, String> tagSubtractions = new HashMap<>(currentResource.getTags());
        getTags().forEach((key, value) -> tagSubtractions.remove(key, value));

        if (!tagAdditions.isEmpty()) {
            List<Tag> tag = new ArrayList<>();
            tagAdditions.forEach((key, value) -> tag.add(Tag.builder().key(key).value(value).build()));
            client.addTags(r -> r.tags(tag)
                    .resourceArns(getArn()));
        }

        if (!tagSubtractions.isEmpty()) {
            List<String> tag = new ArrayList<>();
            tagSubtractions.forEach((key, value) -> tag.add(key));
            client.removeTags(r -> r.tagKeys(tag)
                    .resourceArns(getArn()));
        }
    }

    @Override
    public void delete() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.deleteLoadBalancer(r -> r.loadBalancerArn(getArn()));
    }

    @Override
    public String toDisplayString() {
        return "load balancer " + getName();
    }
}
