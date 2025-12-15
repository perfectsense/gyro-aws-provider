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
import software.amazon.awssdk.services.wafv2.model.AWSManagedRulesBotControlRuleSet;
import software.amazon.awssdk.services.wafv2.model.InspectionLevel;

public class AwsManagedRulesBotControlRuleSetResource extends Diffable implements Copyable<AWSManagedRulesBotControlRuleSet> {

    private String inspectionLevel;
    private Boolean enableMachineLearning;

    /**
     * The inspection level to use for the Bot Control rule group.
     */
    @Required
    @ValidStrings({ "COMMON", "TARGETED" })
    public String getInspectionLevel() {
        return inspectionLevel;
    }

    public void setInspectionLevel(String inspectionLevel) {
        this.inspectionLevel = inspectionLevel;
    }

    /**
     * Whether to use machine learning (ML) to analyze your web traffic for bot-related activity.
     */
    public Boolean getEnableMachineLearning() {
        return enableMachineLearning;
    }

    public void setEnableMachineLearning(Boolean enableMachineLearning) {
        this.enableMachineLearning = enableMachineLearning;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(AWSManagedRulesBotControlRuleSet awsManagedRulesBotControlRuleSet) {
        setInspectionLevel(awsManagedRulesBotControlRuleSet.inspectionLevel() != null ? awsManagedRulesBotControlRuleSet.inspectionLevel().toString() : null);
        setEnableMachineLearning(awsManagedRulesBotControlRuleSet.enableMachineLearning());
    }

    AWSManagedRulesBotControlRuleSet toAwsManagedRulesBotControlRuleSet() {
        AWSManagedRulesBotControlRuleSet.Builder builder = AWSManagedRulesBotControlRuleSet.builder();

        if (getInspectionLevel() != null) {
            builder.inspectionLevel(InspectionLevel.fromValue(getInspectionLevel()));
        }
        if (getEnableMachineLearning() != null) {
            builder.enableMachineLearning(getEnableMachineLearning());
        }

        return builder.build();
    }
}
