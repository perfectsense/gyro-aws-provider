package gyro.aws.elb;

import gyro.aws.ec2.InstanceResource;
import gyro.core.GyroInstance;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;

public class LoadBalancerInstance extends Diffable {

    private InstanceResource instance;
    private String state;

    public GyroInstance getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
    }

    @Output
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String primaryKey() {
        if (instance != null) {
            return instance.getId();
        }

        return "";
    }

}
