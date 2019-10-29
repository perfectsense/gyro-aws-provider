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
import software.amazon.awssdk.services.acm.model.RecordType;
import software.amazon.awssdk.services.acm.model.ResourceRecord;

public class AcmResourceRecord extends Diffable implements Copyable<ResourceRecord> {
    private String name;
    private String value;
    private RecordType type;

    /**
     * The name of the DNS record to create in your domain.
     */
    @Output
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value of the CNAME record to add to your DNS database.
     */
    @Output
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The type of DNS record.
     */
    @Output
    public RecordType getType() {
        return type;
    }

    public void setType(RecordType type) {
        this.type = type;
    }

    @Override
    public void copyFrom(ResourceRecord resourceRecord) {
        setName(resourceRecord.name());
        setType(resourceRecord.type());
        setValue(resourceRecord.value());
    }

    @Override
    public String primaryKey() {
        return "resource record";
    }
}
