package gyro.aws.globalaccelerator;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.globalaccelerator.model.EndpointConfiguration;
import software.amazon.awssdk.services.globalaccelerator.model.EndpointDescription;

public class EndpointGroupConfiguration extends Diffable implements Copyable<EndpointDescription> {

    private Boolean clientIpPreservationEnabled;
    private String endpointId;
    private Integer weight;

    @Override
    public void copyFrom(EndpointDescription configuration) {
        setClientIpPreservationEnabled(configuration.clientIPPreservationEnabled());
        setWeight(configuration.weight());
        setEndpointId(configuration.endpointId());
    }

    @Override
    public String primaryKey() {
        return getEndpointId();
    }

    public Boolean getClientIpPreservationEnabled() {
        return clientIpPreservationEnabled;
    }

    public void setClientIpPreservationEnabled(Boolean clientIpPreservationEnabled) {
        this.clientIpPreservationEnabled = clientIpPreservationEnabled;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    EndpointConfiguration endpointConfiguration() {
        return EndpointConfiguration.builder()
            .endpointId(getEndpointId())
            .weight(getWeight())
            .clientIPPreservationEnabled(getClientIpPreservationEnabled())
            .build();
    }
}
