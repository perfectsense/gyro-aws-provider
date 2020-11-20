package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.dax.model.Subnet;

public class DaxSubnet extends Diffable implements Copyable<Subnet> {

    private String availabilityZone;
    private String identifier;

    /**
     * The availability zone of the subnet.
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The ID of the subnet.
     */
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void copyFrom(Subnet model) {
        setAvailabilityZone(model.subnetAvailabilityZone());
        setIdentifier(model.subnetIdentifier());
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getIdentifier());
    }
}
