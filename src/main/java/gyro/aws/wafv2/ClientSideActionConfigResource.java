/*
 * Copyright 2025, Brightspot.
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

package gyro.aws.wafv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.ClientSideActionConfig;

public class ClientSideActionConfigResource extends Diffable implements Copyable<ClientSideActionConfig> {

    private ClientSideActionResource challenge;

    /**
     * Client-side challenge behavior configuration.
     *
     * @subresource gyro.aws.wafv2.ClientSideActionResource
     */
    @Required
    @Updatable
    public ClientSideActionResource getChallenge() {
        return challenge;
    }

    public void setChallenge(ClientSideActionResource challenge) {
        this.challenge = challenge;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(ClientSideActionConfig clientSideActionConfig) {
        setChallenge(null);
        if (clientSideActionConfig.challenge() != null) {
            ClientSideActionResource resource = newSubresource(ClientSideActionResource.class);
            resource.copyFrom(clientSideActionConfig.challenge());
            setChallenge(resource);
        }
    }

    ClientSideActionConfig toClientSideActionConfig() {
        ClientSideActionConfig.Builder builder = ClientSideActionConfig.builder();

        if (getChallenge() != null) {
            builder.challenge(getChallenge().toClientSideAction());
        }

        return builder.build();
    }
}
