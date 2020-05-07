/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.cloudtrail;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.cloudtrail.model.InsightSelector;
import software.amazon.awssdk.services.cloudtrail.model.InsightType;

public class CloudTrailInsightSelector extends Diffable implements Copyable<InsightSelector> {

    private InsightType insightType;

    /**
     * The type of insights to log on a trail. (Required)
     */
    @Updatable
    @Required
    public InsightType getInsightType() {
        return insightType;
    }

    public void setInsightType(InsightType insightType) {
        this.insightType = insightType;
    }

    @Override
    public void copyFrom(InsightSelector model) {
        setInsightType(model.insightType());
    }

    @Override
    public String primaryKey() {
        return getInsightType().toString();
    }

    InsightSelector toInsightSelector() {
        return InsightSelector.builder().insightType(getInsightType()).build();
    }
}
