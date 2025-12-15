/*
 * Copyright 2020, Brightspot.
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
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.wafv2.model.AWSManagedRulesAntiDDoSRuleSet;

public class AwsManagedRulesAntiDDoSRuleSetResource extends Diffable implements Copyable<AWSManagedRulesAntiDDoSRuleSet> {

    private ClientSideActionConfigResource clientSideActionConfig;
    private String sensitivityToBlock;

    /**
     * Configuration for handling requests during a DDoS attack.
     *
     * @subresource gyro.aws.wafv2.ClientSideActionConfigResource
     */
    @Required
    public ClientSideActionConfigResource getClientSideActionConfig() {
        return clientSideActionConfig;
    }

    public void setClientSideActionConfig(ClientSideActionConfigResource clientSideActionConfig) {
        this.clientSideActionConfig = clientSideActionConfig;
    }

    /**
     * Sensitivity level that the rule group uses when matching against DDoS suspicion labels.
     * Valid values are LOW, MEDIUM, and HIGH.
     */
    @ValidStrings({ "LOW", "MEDIUM", "HIGH" })
    public String getSensitivityToBlock() {
        return sensitivityToBlock;
    }

    public void setSensitivityToBlock(String sensitivityToBlock) {
        this.sensitivityToBlock = sensitivityToBlock;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(AWSManagedRulesAntiDDoSRuleSet awsManagedRulesAntiDDoSRuleSet) {
        setClientSideActionConfig(null);
        if (awsManagedRulesAntiDDoSRuleSet.clientSideActionConfig() != null) {
            ClientSideActionConfigResource resource = newSubresource(ClientSideActionConfigResource.class);
            resource.copyFrom(awsManagedRulesAntiDDoSRuleSet.clientSideActionConfig());
            setClientSideActionConfig(resource);
        }

        setSensitivityToBlock(awsManagedRulesAntiDDoSRuleSet.sensitivityToBlockAsString());
    }

    AWSManagedRulesAntiDDoSRuleSet toAwsManagedRulesAntiDDoSRuleSet() {
        AWSManagedRulesAntiDDoSRuleSet.Builder builder = AWSManagedRulesAntiDDoSRuleSet.builder();

        if (getClientSideActionConfig() != null) {
            builder.clientSideActionConfig(getClientSideActionConfig().toClientSideActionConfig());
        }

        if (getSensitivityToBlock() != null) {
            builder.sensitivityToBlock(getSensitivityToBlock());
        }

        return builder.build();
    }
}
