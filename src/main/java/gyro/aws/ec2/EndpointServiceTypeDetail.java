package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.ec2.model.ServiceType;
import software.amazon.awssdk.services.ec2.model.ServiceTypeDetail;

public class EndpointServiceTypeDetail extends Diffable implements Copyable<ServiceTypeDetail> {
    private ServiceType serviceType;

    /**
     * The type of service
     */
    @Output
    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public void copyFrom(ServiceTypeDetail serviceTypeDetail) {
        setServiceType(serviceTypeDetail.serviceType());
    }

    @Override
    public String primaryKey() {
        return toDisplayString();
    }
}
