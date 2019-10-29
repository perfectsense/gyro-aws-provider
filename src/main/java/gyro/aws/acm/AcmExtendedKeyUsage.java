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
import software.amazon.awssdk.services.acm.model.ExtendedKeyUsage;
import software.amazon.awssdk.services.acm.model.ExtendedKeyUsageName;

public class AcmExtendedKeyUsage extends Diffable implements Copyable<ExtendedKeyUsage> {
    private ExtendedKeyUsageName name;
    private String oid;

    /**
     * The name of an Extended Key Usage value.
     */
    @Output
    public ExtendedKeyUsageName getName() {
        return name;
    }

    public void setName(ExtendedKeyUsageName name) {
        this.name = name;
    }

    /**
     * An object identifier (OID) for the extension value.
     */
    @Output
    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    @Override
    public void copyFrom(ExtendedKeyUsage extendedKeyUsage) {
        setName(extendedKeyUsage.name());
        setOid(extendedKeyUsage.oid());
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", (getName() != null ? getName().toString() : ""), getOid());
    }
}
