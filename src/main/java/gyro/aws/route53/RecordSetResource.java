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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroCore;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.Change;
import software.amazon.awssdk.services.route53.model.ChangeAction;
import software.amazon.awssdk.services.route53.model.HostedZoneNotFoundException;
import software.amazon.awssdk.services.route53.model.NoSuchHostedZoneException;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

/**
 * Creates a record set in the given hosted zone.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::route53-record-set record-set-example
 *         hosted-zone: $(aws::route53-hosted-zone hosted-zone-record-set-example)
 *         name: "record-set-example."
 *         type: "A"
 *         ttl: 300
 *         records: [
 *             "192.0.2.235",
 *             "192.0.2.236"
 *         ]
 *         failover: "secondary"
 *         set-identifier: "set_id"
 *         routing-policy: "failover"
 *         health-check: $(aws::route53-health-check health-check-record-set-example-calculated)
 *     end
 *
 * .. code-block:: gyro
 *
 *     aws::route53-record-set record-set-geolocation-example
 *         hosted-zone: $(aws::route53-hosted-zone hosted-zone-record-set-example)
 *         name: "record-set-geolocation-example."
 *         type: "A"
 *         ttl: 300
 *         records: [
 *             "192.0.2.235",
 *             "192.0.2.236"
 *         ]
 *         set-identifier: "set_id"
 *         routing-policy: "geolocation"
 *
 *         geolocation
 *             country-code: 'US'
 *         end
 *     end
 *
 * .. code-block:: gyro
 *
 *     aws::route53-record-set record-set-alias-example
 *         hosted-zone: $(aws::route53-hosted-zone hosted-zone-record-set-example)
 *         name: "record-set-alias-example."
 *         type: "A"
 *
 *         alias
 *             hosted-zone-id: $(aws::load-balancer elb).hosted-zone-id
 *             evaluate-target-health: false
 *             dns-name: $(aws::load-balancer elb).*.dns-name
 *         end
 *     end
 */
@Type("route53-record-set")
public class RecordSetResource extends AwsResource implements Copyable<ResourceRecordSet> {
    private AliasTarget alias;
    private String comment;
    private String failover;
    private Geolocation geolocation;
    private HostedZoneResource hostedZone;
    private HealthCheckResource healthCheck;
    private Boolean multiValueAnswer;
    private String name;
    private String region;
    private String setIdentifier;
    private TrafficPolicyInstanceResource trafficPolicyInstance;
    private Long ttl;
    private String type;
    private Long weight;
    private Set<String> records;
    private String routingPolicy;
    private String id;

    private static transient Map<String, List<ResourceRecordSet>> recordSetCache;

    private static final Set<String> ROUTING_POLICY_SET = ImmutableSet.of("geolocation", "failover", "multivalue", "weighted", "latency", "simple");

    /**
     * The alias target of the record.
     *
     * @subresource gyro.aws.route53.AliasTarget
     */
    @Updatable
    public AliasTarget getAlias() {
        return alias;
    }

    public void setAlias(AliasTarget alias) {
        this.alias = alias;
    }

    /**
     * A comment when creating/updating/deleting a Record Set.
     */
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * The failover value. Required if 'route policy' set to ``failover``.
     */
    @Updatable
    @ValidStrings({"Primary", "Secondary"})
    public String getFailover() {
        return failover != null ? failover.toUpperCase() : null;
    }

    public void setFailover(String failover) {
        this.failover = failover;
    }

    /**
     * The geolocation configuration of the record.
     *
     * @subresource gyro.aws.route53.Geolocation
     */
    @Updatable
    public Geolocation getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(Geolocation geolocation) {
        this.geolocation = geolocation;
    }

    /**
     * The Hosted Zone under which the the Record Set is to be created.
     */
    @Required
    public HostedZoneResource getHostedZone() {
        return hostedZone;
    }

    public void setHostedZone(HostedZoneResource hostedZone) {
        this.hostedZone = hostedZone;
    }

    /**
     * The health check to be associated with the Record Set. Required if 'failover' is set to ``primary``.
     */
    @Updatable
    public HealthCheckResource getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheckResource healthCheck) {
        this.healthCheck = healthCheck;
    }

    /**
     * Needs to be enabled if Routing Policy is ``multivalue``. Required if 'route policy' set to ``multivalue``.
     */
    @Updatable
    public Boolean getMultiValueAnswer() {
        return multiValueAnswer;
    }

    public void setMultiValueAnswer(Boolean multiValueAnswer) {
        this.multiValueAnswer = multiValueAnswer;
    }

    /**
     * The name of the Record Set being created.
     */
    @Required
    @Updatable
    public String getName() {
        if (name != null) {
            name = StringUtils.ensureEnd(name, ".");
        }

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The region where the records mentioned resides. Required if 'route policy' set to ``latency``.
     */
    @Updatable
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * A set identifier that differentiates this from other Record Set of the same type and Routing Policy. Required if 'enable alias' is set to ``false``.
     */
    @Updatable
    public String getSetIdentifier() {
        return setIdentifier;
    }

    public void setSetIdentifier(String setIdentifier) {
        this.setIdentifier = setIdentifier;
    }

    /**
     * The Traffic Policy instance to be associated with the record set.
     */
    @Updatable
    public TrafficPolicyInstanceResource getTrafficPolicyInstance() {
        return trafficPolicyInstance;
    }

    public void setTrafficPolicyInstance(TrafficPolicyInstanceResource trafficPolicyInstance) {
        this.trafficPolicyInstance = trafficPolicyInstance;
    }

    /**
     * The resource record cache time to live. Required if 'enable alias' is set to ``false``.
     */
    @Updatable
    @Range(min = 0, max = 172800)
    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    /**
     * The type of Record Set being created.
     */
    @Required
    @Updatable
    @ValidStrings({"SOA", "A", "TXT", "NS", "CNAME", "MX", "NAPTR", "PTR", "SRV", "SPF", "AAAA", "CAA"})
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The weight value determines the probability of a Record Set being selected. Required if 'route policy' set to ``weighted``.
     */
    @Updatable
    @Range(min = 0, max = 255)
    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    /**
     * A list of ip addresses for the Record Set. Required if 'enable alias' is set to ``false``.
     */
    @Updatable
    public Set<String> getRecords() {
        if (records == null) {
            records = new HashSet<>();
        }

        return records;
    }

    public void setRecords(Set<String> records) {
        this.records = records;
    }

    /**
     * Routing policy type the Record Set is going to be. Defaults to ``Simple``.
     */
    @Updatable
    @ValidStrings({"geolocation", "failover", "multivalue", "weighted", "latency", "simple"})
    public String getRoutingPolicy() {
        if (routingPolicy == null) {
            routingPolicy = "simple";
        }

        return routingPolicy.toLowerCase();
    }

    public void setRoutingPolicy(String routingPolicy) {
        this.routingPolicy = routingPolicy;
    }

    /**
     * The ID of the Record Set.
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
    public void copyFrom(ResourceRecordSet recordSet) {
        setName(recordSet.name().replace("\\052", "*"));
        setType(recordSet.typeAsString());
        setFailover(recordSet.failoverAsString());
        setHealthCheck(findById(HealthCheckResource.class, recordSet.healthCheckId()));
        setMultiValueAnswer(recordSet.multiValueAnswer());
        setRegion(recordSet.regionAsString());
        setWeight(recordSet.weight());
        setTrafficPolicyInstance(findById(TrafficPolicyInstanceResource.class, recordSet.trafficPolicyInstanceId()));
        setTtl(recordSet.ttl());
        setRecords(recordSet.resourceRecords().stream().map(ResourceRecord::value).collect(Collectors.toSet()));
        setId(String.format("%s %s", getName(), getType()));

        if (recordSet.aliasTarget() != null) {
            setAlias(newSubresource(AliasTarget.class));
            getAlias().setDnsName(recordSet.aliasTarget().dnsName());
            getAlias().setEvaluateTargetHealth(recordSet.aliasTarget().evaluateTargetHealth());
            getAlias().setHostedZoneId(recordSet.aliasTarget().hostedZoneId());
        }

        if (recordSet.geoLocation() != null) {
            setGeolocation(newSubresource(Geolocation.class));
            getGeolocation().setCountryCode(recordSet.geoLocation().countryCode());
            getGeolocation().setContinentCode(recordSet.geoLocation().continentCode());
            getGeolocation().setSubdivisionCode(recordSet.geoLocation().subdivisionCode());
        }
    }

    @Override
    public boolean refresh() {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        List<ResourceRecordSet> records = getResourceRecordSets(client, getHostedZone().getId());
        ResourceRecordSet recordSet = getResourceRecordSet(records);

        if (recordSet == null) {
            return false;
        }

        copyFrom(recordSet);

        return true;
    }

    @Override
    public Map<? extends Resource, Boolean> batchRefresh(List<? extends Resource> resources) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        Map<RecordSetResource, Boolean> refreshStatus = new HashMap<>();
        Map<String, List<ResourceRecordSet>> zoneRecordCache = new HashMap<>();

        for (Resource resource : resources) {
            RecordSetResource recordSetResource = (RecordSetResource) resource;
            String hostedZoneId = recordSetResource.getHostedZone().getId();

            List<ResourceRecordSet> recordSets = zoneRecordCache.computeIfAbsent(hostedZoneId,
                    m -> RecordSetResource.getResourceRecordSets(client, m));

            ResourceRecordSet recordSet = recordSetResource.getResourceRecordSet(recordSets);

            if (recordSet == null) {
                refreshStatus.put(recordSetResource, false);
            } else {
                recordSetResource.copyFrom(recordSet);
            }

            refreshStatus.put(recordSetResource, true);
        }

        return refreshStatus;
    }

    @Override
    public void create(GyroUI ui, State state) {
        validate();

        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        if (getType().equals("NS") || getType().equals("SOA")) {
            saveResourceRecordSet(client,this, ChangeAction.UPSERT);
        } else {
            saveResourceRecordSet(client,this, ChangeAction.CREATE);
        }

        setId(String.format("%s %s", getName(), getType()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        validate();

        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        RecordSetResource oldResource = (RecordSetResource) current;

        if (changedFieldNames.contains("name") || changedFieldNames.contains("set-identifier")) {
            saveResourceRecordSet(client, oldResource, ChangeAction.DELETE);
        }

        saveResourceRecordSet(client, this, ChangeAction.UPSERT);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        saveResourceRecordSet(client, this, ChangeAction.DELETE);
    }

    private static List<ResourceRecordSet> getResourceRecordSets(Route53Client client, String hostedZoneId) {
        try {
            return client.listResourceRecordSetsPaginator(
                r -> r.hostedZoneId(hostedZoneId)
            ).resourceRecordSets().stream().collect(Collectors.toList());

        } catch (HostedZoneNotFoundException | NoSuchHostedZoneException ignore) {
            return new ArrayList<>();
        }
    }

    private ResourceRecordSet getResourceRecordSet(List<ResourceRecordSet> records) {
        ResourceRecordSet recordSet = null;

        if (!records.isEmpty()) {
            recordSet = records
                .stream()
                .filter(o -> o.name().equals(getName().replace("*", "\\052")))
                .filter(o -> o.type().name().equalsIgnoreCase(getType()))
                .findFirst()
                .orElse(null);
        }

        return recordSet;
    }

    private void saveResourceRecordSet(Route53Client client, RecordSetResource recordSetResource, ChangeAction changeAction) {
        ResourceRecordSet.Builder recordSetBuilder = ResourceRecordSet.builder()
            .name(recordSetResource.getName())
            .healthCheckId(recordSetResource.getHealthCheck() != null ? recordSetResource.getHealthCheck().getId() : null)
            .setIdentifier(recordSetResource.getSetIdentifier())
            .trafficPolicyInstanceId(recordSetResource.getTrafficPolicyInstance() != null ? recordSetResource.getTrafficPolicyInstance().getId() : null)
            .type(recordSetResource.getType());

        if (recordSetResource.getAlias() != null) {
            AliasTarget alias = recordSetResource.getAlias();
            recordSetBuilder.aliasTarget(
                a -> a.dnsName(alias.getDnsName())
                    .evaluateTargetHealth(alias.getEvaluateTargetHealth())
                    .hostedZoneId(alias.getHostedZoneId()));
        } else {
            recordSetBuilder.resourceRecords(recordSetResource.getRecords().stream()
                .map(o -> ResourceRecord.builder().value(o).build())
                .collect(Collectors.toList()))
                .ttl(recordSetResource.getTtl());
        }

        switch (recordSetResource.getRoutingPolicy()) {
            case "geolocation":
                Geolocation geolocation = recordSetResource.getGeolocation();
                recordSetBuilder.geoLocation(
                    g -> g.continentCode(geolocation.getContinentCode())
                        .countryCode(geolocation.getCountryCode())
                        .subdivisionCode(geolocation.getSubdivisionCode()));
                break;
            case "failover":
                recordSetBuilder.failover(recordSetResource.getFailover());
                break;
            case "multivalue":
                recordSetBuilder.multiValueAnswer(recordSetResource.getMultiValueAnswer());
                break;
            case "weighted":
                recordSetBuilder.weight(recordSetResource.getWeight());
                break;
            case "latency":
                recordSetBuilder.region(recordSetResource.getRegion());
                break;
            default: break;
        }

        Change change = Change.builder()
            .action(changeAction)
            .resourceRecordSet(recordSetBuilder.build())
            .build();

        client.changeResourceRecordSets(
            r -> r.hostedZoneId(recordSetResource.getHostedZone().getId())
                .changeBatch(
                    c -> c.comment(recordSetResource.getComment())
                        .changes(change)
                )
        );
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (! ROUTING_POLICY_SET.contains(getRoutingPolicy())) {
            errors.add(new ValidationError(this, null, String.format("The value - (%s) is invalid for parameter 'routing-policy'."
                + " Valid values [ '%s' ].",getRoutingPolicy(),String.join("', '", ROUTING_POLICY_SET))));
        }

        if (alias != null) {
            if (!ObjectUtils.isBlank(getTtl())) {
                errors.add(new ValidationError(this, null, "The param 'ttl' is not allowed when 'alias' is set."));
            }

            if (!getRecords().isEmpty()) {
                errors.add(new ValidationError(this, null, "The param 'records' is not allowed when 'alias' is set."));
            }

        } else {
            if (ObjectUtils.isBlank(getTtl()) || getTtl() < 0 || getTtl() > 172800) {
                errors.add(new ValidationError(this, null, "The param 'ttl' is required when 'alias' is not set."
                    + " Valid values [ Long 0 - 172800 ]."));
            }

            if (getRecords().isEmpty()) {
                errors.add(new ValidationError(this, null, "The param 'records' is required when 'alias' is not set."));
            }
        }

        if (!getRoutingPolicy().equals("geolocation") && getGeolocation() != null) {
            errors.add(new ValidationError(this, null, "The param 'geolocation' is not allowed when 'routing-policy' is not set to 'geolocation'."));

        } else if (getRoutingPolicy().equals("geolocation") && getGeolocation() == null) {
            errors.add(new ValidationError(this, null, "The param 'geolocation' is required when 'routing-policy' is set to 'geolocation'."));
        }

        if (!getRoutingPolicy().equals("failover") && getFailover() != null) {
            errors.add(new ValidationError(this, null, "The param 'failover' is not allowed when 'routing-policy' is not set to 'failover'."));
        } else if (getRoutingPolicy().equals("failover")
            && (ObjectUtils.isBlank(getFailover()) || (!getFailover().equals("PRIMARY") && !getFailover().equals("SECONDARY")))) {
            errors.add(new ValidationError(this, null, "The param 'failover' is required when 'routing-policy' is set to 'failover'."
                + " Valid values [ PRIMARY, SECONDARY ]."));
        }

        if (!getRoutingPolicy().equals("multivalue") && getMultiValueAnswer() != null) {
            errors.add(new ValidationError(this, null, "The param 'multi-value-answer' is not allowed when 'routing-policy' is not set to 'multivalue'."));
        } else if (getRoutingPolicy().equals("multivalue")) {
            if (getMultiValueAnswer() == null) {
                errors.add(new ValidationError(this, null, "The param 'multi-value-answer' is required when 'routing-policy' is set to 'multivalue'."));
            }

            if (getRecords().size() > 1) {
                errors.add(new ValidationError(this, null, "The param 'records' can only have one value when 'routing-policy' is set to 'multivalue'."));
            }
        }

        if (!getRoutingPolicy().equals("weighted") && getWeight() != null) {
            errors.add(new ValidationError(this, null, "The param 'weight' is not allowed when 'routing-policy' is not set to 'weighted'."));
        } else if (getRoutingPolicy().equals("weighted")) {
            if ((getWeight() == null) || getWeight() < 0 || getWeight() > 255) {
                errors.add(new ValidationError(this, null, "The param 'weight' is required when 'routing-policy' is set to 'weighted'."
                    + ""));
            }
        }

        if (!getRoutingPolicy().equals("latency") && getRegion() != null) {
            errors.add(new ValidationError(this, null, "The param 'region' is not allowed when 'routing-policy' is not set to 'latency'."));
        } else if (getRoutingPolicy().equals("latency") && ObjectUtils.isBlank(getRegion())) {
            errors.add(new ValidationError(this, null, "The param 'region' is required when 'routing-policy' is set to 'latency'."));
        }

        return errors;
    }
}
