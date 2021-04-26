package gyro.aws.clientconfiguration;

import java.util.HashMap;
import java.util.Map;

import gyro.core.scope.Settings;

public class ClientConfigurationSettings extends Settings {

    private Map<String, ClientConfiguration> clientConfigurations;

    public Map<String, ClientConfiguration> getClientConfigurations() {
        if (clientConfigurations == null) {
            clientConfigurations = new HashMap<>();
        }

        return clientConfigurations;
    }

    public void setClientConfigurations(Map<String, ClientConfiguration> clientConfigurations) {
        this.clientConfigurations = clientConfigurations;
    }
}
