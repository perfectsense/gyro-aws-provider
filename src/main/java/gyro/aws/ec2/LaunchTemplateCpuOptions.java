package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateCpuOptionsRequest;

public class LaunchTemplateCpuOptions extends Diffable
    implements Copyable<software.amazon.awssdk.services.ec2.model.LaunchTemplateCpuOptions> {

    private Integer coreCount;
    private Integer threadsPerCore;

    /**
     * The number of CPU cores for the instance. Defaults to ``0`` which sets its to the instance type defaults.
     */
    @Updatable
    public Integer getCoreCount() {
        return coreCount;
    }

    public void setCoreCount(Integer coreCount) {
        this.coreCount = coreCount;
    }

    /**
     * The number of threads per CPU core. Defaults to ``0`` which sets its to the instance type defaults.
     */
    @Updatable
    public Integer getThreadsPerCore() {
        return threadsPerCore;
    }

    public void setThreadsPerCore(Integer threadsPerCore) {
        this.threadsPerCore = threadsPerCore;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.LaunchTemplateCpuOptions model) {
        setCoreCount(model.coreCount());
        setThreadsPerCore(model.threadsPerCore());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    LaunchTemplateCpuOptionsRequest toLaunchTemplateCpuOptionsRequest() {
        return LaunchTemplateCpuOptionsRequest.builder().coreCount(getCoreCount())
            .threadsPerCore(getThreadsPerCore()).build();
    }
}
