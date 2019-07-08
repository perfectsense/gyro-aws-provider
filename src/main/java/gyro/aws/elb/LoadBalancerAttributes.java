package gyro.aws.elb;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancing.model.AdditionalAttribute;

import java.util.Set;
import java.util.stream.Collectors;

public class LoadBalancerAttributes extends Diffable implements Copyable<software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerAttributes> {
    private LoadBalancerAccessLog accessLog;
    private Set<LoadBalancerAdditionalAttribute> additionalAttribute;
    private LoadBalancerConnectionDraining connectionDraining;
    private LoadBalancerCrossZoneLoadBalancing crossZoneLoadBalancing;
    private LoadBalancerConnectionSettings connectionSettings;

    @Updatable
    public LoadBalancerAccessLog getAccessLog() {
        return accessLog;
    }

    public void setAccessLog(LoadBalancerAccessLog accessLog) {
        this.accessLog = accessLog;
    }

    @Updatable
    public Set<LoadBalancerAdditionalAttribute> getAdditionalAttribute() {
        return additionalAttribute;
    }

    public void setAdditionalAttribute(Set<LoadBalancerAdditionalAttribute> additionalAttribute) {
        this.additionalAttribute = additionalAttribute;
    }

    @Updatable
    public LoadBalancerConnectionDraining getConnectionDraining() {
        return connectionDraining;
    }

    public void setConnectionDraining(LoadBalancerConnectionDraining connectionDraining) {
        this.connectionDraining = connectionDraining;
    }

    @Updatable
    public LoadBalancerCrossZoneLoadBalancing getCrossZoneLoadBalancing() {
        return crossZoneLoadBalancing;
    }

    public void setCrossZoneLoadBalancing(LoadBalancerCrossZoneLoadBalancing crossZoneLoadBalancing) {
        this.crossZoneLoadBalancing = crossZoneLoadBalancing;
    }

    @Updatable
    public LoadBalancerConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    public void setConnectionSettings(LoadBalancerConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerAttributes loadBalancerAttributes) {
        if (loadBalancerAttributes.accessLog() != null) {
            LoadBalancerAccessLog accessLog = newSubresource(LoadBalancerAccessLog.class);
            accessLog.copyFrom(loadBalancerAttributes.accessLog());
            setAccessLog(accessLog);
        } else {
            setAccessLog(null);
        }

        getAdditionalAttribute().clear();
        if (loadBalancerAttributes.additionalAttributes() != null && !loadBalancerAttributes.additionalAttributes().isEmpty()) {
            for (AdditionalAttribute additionalAttribute : loadBalancerAttributes.additionalAttributes()) {
                LoadBalancerAdditionalAttribute lbAdditionalAttribute = newSubresource(LoadBalancerAdditionalAttribute.class);
                lbAdditionalAttribute.copyFrom(additionalAttribute);
                getAdditionalAttribute().add(lbAdditionalAttribute);
            }
        }

        if (loadBalancerAttributes.crossZoneLoadBalancing() != null) {
            LoadBalancerCrossZoneLoadBalancing crossZoneLoadBalancing = newSubresource(LoadBalancerCrossZoneLoadBalancing.class);
            crossZoneLoadBalancing.copyFrom(loadBalancerAttributes.crossZoneLoadBalancing());
            
        }

        if (loadBalancerAttributes.connectionDraining() != null) {
            LoadBalancerConnectionDraining connectionDraining = newSubresource(LoadBalancerConnectionDraining.class);
            connectionDraining.copyFrom(loadBalancerAttributes.connectionDraining());
        }

        if (loadBalancerAttributes.connectionSettings() != null) {
            LoadBalancerConnectionSettings connectionSettings = newSubresource(LoadBalancerConnectionSettings.class);
            connectionSettings.copyFrom(loadBalancerAttributes.connectionSettings());
        }
    }

    @Override
    public String toDisplayString() {
        return "load balancer attribute";
    }

    @Override
    public String primaryKey() {
        return "load balancer attribute";
    }

    software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerAttributes toLoadBalancerAttributes() {
        return software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerAttributes.builder()
            .additionalAttributes(getAdditionalAttribute().stream().map(LoadBalancerAdditionalAttribute::toAdditionalAttribute).collect(Collectors.toList()))
            .crossZoneLoadBalancing(getCrossZoneLoadBalancing() != null ? getCrossZoneLoadBalancing().toCrossZoneLoadBalancing() : null)
            .connectionSettings(getConnectionSettings() != null ? getConnectionSettings().toConnectionSettings() : null)
            .connectionDraining(getConnectionDraining() != null ? getConnectionDraining().toConnectionDraining() : null)
            .accessLog(getAccessLog() != null ? getAccessLog().toAccessLog() : null)
            .build();
    }
}
