package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.dax.model.ParameterGroupStatus;

public class DaxParameterGroupStatus extends Diffable implements Copyable<ParameterGroupStatus> {

    private List<String> nodeIdsToReboot;
    private String parameterApplyStatus;
    private String parameterGroupName;

    public List<String> getNodeIdsToReboot() {
        if (nodeIdsToReboot == null) {
            nodeIdsToReboot = new ArrayList<>();
        }

        return nodeIdsToReboot;
    }

    public void setNodeIdsToReboot(List<String> nodeIdsToReboot) {
        this.nodeIdsToReboot = nodeIdsToReboot;
    }

    public String getParameterApplyStatus() {
        return parameterApplyStatus;
    }

    public void setParameterApplyStatus(String parameterApplyStatus) {
        this.parameterApplyStatus = parameterApplyStatus;
    }

    public String getParameterGroupName() {
        return parameterGroupName;
    }

    public void setParameterGroupName(String parameterGroupName) {
        this.parameterGroupName = parameterGroupName;
    }

    @Override
    public void copyFrom(ParameterGroupStatus model) {
        setNodeIdsToReboot(model.nodeIdsToReboot());
        setParameterApplyStatus(model.parameterApplyStatus());
        setParameterGroupName(model.parameterGroupName());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
