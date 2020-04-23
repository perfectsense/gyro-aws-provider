package gyro.aws.eks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.eks.model.Logging;

public class EksLogging extends Diffable implements Copyable<Logging> {

    private List<EksLogSetup> logSetup;

    public List<EksLogSetup> getLogSetup() {
        if (logSetup == null) {
            logSetup = new ArrayList<>();
        }

        return logSetup;
    }

    public void setLogSetup(List<EksLogSetup> logSetup) {
        this.logSetup = logSetup;
    }

    @Override
    public void copyFrom(Logging model) {
        if (model.hasClusterLogging()) {
            getLogSetup().clear();
            model.clusterLogging().forEach(l -> {
                EksLogSetup eksLogSetup = newSubresource(EksLogSetup.class);
                eksLogSetup.copyFrom(l);
                getLogSetup().add(eksLogSetup);
            });
        }
    }

    @Override
    public String primaryKey() {
        return null;
    }

    Logging toLogging() {
        return Logging.builder()
            .clusterLogging(getLogSetup().stream().map(EksLogSetup::toLogSeup).collect(Collectors.toList()))
            .build();
    }
}
