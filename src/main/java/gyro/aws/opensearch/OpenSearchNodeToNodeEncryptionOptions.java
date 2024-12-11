/*
 * Copyright 2024, Brightspot.
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

package gyro.aws.opensearch;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.opensearch.model.NodeToNodeEncryptionOptions;

public class OpenSearchNodeToNodeEncryptionOptions extends Diffable
    implements Copyable<NodeToNodeEncryptionOptions> {

    private Boolean enableNodeToNodeEncryption;

    /**
     * Enable node to node encryption to prevent potential interception of traffic between OpenSearch nodes.
     */
    @Required
    public Boolean getEnableNodeToNodeEncryption() {
        return enableNodeToNodeEncryption;
    }

    public void setEnableNodeToNodeEncryption(Boolean enableNodeToNodeEncryption) {
        this.enableNodeToNodeEncryption = enableNodeToNodeEncryption;
    }

    @Override
    public void copyFrom(NodeToNodeEncryptionOptions model) {
        setEnableNodeToNodeEncryption(model.enabled());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    NodeToNodeEncryptionOptions toNodeEncryptionOptions() {
        return NodeToNodeEncryptionOptions.builder().enabled(getEnableNodeToNodeEncryption()).build();
    }
}
