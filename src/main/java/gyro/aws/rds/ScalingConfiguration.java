/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.rds;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

public class ScalingConfiguration extends Diffable {

    private Boolean autoPause;
    private Integer maxCapacity;
    private Integer minCapacity;
    private Integer secondsUntilAutoPause;

    /**
     * Allow automatic pause for an Aurora DB cluster in serverless DB engine mode. A DB cluster can be paused only when it's idle (i.e. it has no connections).
     */
    @Updatable
    public Boolean getAutoPause() {
        return autoPause;
    }

    public void setAutoPause(Boolean autoPause) {
        this.autoPause = autoPause;
    }

    /**
     * The maximum capacity for an Aurora DB cluster in serverless DB engine mode.
     */
    @Updatable
    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * The minimum capacity for an Aurora DB cluster in serverless DB engine mode.
     */
    @Updatable
    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
    }

    /**
     * The time before an Aurora DB cluster in serverless mode is paused in seconds.
     */
    @Updatable
    public Integer getSecondsUntilAutoPause() {
        return secondsUntilAutoPause;
    }

    public void setSecondsUntilAutoPause(Integer secondsUntilAutoPause) {
        this.secondsUntilAutoPause = secondsUntilAutoPause;
    }

    @Override
    public String primaryKey() {
        return "scaling configuration";
    }

}
