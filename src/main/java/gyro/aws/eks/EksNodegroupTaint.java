/*
 * Copyright 2024, Perfect Sense, Inc.
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

package gyro.aws.eks;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.eks.model.Taint;
import software.amazon.awssdk.services.eks.model.TaintEffect;

public class EksNodegroupTaint extends Diffable implements Copyable<Taint> {

    private String key;
    private String value;
    private TaintEffect taintEffect;

    /*
    * The key of the taint.
    */
    @Required
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /*
    * The value of the taint.
    */
    @Required
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @ValidStrings({"NO_SCHEDULE", "NO_EXECUTE", "PREFER_NO_SCHEDULE"})
    @Required
    public TaintEffect getTaintEffect() {
        return taintEffect;
    }

    public void setTaintEffect(TaintEffect taintEffect) {
        this.taintEffect = taintEffect;
    }

    @Override
    public void copyFrom(Taint taint) {
        setKey(taint.key());
        setValue(taint.value());
        setTaintEffect(taint.effect());
    }

    public Taint toTaint() {
        return Taint.builder()
                .key(getKey())
                .value(getValue())
                .effect(getTaintEffect())
                .build();
    }

    @Override
    public String primaryKey() {
        return getKey() + " : " + getValue() + " : " + getTaintEffect();
    }
}
