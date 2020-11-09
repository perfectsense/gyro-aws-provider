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

package gyro.aws.ecs;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ecs.model.Ulimit;
import software.amazon.awssdk.services.ecs.model.UlimitName;

public class EcsUlimit extends Diffable {

    private UlimitName name;
    private Integer softLimit;
    private Integer hardLimit;

    /**
     * The type of the ulimit.
     */
    @Required
    @ValidStrings({"core", "cpu", "data", "fsize", "locks", "memlock", "msgqueue", "nice", "nofile", "nproc", "rss", "rtprio", "rttime", "sigpending", "stack"})
    public UlimitName getName() {
        return name;
    }

    public void setName(UlimitName name) {
        this.name = name;
    }

    /**
     * The soft limit for the ulimit type.
     */
    @Required
    public Integer getSoftLimit() {
        return softLimit;
    }

    public void setSoftLimit(Integer softLimit) {
        this.softLimit = softLimit;
    }

    /**
     * The hard limit for the ulimit type.
     */
    @Required
    public Integer getHardLimit() {
        return hardLimit;
    }

    public void setHardLimit(Integer hardLimit) {
        this.hardLimit = hardLimit;
    }

    @Override
    public String primaryKey() {
        return getName().toString();
    }

    public void copyFrom(Ulimit model) {
        setName(model.name());
        setSoftLimit(model.softLimit());
        setHardLimit(model.hardLimit());
    }

    public Ulimit copyTo() {
        return Ulimit.builder()
            .name(getName())
            .softLimit(getSoftLimit())
            .hardLimit(getHardLimit())
            .build();
    }
}
