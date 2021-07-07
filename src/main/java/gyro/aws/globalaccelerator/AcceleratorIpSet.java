package gyro.aws.globalaccelerator;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;

public class AcceleratorIpSet extends Diffable implements Copyable<software.amazon.awssdk.services.globalaccelerator.model.IpSet> {

    private List<String> ipAddresses;
    private String ipFamily;

    @Override
    public String primaryKey() {
        return "";
    }

    public List<String> getIpAddresses() {
        if (ipAddresses == null) {
            ipAddresses = new ArrayList<>();
        }

        return ipAddresses;
    }

    public void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public String getIpFamily() {
        return ipFamily;
    }

    public void setIpFamily(String ipFamily) {
        this.ipFamily = ipFamily;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.globalaccelerator.model.IpSet ipSet) {
        setIpAddresses(ipSet.ipAddresses());
        setIpFamily(ipSet.ipFamily());
    }
}
