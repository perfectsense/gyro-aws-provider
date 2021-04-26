/*
 * Copyright 2020, Brightspot, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
