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

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.HostHeaderConditionConfig;

public class HostHeaderConditionConfiguration extends Diffable implements Copyable<HostHeaderConditionConfig>, ConditionValue {

    private List<String> values;

    /**
     * A list of host names.
     */
    @Required
    @Updatable
    public List<String> getValues() {
        if (values == null) {
            values = new ArrayList<>();
        }

        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public void copyFrom(HostHeaderConditionConfig model) {
        setValues(model.values());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    HostHeaderConditionConfig toHostHeaderConditionConfig() {
        return HostHeaderConditionConfig.builder().values(getValues()).build();
    }

    @Override
    public String getField() {
        return "host-header";
    }
}
