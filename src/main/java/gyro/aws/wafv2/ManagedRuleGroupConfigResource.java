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
import software.amazon.awssdk.services.wafv2.model.ManagedRuleGroupConfig;

public class ManagedRuleGroupConfigResource extends Diffable implements Copyable<ManagedRuleGroupConfig> {

    private AwsManagedRulesACFPRuleSetResource awsManagedRulesAcfpRuleSet;
    private AwsManagedRulesAntiDDoSRuleSetResource awsManagedRulesAntiDdosRuleSet;
    private AwsManagedRulesATPRuleSetResource awsManagedRulesAtpRuleSet;
    private AwsManagedRulesBotControlRuleSetResource awsManagedRulesBotControlRuleSet;

    /**
     * Account creation fraud prevention (ACFP) managed rule group configuration.
     *
     * @subresource gyro.aws.wafv2.AwsManagedRulesACFPRuleSetResource
     */
    public AwsManagedRulesACFPRuleSetResource getAwsManagedRulesAcfpRuleSet() {
        return awsManagedRulesAcfpRuleSet;
    }

    public void setAwsManagedRulesAcfpRuleSet(
        AwsManagedRulesACFPRuleSetResource awsManagedRulesAcfpRuleSet
    ) {
        this.awsManagedRulesAcfpRuleSet = awsManagedRulesAcfpRuleSet;
    }

    /**
     * Anti-DDoS managed rule group configuration (AWSManagedRulesAntiDDoSRuleSet).
     *
     * @subresource gyro.aws.wafv2.AwsManagedRulesAntiDDoSRuleSetResource
     */
    public AwsManagedRulesAntiDDoSRuleSetResource getAwsManagedRulesAntiDdosRuleSet() {
        return awsManagedRulesAntiDdosRuleSet;
    }

    public void setAwsManagedRulesAntiDdosRuleSet(
        AwsManagedRulesAntiDDoSRuleSetResource awsManagedRulesAntiDDoSRuleSet
    ) {
        this.awsManagedRulesAntiDdosRuleSet = awsManagedRulesAntiDDoSRuleSet;
    }

    /**
     * Account takeover prevention (ATP) managed rule group configuration.
     *
     * @subresource gyro.aws.wafv2.AwsManagedRulesATPRuleSetResource
     */
    public AwsManagedRulesATPRuleSetResource getAwsManagedRulesAtpRuleSet() {
        return awsManagedRulesAtpRuleSet;
    }

    public void setAwsManagedRulesAtpRuleSet(
        AwsManagedRulesATPRuleSetResource awsManagedRulesAtpRuleSet
    ) {
        this.awsManagedRulesAtpRuleSet = awsManagedRulesAtpRuleSet;
    }

    /**
     * Bot Control managed rule group configuration.
     *
     * @subresource gyro.aws.wafv2.AwsManagedRulesBotControlRuleSetResource
     */
    public AwsManagedRulesBotControlRuleSetResource getAwsManagedRulesBotControlRuleSet() {
        return awsManagedRulesBotControlRuleSet;
    }

    public void setAwsManagedRulesBotControlRuleSet(
        AwsManagedRulesBotControlRuleSetResource awsManagedRulesBotControlRuleSet
    ) {
        this.awsManagedRulesBotControlRuleSet = awsManagedRulesBotControlRuleSet;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(ManagedRuleGroupConfig managedRuleGroupConfig) {
        setAwsManagedRulesAcfpRuleSet(null);
        if (managedRuleGroupConfig.awsManagedRulesACFPRuleSet() != null) {
            AwsManagedRulesACFPRuleSetResource rule = newSubresource(AwsManagedRulesACFPRuleSetResource.class);
            rule.copyFrom(managedRuleGroupConfig.awsManagedRulesACFPRuleSet());
            setAwsManagedRulesAcfpRuleSet(rule);
        }

        setAwsManagedRulesAntiDdosRuleSet(null);
        if (managedRuleGroupConfig.awsManagedRulesAntiDDoSRuleSet() != null) {
            AwsManagedRulesAntiDDoSRuleSetResource rule = newSubresource(AwsManagedRulesAntiDDoSRuleSetResource.class);
            rule.copyFrom(managedRuleGroupConfig.awsManagedRulesAntiDDoSRuleSet());
            setAwsManagedRulesAntiDdosRuleSet(rule);
        }

        setAwsManagedRulesAtpRuleSet(null);
        if (managedRuleGroupConfig.awsManagedRulesATPRuleSet() != null) {
            AwsManagedRulesATPRuleSetResource rule = newSubresource(AwsManagedRulesATPRuleSetResource.class);
            rule.copyFrom(managedRuleGroupConfig.awsManagedRulesATPRuleSet());
            setAwsManagedRulesAtpRuleSet(rule);
        }

        setAwsManagedRulesBotControlRuleSet(null);
        if (managedRuleGroupConfig.awsManagedRulesBotControlRuleSet() != null) {
            AwsManagedRulesBotControlRuleSetResource rule = newSubresource(AwsManagedRulesBotControlRuleSetResource.class);
            rule.copyFrom(managedRuleGroupConfig.awsManagedRulesBotControlRuleSet());
            setAwsManagedRulesBotControlRuleSet(rule);
        }
    }

    ManagedRuleGroupConfig toManagedRuleGroupConfig() {
        ManagedRuleGroupConfig.Builder builder = ManagedRuleGroupConfig.builder();

        if (getAwsManagedRulesAcfpRuleSet() != null) {
            builder.awsManagedRulesACFPRuleSet(
                getAwsManagedRulesAcfpRuleSet().toAwsManagedRulesACFPRuleSet());
        }

        if (getAwsManagedRulesAntiDdosRuleSet() != null) {
            builder.awsManagedRulesAntiDDoSRuleSet(
                getAwsManagedRulesAntiDdosRuleSet().toAwsManagedRulesAntiDDoSRuleSet());
        }

        if (getAwsManagedRulesAtpRuleSet() != null) {
            builder.awsManagedRulesATPRuleSet(
                getAwsManagedRulesAtpRuleSet().toAwsManagedRulesATPRuleSet());
        }

        if (getAwsManagedRulesBotControlRuleSet() != null) {
            builder.awsManagedRulesBotControlRuleSet(
                getAwsManagedRulesBotControlRuleSet().toAwsManagedRulesBotControlRuleSet());
        }

        return builder.build();
    }
}
