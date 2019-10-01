package gyro.aws.route53;

import com.psddev.dari.util.StringUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.google.common.collect.ImmutableSet;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.Change;
import software.amazon.awssdk.services.route53.model.ChangeAction;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 */
@Type("route53-record-set")
public class RecordSetResource extends AwsResource implements Copyable<ResourceRecordSet> {
    private String comment;
    private String continentCode;
    private String countryCode;
    private String dnsName;
    private Boolean evaluateTargetHealth;
    private String failover;
    private HostedZoneResource hostedZone;
    private HealthCheckResource healthCheck;
    private Boolean multiValueAnswer;
    private String name;
    private String region;
    private String setIdentifier;
    private String subdivisionCode;
    private TrafficPolicyInstanceResource trafficPolicyInstance;
    private Long ttl;
    private String type;
    private Long weight;
    private List<String> records;
    private String routingPolicy;
    private Boolean enableAlias;
    private String aliasHostedZoneId;
    private String id;

    private static final Set<String> ROUTING_POLICY_SET = ImmutableSet.of("geolocation", "failover", "multivalue", "weighted", "latency", "simple");

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
     * The continent code. At least one of continent code, country code or subdivision code required if type selected as ``geolocation``.
     */
    @Updatable
    public String getContinentCode() {
        return continentCode != null ? continentCode.toUpperCase() : null;
    }

    public void setContinentCode(String continentCode) {
        this.continentCode = continentCode;
    }

    /**
     * The country code. At least one of continent code, country code or subdivision code required if 'type' selected as ``geolocation``.
     */
    @Updatable
    public String getCountryCode() {
        return countryCode != null ? countryCode.toUpperCase() : null;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Dns name to associate with this Record Set. Required if 'enable alias' is set to ``true``.
     */
    @Updatable
    public String getDnsName() {

        if (dnsName != null) {
            dnsName = StringUtils.ensureEnd(dnsName, ".");
        }

        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    /**
     * Enable target health evaluation with this Record Set. Required if 'enable alias' is set to ``true``.
     */
    @Updatable
    public Boolean getEvaluateTargetHealth() {
        return evaluateTargetHealth;
    }

    public void setEvaluateTargetHealth(Boolean evaluateTargetHealth) {
        this.evaluateTargetHealth = evaluateTargetHealth;
    }

    /**
     * The failover value. Valid values [ Primary, Secondary]. Required if 'route policy' set to ``failover``.
     */
    @Updatable
    public String getFailover() {
        return failover != null ? failover.toUpperCase() : null;
    }

    public void setFailover(String failover) {
        this.failover = failover;
    }

    /**
     * The Hosted Zone under which the the Record Set is to be created. (Required)
     */
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
     * The name of the Record Set being created. (Required)
     */
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
     * The sub division code. At least one of continent code, country code or subdivision code required if type selected as ``geolocation``.
     */
    @Updatable
    public String getSubdivisionCode() {
        return subdivisionCode != null ? subdivisionCode.toUpperCase() : null;
    }

    public void setSubdivisionCode(String subdivisionCode) {
        this.subdivisionCode = subdivisionCode;
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
     * The resource record cache time to live. Valid values [ 0 - 172800]. Required if 'enable alias' is set to ``false``.
     */
    @Updatable
    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    /**
     * The type of Record Set being created. Valid values are ``SOA`` or ``A`` or ``TXT`` or ``NS`` or ``CNAME`` or ``MX`` or ``NAPTR`` or ``PTR`` or ``SRV`` or ``SPF`` or ``AAAA`` or ``CAA``. (Required)
     */
    @Updatable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The weight value determines the probability of a Record Set being selected. Valid values ``[ 0 - 255]``. Required if 'route policy' set to ``weighted``.
     */
    @Updatable
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
    public List<String> getRecords() {
        if (records == null) {
            records = new ArrayList<>();
        }

        return records;
    }

    public void setRecords(List<String> records) {
        this.records = records;
    }

    /**
     * Routing policy type the Record Set is going to be. Defaults to Simple. Valid Values are ``geolocation`` or ``failover`` or ``multivalue`` or ``weighted`` or ``latency`` or ``simple``.
     */
    @Updatable
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
     * Enable alias. Defaults to false.
     */
    @Updatable
    public Boolean getEnableAlias() {
        if (enableAlias == null) {
            enableAlias = false;
        }

        return enableAlias;
    }

    public void setEnableAlias(Boolean enableAlias) {
        this.enableAlias = enableAlias;
    }

    /**
     * The Hosted Zone where the 'dns name' belongs as configured. Required if 'enable alias' is set to ``true``.
     */
    @Updatable
    public String getAliasHostedZoneId() {
        return aliasHostedZoneId;
    }

    public void setAliasHostedZoneId(String aliasHostedZoneId) {
        this.aliasHostedZoneId = aliasHostedZoneId;
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
        setRecords(recordSet.resourceRecords().stream().map(ResourceRecord::value).collect(Collectors.toList()));
        setId(String.format("%s %s", getName(), getType()));

        if (recordSet.aliasTarget() != null) {
            setDnsName(recordSet.aliasTarget().dnsName());
            setEvaluateTargetHealth(recordSet.aliasTarget().evaluateTargetHealth());
            setAliasHostedZoneId(recordSet.aliasTarget().hostedZoneId());
        }

        if (recordSet.geoLocation() != null) {
            setCountryCode(recordSet.geoLocation().countryCode());
            setContinentCode(recordSet.geoLocation().continentCode());
            setSubdivisionCode(recordSet.geoLocation().subdivisionCode());
        }
    }

    @Override
    public boolean refresh() {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        ResourceRecordSet recordSet = getResourceRecordSet(client);

        if (recordSet == null) {
            return false;
        }

        copyFrom(recordSet);

        return true;
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

    private ResourceRecordSet getResourceRecordSet(Route53Client client) {
        List<ResourceRecordSet> records = client.listResourceRecordSetsPaginator(
            r -> r.hostedZoneId(getHostedZone().getId())
        ).resourceRecordSets().stream().collect(Collectors.toList());

        if (!records.isEmpty()) {
            return records.stream().filter(o -> o.name().equals(getName().replace("*", "\\052"))).findFirst().orElse(null);
        }

        return null;
    }

    private void saveResourceRecordSet(Route53Client client, RecordSetResource recordSetResource, ChangeAction changeAction) {
        ResourceRecordSet.Builder recordSetBuilder = ResourceRecordSet.builder()
            .name(recordSetResource.getName())
            .healthCheckId(recordSetResource.getHealthCheck() != null ? recordSetResource.getHealthCheck().getId() : null)
            .setIdentifier(recordSetResource.getSetIdentifier())
            .trafficPolicyInstanceId(recordSetResource.getTrafficPolicyInstance() != null ? recordSetResource.getTrafficPolicyInstance().getId() : null)
            .type(recordSetResource.getType());

        if (recordSetResource.getEnableAlias()) {
            recordSetBuilder.aliasTarget(
                a -> a.dnsName(recordSetResource.getDnsName())
                    .evaluateTargetHealth(recordSetResource.getEvaluateTargetHealth())
                    .hostedZoneId(recordSetResource.getAliasHostedZoneId()));
        } else {
            recordSetBuilder.resourceRecords(recordSetResource.getRecords().stream()
                .map(o -> ResourceRecord.builder().value(o).build())
                .collect(Collectors.toList()))
                .ttl(recordSetResource.getTtl());
        }

        switch (recordSetResource.getRoutingPolicy()) {
            case "geolocation":
                recordSetBuilder.geoLocation(
                    g -> g.continentCode(recordSetResource.getContinentCode())
                        .countryCode(recordSetResource.getCountryCode())
                        .subdivisionCode(recordSetResource.getSubdivisionCode()));
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

        if (ObjectUtils.isBlank(getType())
            || RRType.fromValue(getType())
            .equals(RRType.UNKNOWN_TO_SDK_VERSION)) {
            errors.add(new ValidationError(this, null, String.format("Invalid value '%s' for param 'insufficient-data-health-status'."
                    + " Valid values [ '%s' ]", getType(),
                Stream.of(RRType.values())
                    .filter(o -> !o.equals(RRType.UNKNOWN_TO_SDK_VERSION))
                    .map(Enum::toString).collect(Collectors.joining("', '")))));
        }

        if (getEnableAlias()) {
            if (!ObjectUtils.isBlank(getTtl())) {
                errors.add(new ValidationError(this, null, "The param 'ttl' is not allowed when 'enable-alias' is set to 'true'."));
            }

            if (!getRecords().isEmpty()) {
                errors.add(new ValidationError(this, null, "The param 'records' is not allowed when 'enable-alias' is set to 'true'."));
            }

            if (getEvaluateTargetHealth() == null) {
                errors.add(new ValidationError(this, null, "The param 'evaluate-target-health' is required when 'enable-alias' is set to 'true'."));
            }

            if (ObjectUtils.isBlank(getDnsName())) {
                errors.add(new ValidationError(this, null, "The param 'dns-name' is required when 'enable-alias' is set to 'true'."));
            }

            if (ObjectUtils.isBlank(getAliasHostedZoneId())) {
                errors.add(new ValidationError(this, null, "The param 'alias-hosted-zone-id' is required when 'enable-alias' is set to 'true'."));
            }
        } else {
            if (getEvaluateTargetHealth() != null) {
                errors.add(new ValidationError(this, null, "The param 'evaluate-target-health' is not allowed when 'enable-alias' is set to 'false' or not set."));
            }

            if (getDnsName() != null) {
                errors.add(new ValidationError(this, null, "The param 'dns-name' is not allowed when 'enable-alias' is set to 'false' or not set."));
            }

            if (getAliasHostedZoneId() != null) {
                errors.add(new ValidationError(this, null, "The param 'alias-hosted-zone-id' is not allowed when 'enable-alias' is set to 'false' or not set."));
            }

            if (ObjectUtils.isBlank(getTtl()) || getTtl() < 0 || getTtl() > 172800) {
                errors.add(new ValidationError(this, null, "The param 'ttl' is required when 'enable-alias' is set to 'false' or not set."
                    + " Valid values [ Long 0 - 172800 ]."));
            }

            if (getRecords().isEmpty()) {
                errors.add(new ValidationError(this, null, "The param 'records' is required when 'enable-alias' is set to 'false' or not set."));
            }
        }

        if (!getRoutingPolicy().equals("geolocation")) {
            if (!ObjectUtils.isBlank(getContinentCode())) {
                errors.add(new ValidationError(this, null, "The param 'continent-code' is not allowed when 'routing-policy' is not set to 'geolocation'."));
            }

            if (!ObjectUtils.isBlank(getCountryCode())) {
                errors.add(new ValidationError(this, null, "The param 'country-code' is not allowed when 'routing-policy' is not set to 'geolocation'."));
            }

            if (!ObjectUtils.isBlank(getSubdivisionCode())) {
                errors.add(new ValidationError(this, null, "The param 'subdivision-code' is not allowed when 'routing-policy' is not set to 'geolocation'."));
            }
        } else {
            if (ObjectUtils.isBlank(getContinentCode()) && ObjectUtils.isBlank(getCountryCode()) && ObjectUtils.isBlank(getSubdivisionCode())) {
                errors.add(new ValidationError(this, null, "At least one of the param [ 'continent-code', 'country-code', 'subdivision-code']"
                    + " is required when 'routing-policy' is set to 'geolocation'."));
            }
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
                    + " Valid values [ Long 0 - 255 ]."));
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
