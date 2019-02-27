package gyro.aws.route53;

import gyro.aws.AwsResource;
import gyro.core.BeamException;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.ChangeTagsForResourceRequest;
import software.amazon.awssdk.services.route53.model.CreateHealthCheckResponse;
import software.amazon.awssdk.services.route53.model.GetHealthCheckResponse;
import software.amazon.awssdk.services.route53.model.HealthCheck;
import software.amazon.awssdk.services.route53.model.HealthCheckConfig;
import software.amazon.awssdk.services.route53.model.HealthCheckRegion;
import software.amazon.awssdk.services.route53.model.HealthCheckType;
import software.amazon.awssdk.services.route53.model.InsufficientDataHealthStatus;
import software.amazon.awssdk.services.route53.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.route53.model.Tag;
import software.amazon.awssdk.services.route53.model.TagResourceType;
import software.amazon.awssdk.services.route53.model.UpdateHealthCheckRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creates a heath Check resource.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::health-check health-check-example-calculated
 *         type: "CALCULATED"
 *         inverted: false
 *         disabled: false
 *         health-threshold: 0
 *         tags: {
 *             Name: "health-check-example-calculated"
 *         }
 *     end
 *
 */
@ResourceName("health-check")
public class HealthCheckResource extends AwsResource {

    private String healthCheckId;
    private List<String> childHealthChecks;
    private Boolean disabled;
    private Boolean enableSni;
    private Integer failureThreshold;
    private String domainName;
    private Integer healthThreshold;
    private String insufficientDataHealthStatus;
    private Boolean inverted;
    private String ipAddress;
    private Boolean measureLatency;
    private Integer port;
    private List<String> regions;
    private Integer requestInterval;
    private String resourcePath;
    private String searchString;
    private String type;
    private String alarmName;
    private String alarmRegion;
    private Map<String, String> tags;

    private static final Set<String> regionSet = ImmutableSet.copyOf(
        HealthCheckRegion.knownValues().stream()
            .map(HealthCheckRegion::toString).collect(Collectors.toList())
    );

    public String getHealthCheckId() {
        return healthCheckId;
    }

    public void setHealthCheckId(String healthCheckId) {
        this.healthCheckId = healthCheckId;
    }

    /**
     * A list of children health check ids.
     */
    @ResourceDiffProperty(updatable = true, nullable = true)
    public List<String> getChildHealthChecks() {
        if (childHealthChecks == null) {
            childHealthChecks = new ArrayList<>();
        }

        return childHealthChecks;
    }

    public void setChildHealthChecks(List<String> childHealthChecks) {
        this.childHealthChecks = childHealthChecks;
    }

    /**
     * Disable the health check.
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * Enable SNI on the health check.
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getEnableSni() {
        return enableSni;
    }

    public void setEnableSni(Boolean enableSni) {
        this.enableSni = enableSni;
    }

    /**
     * Set the failure threshold upon which the health check changes its status.
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(Integer failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    /**
     * The domain name to monitor for the health check.
     */
    @ResourceDiffProperty(updatable = true)
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     *
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getHealthThreshold() {
        return healthThreshold;
    }

    public void setHealthThreshold(Integer healthThreshold) {
        this.healthThreshold = healthThreshold;
    }

    /**
     * What status to give if there is insufficient data for the health check to analyze.
     */
    @ResourceDiffProperty(updatable = true)
    public String getInsufficientDataHealthStatus() {
        return insufficientDataHealthStatus;
    }

    public void setInsufficientDataHealthStatus(String insufficientDataHealthStatus) {
        this.insufficientDataHealthStatus = insufficientDataHealthStatus;
    }

    /**
     * Invert the health check.
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getInverted() {
        return inverted;
    }

    public void setInverted(Boolean inverted) {
        this.inverted = inverted;
    }

    /**
     * The ip to monitor for the health check.
     */
    @ResourceDiffProperty(updatable = true)
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Enable latency measurement.
     */
    public Boolean getMeasureLatency() {
        return measureLatency;
    }

    public void setMeasureLatency(Boolean measureLatency) {
        this.measureLatency = measureLatency;
    }

    /**
     * The port for the domain name and/or ip address to monitor for the health check.
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Set the regions where the health check would be active.
     * For types that support it, having an empty region would default to all regions being selected.
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getRegions() {
        if (regions == null) {
            regions = new ArrayList<>();
        }

        if (regions.isEmpty() && getType() != null
            && !getType().equals("CALCULATED") && !getType().equals("CLOUDWATCH_METRIC")) {
            regions = new ArrayList<>(regionSet);
        }

        return regions;
    }

    public void setRegions(List<String> regions) {
        this.regions = regions;
    }

    /**
     * The request interval upon which the health check would work.
     */
    public Integer getRequestInterval() {
        return requestInterval;
    }

    public void setRequestInterval(Integer requestInterval) {
        this.requestInterval = requestInterval;
    }

    /**
     * The resource path attached at the end of domain name and/or ip address to monitor for the health check.
     */
    @ResourceDiffProperty(updatable = true)
    public String getResourcePath() {
        if (!ObjectUtils.isBlank(resourcePath)) {
            resourcePath = (resourcePath.startsWith("/") ? "" : "/") + resourcePath;
        }
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * The search string if types 'HTTP_STR_MATCH' or 'HTTPS_STR_MATCH' is selected.
     */
    @ResourceDiffProperty(updatable = true)
    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    /**
     * The type of health check being created.
     */
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The alarm name to attach with the health check if type 'CLOUDWATCH_METRIC' selected.
     */
    @ResourceDiffProperty(updatable = true)
    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    /**
     * The alarm region to attach with the health check if type 'CLOUDWATCH_METRIC' selected.
     */
    @ResourceDiffProperty(updatable = true)
    public String getAlarmRegion() {
        return alarmRegion;
    }

    public void setAlarmRegion(String alarmRegion) {
        this.alarmRegion = alarmRegion;
    }

    @ResourceDiffProperty(updatable = true, nullable = true)
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean refresh() {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        GetHealthCheckResponse response = client.getHealthCheck(
            r -> r.healthCheckId(getHealthCheckId())
        );

        HealthCheck healthCheck = response.healthCheck();
        HealthCheckConfig healthCheckConfig = healthCheck.healthCheckConfig();
        setChildHealthChecks(healthCheckConfig.childHealthChecks());
        setDisabled(healthCheckConfig.disabled());
        setEnableSni(healthCheckConfig.enableSNI());
        setFailureThreshold(healthCheckConfig.failureThreshold());
        setDomainName(healthCheckConfig.fullyQualifiedDomainName());
        setHealthThreshold(healthCheckConfig.healthThreshold());
        setInsufficientDataHealthStatus(healthCheckConfig.insufficientDataHealthStatusAsString());
        setInverted(healthCheckConfig.inverted());
        setIpAddress(healthCheckConfig.ipAddress());
        setMeasureLatency(healthCheckConfig.measureLatency());
        setPort(healthCheckConfig.port());

        setRequestInterval(healthCheckConfig.requestInterval());
        setResourcePath(healthCheckConfig.resourcePath());
        setSearchString(healthCheckConfig.searchString());
        setType(healthCheckConfig.typeAsString());

        if (getType().equals("CALCULATED") || getType().equals("CLOUDWATCH_METRIC")) {
            setRegions(new ArrayList<>());
        } else {
            if (healthCheckConfig.regionsAsStrings().isEmpty()) {
                setRegions(new ArrayList<>(regionSet));
            } else {
                setRegions(healthCheckConfig.regionsAsStrings());
            }
        }

        if (healthCheckConfig.alarmIdentifier() != null) {
            setAlarmName(healthCheckConfig.alarmIdentifier().name());
            setAlarmRegion(healthCheckConfig.alarmIdentifier().regionAsString());
        }

        loadTags(client);

        return true;
    }

    @Override
    public void create() {
        validate();

        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        CreateHealthCheckResponse response = client.createHealthCheck(
            r -> r.callerReference(UUID.randomUUID().toString())
                .healthCheckConfig(getCreateHealthCheckRequest())
        );

        HealthCheck healthCheck = response.healthCheck();

        setHealthCheckId(healthCheck.id());

        saveTags(client, new HashMap<>());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        validate();

        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        if (changedProperties.contains("tags")) {
            HealthCheckResource oldResource = (HealthCheckResource) current;
            saveTags(client, oldResource.getTags());
        }

        client.updateHealthCheck(getUpdateHealthCheckRequest());
    }

    @Override
    public void delete() {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteHealthCheck(
            r -> r.healthCheckId(getHealthCheckId())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("health check");

        if (!ObjectUtils.isBlank(getHealthCheckId())) {
            sb.append(" - ").append(getHealthCheckId());
        }

        if (!ObjectUtils.isBlank(getType())) {
            sb.append(" [ ").append(getType()).append(" ]");
        }

        return sb.toString();
    }

    private HealthCheckConfig getCreateHealthCheckRequest() {
        if (getType().equals("CALCULATED")) {
            return HealthCheckConfig.builder()
                .type(getType())
                .childHealthChecks(getChildHealthChecks())
                .disabled(getDisabled())
                .inverted(getInverted())
                .healthThreshold(getHealthThreshold())
                .build();

        } else if (getType().equals("CLOUDWATCH_METRIC")) {
            return HealthCheckConfig.builder()
                .type(getType())
                .disabled(getDisabled())
                .insufficientDataHealthStatus(getInsufficientDataHealthStatus())
                .inverted(getInverted())
                .alarmIdentifier(
                    a -> a.name(getAlarmName())
                        .region(getAlarmRegion())
                )
                .build();

        } else {
            return HealthCheckConfig.builder()
                .disabled(getDisabled())
                .enableSNI(getEnableSni())
                .failureThreshold(getFailureThreshold())
                .fullyQualifiedDomainName(getDomainName())
                .healthThreshold(getHealthThreshold())
                .inverted(getInverted())
                .ipAddress(getIpAddress())
                .measureLatency(getMeasureLatency())
                .port(getPort())
                .requestInterval(getRequestInterval())
                .resourcePath(getResourcePath())
                .searchString(getSearchString())
                .type(getType())
                .regionsWithStrings(getRegions())
                .build();
        }
    }

    private UpdateHealthCheckRequest getUpdateHealthCheckRequest() {
        if (getType().equals("CALCULATED")) {
            return UpdateHealthCheckRequest.builder()
                .healthCheckId(getHealthCheckId())
                .childHealthChecks(getChildHealthChecks())
                .disabled(getDisabled())
                .inverted(getInverted())
                .healthThreshold(getHealthThreshold())
                .build();
        } else if (getType().equals("CLOUDWATCH_METRIC")) {
            return UpdateHealthCheckRequest.builder()
                .healthCheckId(getHealthCheckId())
                .disabled(getDisabled())
                .insufficientDataHealthStatus(getInsufficientDataHealthStatus())
                .inverted(getInverted())
                .alarmIdentifier(
                    a -> a.name(getAlarmName())
                        .region(getAlarmRegion())
                )
                .build();
        } else {
            return UpdateHealthCheckRequest.builder()
                .healthCheckId(getHealthCheckId())
                .disabled(getDisabled())
                .enableSNI(getEnableSni())
                .failureThreshold(getFailureThreshold())
                .fullyQualifiedDomainName(getDomainName())
                .healthThreshold(getHealthThreshold())
                .inverted(getInverted())
                .ipAddress(getIpAddress())
                .port(getPort())
                .resourcePath(getResourcePath())
                .searchString(getSearchString())
                .regionsWithStrings(getRegions())
                .build();
        }
    }

    private List<Tag> getRoute53Tags(Map<String, String> tags) {
        List<Tag> tagList = new ArrayList<>();

        for (String key: tags.keySet()) {
            tagList.add(
                Tag.builder()
                    .key(key)
                    .value(tags.get(key))
                    .build()
            );
        }

        return tagList;
    }

    private void saveTags(Route53Client client, Map<String, String> oldTags) {
        if (!oldTags.isEmpty() || !getTags().isEmpty()) {
            MapDifference<String, String> diff = Maps.difference(oldTags, getTags());

            ChangeTagsForResourceRequest tagRequest;

            if (getTags().isEmpty()) {
                tagRequest = ChangeTagsForResourceRequest.builder()
                    .resourceId(getHealthCheckId())
                    .resourceType(TagResourceType.HEALTHCHECK)
                    .removeTagKeys(diff.entriesOnlyOnLeft().keySet())
                    .build();
            } else if (diff.entriesOnlyOnLeft().isEmpty()) {
                tagRequest = ChangeTagsForResourceRequest.builder()
                    .resourceId(getHealthCheckId())
                    .resourceType(TagResourceType.HEALTHCHECK)
                    .addTags(getRoute53Tags(getTags()))
                    .build();
            } else {
                tagRequest = ChangeTagsForResourceRequest.builder()
                    .resourceId(getHealthCheckId())
                    .resourceType(TagResourceType.HEALTHCHECK)
                    .addTags(getRoute53Tags(getTags()))
                    .removeTagKeys(diff.entriesOnlyOnLeft().keySet())
                    .build();
            }

            client.changeTagsForResource(tagRequest);
        }
    }

    private void loadTags(Route53Client client) {
        ListTagsForResourceResponse response = client.listTagsForResource(
            r -> r.resourceId(getHealthCheckId())
                .resourceType(TagResourceType.HEALTHCHECK)
        );

        List<Tag> tags = response.resourceTagSet().tags();

        getTags().clear();

        for (Tag tag : tags) {
            getTags().put(tag.key(), tag.value());
        }
    }

    private void validate() {
        //Type validation
        if (ObjectUtils.isBlank(getType())
            || HealthCheckType.fromValue(getType()).equals(HealthCheckType.UNKNOWN_TO_SDK_VERSION)) {
            throw new BeamException(String.format("Invalid value '%s' for param 'type'. Valid values [ '%s' ]", getType(),
                Stream.of(HealthCheckType.values())
                    .filter(o -> !o.equals(HealthCheckType.UNKNOWN_TO_SDK_VERSION))
                    .map(Enum::toString).collect(Collectors.joining("', '"))));
        }

        //Attribute validation when type not CALCULATED
        if (!getType().equals("CALCULATED")) {
            if (!ObjectUtils.isBlank(getHealthThreshold())) {
                throw new BeamException("The param 'health-threshold' is only allowed when"
                    + " 'type' is 'CALCULATED'.");
            }

            if (!getChildHealthChecks().isEmpty()) {
                throw new BeamException("The param 'child-health-checks' is only allowed when"
                    + " 'type' is 'CALCULATED'.");
            }
        }

        //Attribute validation when type not CLOUDWATCH_METRIC
        if (!getType().equals("CLOUDWATCH_METRIC")) {
            if (!ObjectUtils.isBlank(getInsufficientDataHealthStatus())) {
                throw new BeamException("The param 'insufficient-data-health-status' is only allowed when"
                    + " 'type' is 'CLOUDWATCH_METRIC'.");
            }

            if (!ObjectUtils.isBlank(getAlarmName())) {
                throw new BeamException("The param 'alarm-name' is only allowed when"
                    + " 'type' is 'CLOUDWATCH_METRIC'.");
            }

            if (!ObjectUtils.isBlank(getAlarmRegion())) {
                throw new BeamException("The param 'alarm-region' is only allowed when"
                    + " 'type' is 'CLOUDWATCH_METRIC'.");
            }
        }

        //Attribute validation when type CALCULATED
        if (getType().equals("CALCULATED")) {
            if (ObjectUtils.isBlank(getHealthThreshold()) || getHealthThreshold() < 0) {
                throw new BeamException("The value - (" + getHealthThreshold()
                    + ") is invalid for parameter 'health-threshold'. Valid values [ Integer value grater or equal to 0. ]");
            }
        }

        //Attribute validation when type CLOUDWATCH_METRIC
        if (getType().equals("CLOUDWATCH_METRIC")) {
            if (ObjectUtils.isBlank(getInsufficientDataHealthStatus())
                || InsufficientDataHealthStatus.fromValue(getInsufficientDataHealthStatus())
                .equals(InsufficientDataHealthStatus.UNKNOWN_TO_SDK_VERSION)) {
                throw new BeamException(String.format("Invalid value '%s' for param 'insufficient-data-health-status'."
                        + " Valid values [ '%s' ]", getInsufficientDataHealthStatus(),
                    Stream.of(InsufficientDataHealthStatus.values())
                        .filter(o -> !o.equals(InsufficientDataHealthStatus.UNKNOWN_TO_SDK_VERSION))
                        .map(Enum::toString).collect(Collectors.joining("', '"))));
            }
        }

        //Attribute validation when type is CALCULATED or CLOUDWATCH_METRIC
        if (getType().equals("CALCULATED") && getType().equals("CLOUDWATCH_METRIC")) {
            if (!getRegions().isEmpty()) {
                throw new BeamException("The param 'regions' is not allowed when"
                    + " 'type' is 'CALCULATED' or 'CLOUDWATCH_METRIC'.");
            }

            if (!ObjectUtils.isBlank(getRequestInterval())) {
                throw new BeamException("The param 'request-interval' is not allowed when"
                    + " 'type' is 'CALCULATED' or 'CLOUDWATCH_METRIC'.");
            }

            if (!ObjectUtils.isBlank(getResourcePath())) {
                throw new BeamException("The param 'resource-path' is not allowed when"
                    + " 'type' is 'CALCULATED' or 'CLOUDWATCH_METRIC'.");
            }

            if (!ObjectUtils.isBlank(getIpAddress())) {
                throw new BeamException("The param 'ip-address' is not allowed when"
                    + " 'type' is 'CALCULATED' or 'CLOUDWATCH_METRIC'.");
            }

            if (!ObjectUtils.isBlank(getDomainName())) {
                throw new BeamException("The param 'domain-name' is not allowed when"
                    + " 'type' is 'CALCULATED' or 'CLOUDWATCH_METRIC'.");
            }

            if (getMeasureLatency() != null) {
                throw new BeamException("The param 'measure-latency' is not allowed when"
                    + " 'type' is 'CALCULATED' or 'CLOUDWATCH_METRIC'.");
            }

            if (getPort() != null) {
                throw new BeamException("The param 'port' is not allowed when"
                    + " 'type' is 'CALCULATED' or 'CLOUDWATCH_METRIC'.");
            }

            if (getFailureThreshold() != null) {
                throw new BeamException("The param 'failure-threshold' is not allowed when"
                    + " 'type' is 'CALCULATED' or 'CLOUDWATCH_METRIC'.");
            }
        }

        //Attribute validation when type is HTTP_STR_MATCH or HTTPS_STR_MATCH
        if (!getType().equals("HTTP_STR_MATCH") && !getType().equals("HTTPS_STR_MATCH")) {
            if (getSearchString() != null) {
                throw new BeamException("The param 'search-string' is only allowed when"
                    + " 'type' is 'HTTP_STR_MATCH' or 'HTTPS_STR_MATCH'.");
            }
        }

        //Attribute validation when type is HTTPS or HTTPS_STR_MATCH
        if (!getType().equals("HTTPS") && !getType().equals("HTTPS_STR_MATCH")) {
            if (getEnableSni() != null) {
                throw new BeamException("The param 'enable-sni' is only allowed when"
                    + " 'type' is 'HTTPS' or 'HTTPS_STR_MATCH'.");
            }
        }

        //Attribute validation when type is not CALCULATED or CLOUDWATCH_METRIC
        if (!getType().equals("CALCULATED") && !getType().equals("CLOUDWATCH_METRIC")) {
            if (ObjectUtils.isBlank(getRequestInterval())
                || (getRequestInterval() != 10 && getRequestInterval() != 30)) {
                throw new BeamException("The value - (" + getRequestInterval()
                    + ") is invalid for parameter 'request-interval'. Valid values [ 10, 30 ].");
            }

            if (!getRegions().isEmpty() && !regionSet.containsAll(getRegions())) {
                throw new BeamException(String.format("Invalid values [ '%s' ] for param 'regions'."
                        + " Valid values [ '%s' ]",
                    getRegions().stream().filter(o -> !regionSet.contains(o)).collect(Collectors.joining("', '")),
                    String.join("', '", regionSet)));
            }
        }

        //Attribute validation when type is not CALCULATED or CLOUDWATCH_METRIC
        if (getType().equals("TCP")) {
            if ((!ObjectUtils.isBlank(getIpAddress()) && !ObjectUtils.isBlank(getDomainName()))
                || (ObjectUtils.isBlank(getIpAddress()) && ObjectUtils.isBlank(getDomainName()))) {
                throw new BeamException("When parameter 'type' is 'TCP' either param 'ip-address' or 'domain-name' needs to be specified.");
            }
        }
    }
}
