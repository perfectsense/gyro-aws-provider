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

package gyro.aws.acm;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.acm.model.KeyUsage;
import software.amazon.awssdk.services.acm.model.KeyUsageName;

public class AcmKeyUsage extends Diffable implements Copyable<KeyUsage> {
    private KeyUsageName name;

    /**
     * Key Usage extension name.
     */
    @Output
    public KeyUsageName getName() {
        return name;
    }

    public void setName(KeyUsageName name) {
        this.name = name;
    }

    @Override
    public void copyFrom(KeyUsage keyUsage) {
        setName(keyUsage.name());
    }

    @Override
    public String primaryKey() {
        return getName() != null ? getName().toString() : "";
    }
}
