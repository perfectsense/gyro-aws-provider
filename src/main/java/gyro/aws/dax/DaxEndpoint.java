package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.dax.model.Endpoint;

public class DaxEndpoint extends Diffable implements Copyable<Endpoint> {

    private String address;
    private Integer port;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public void copyFrom(Endpoint model) {
        setAddress(model.address());
        setPort(model.port());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
