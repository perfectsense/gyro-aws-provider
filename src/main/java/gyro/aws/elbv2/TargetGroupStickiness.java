/*
 * Copyright 2021, Brightspot.
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

package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupStickinessConfig;

public class TargetGroupStickiness extends Diffable implements Copyable<TargetGroupStickinessConfig> {

    private Boolean enabled;
    private Integer duration;

    /**
     * When set to ``true``, the requests will be directed to the same target group
     */
    @Updatable
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The amount of time for which requests should be directed to the same target group
     */
    @Updatable
    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @Override
    public void copyFrom(TargetGroupStickinessConfig targetGroupStickinessConfig) {
        setDuration(targetGroupStickinessConfig.durationSeconds());
        setEnabled(targetGroupStickinessConfig.enabled());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
