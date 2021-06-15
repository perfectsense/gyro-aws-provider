/*
 * Copyright 2021, Perfect Sense.
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

package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateCpuOptionsRequest;

public class LaunchTemplateCpuOptions extends Diffable
    implements Copyable<software.amazon.awssdk.services.ec2.model.LaunchTemplateCpuOptions> {

    private Integer coreCount;
    private Integer threadsPerCore;

    /**
     * The number of CPU cores for the instance. Defaults to ``0`` which sets its to the instance type defaults.
     */
    @Updatable
    public Integer getCoreCount() {
        return coreCount;
    }

    public void setCoreCount(Integer coreCount) {
        this.coreCount = coreCount;
    }

    /**
     * The number of threads per CPU core. Defaults to ``0`` which sets its to the instance type defaults.
     */
    @Updatable
    public Integer getThreadsPerCore() {
        return threadsPerCore;
    }

    public void setThreadsPerCore(Integer threadsPerCore) {
        this.threadsPerCore = threadsPerCore;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.LaunchTemplateCpuOptions model) {
        setCoreCount(model.coreCount());
        setThreadsPerCore(model.threadsPerCore());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    LaunchTemplateCpuOptionsRequest toLaunchTemplateCpuOptionsRequest() {
        return LaunchTemplateCpuOptionsRequest.builder().coreCount(getCoreCount())
            .threadsPerCore(getThreadsPerCore()).build();
    }
}
