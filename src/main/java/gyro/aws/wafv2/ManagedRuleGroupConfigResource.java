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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidationError;
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
    @Updatable
    public AwsManagedRulesACFPRuleSetResource getAwsManagedRulesAcfpRuleSet() {
        return awsManagedRulesAcfpRuleSet;
    }

    public void setAwsManagedRulesAcfpRuleSet(AwsManagedRulesACFPRuleSetResource awsManagedRulesAcfpRuleSet) {
        this.awsManagedRulesAcfpRuleSet = awsManagedRulesAcfpRuleSet;
    }

    /**
     * Anti-DDoS managed rule group configuration.
     *
     * @subresource gyro.aws.wafv2.AwsManagedRulesAntiDDoSRuleSetResource
     */
    @Updatable
    public AwsManagedRulesAntiDDoSRuleSetResource getAwsManagedRulesAntiDdosRuleSet() {
        return awsManagedRulesAntiDdosRuleSet;
    }

    public void setAwsManagedRulesAntiDdosRuleSet(
        AwsManagedRulesAntiDDoSRuleSetResource awsManagedRulesAntiDDoSRuleSet) {
        this.awsManagedRulesAntiDdosRuleSet = awsManagedRulesAntiDDoSRuleSet;
    }

    /**
     * Account takeover prevention (ATP) managed rule group configuration.
     *
     * @subresource gyro.aws.wafv2.AwsManagedRulesATPRuleSetResource
     */
    @Updatable
    public AwsManagedRulesATPRuleSetResource getAwsManagedRulesAtpRuleSet() {
        return awsManagedRulesAtpRuleSet;
    }

    public void setAwsManagedRulesAtpRuleSet(AwsManagedRulesATPRuleSetResource awsManagedRulesAtpRuleSet) {
        this.awsManagedRulesAtpRuleSet = awsManagedRulesAtpRuleSet;
    }

    /**
     * Bot Control managed rule group configuration.
     *
     * @subresource gyro.aws.wafv2.AwsManagedRulesBotControlRuleSetResource
     */
    @Updatable
    public AwsManagedRulesBotControlRuleSetResource getAwsManagedRulesBotControlRuleSet() {
        return awsManagedRulesBotControlRuleSet;
    }

    public void setAwsManagedRulesBotControlRuleSet(
        AwsManagedRulesBotControlRuleSetResource awsManagedRulesBotControlRuleSet) {
        this.awsManagedRulesBotControlRuleSet = awsManagedRulesBotControlRuleSet;
    }

    @Override
    public String primaryKey() {
        List<String> configured = new ArrayList<>();

        if (getAwsManagedRulesAcfpRuleSet() != null) {
            configured.add("acfp rule set");
        }
        if (getAwsManagedRulesAntiDdosRuleSet() != null) {
            configured.add("anti-ddos rule set");
        }
        if (getAwsManagedRulesAtpRuleSet() != null) {
            configured.add("atp rule set");
        }
        if (getAwsManagedRulesBotControlRuleSet() != null) {
            configured.add("bot control rule set");
        }

        return String.format("with config - '%s'", String.join(", ", configured));
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
            AwsManagedRulesBotControlRuleSetResource rule =
                newSubresource(AwsManagedRulesBotControlRuleSetResource.class);
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

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        long count = Stream.of(
                getAwsManagedRulesAcfpRuleSet(),
                getAwsManagedRulesAntiDdosRuleSet(),
                getAwsManagedRulesAtpRuleSet(),
                getAwsManagedRulesBotControlRuleSet())
            .filter(Objects::nonNull)
            .count();

        if (count != 1) {
            errors.add(new ValidationError(
                this,
                null,
                "Exactly one of [ 'aws-managed-rules-acfp-rule-set', 'aws-managed-rules-anti-ddos-rule-set', "
                    + "'aws-managed-rules-atp-rule-set', 'aws-managed-rules-bot-control-rule-set' ] is required"));
        }

        return errors;
    }
}
