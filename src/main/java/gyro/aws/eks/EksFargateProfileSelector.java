package gyro.aws.eks;

import java.util.HashMap;
import java.util.Map;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.model.FargateProfileSelector;

public class EksFargateProfileSelector extends Diffable implements Copyable<FargateProfileSelector> {

    private String namespace;
    private Map<String, String> labels;

    @Required
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Map<String, String> getLabels() {
        if (labels == null) {
            labels = new HashMap<>();
        }

        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    @Override
    public void copyFrom(FargateProfileSelector model) {
        setLabels(model.labels());
        setNamespace(model.namespace());
    }

    @Override
    public String primaryKey() {
        return namespace;
    }

    FargateProfileSelector toFargateProfileSelector() {
        return FargateProfileSelector.builder().namespace(getNamespace()).labels(getLabels()).build();
    }
}
