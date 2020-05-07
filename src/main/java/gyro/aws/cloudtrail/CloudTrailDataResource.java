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

package gyro.aws.cloudtrail;

import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.cloudtrail.model.DataResource;

public class CloudTrailDataResource extends Diffable implements Copyable<DataResource> {

    private String type;
    private List<String> values;

    /**
     * The resource type in which the data events should be logged. Valid values are ``AWS::S3::Object`` or ``AWS::Lambda::Function``. (Required)
     */
    @Updatable
    @Required
    @ValidStrings({"AWS::S3::Object", "AWS::Lambda::Function"})
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The list of Amazon Resource Names (ARNs) of the resources in which the data events should be logged. (Required)
     */
    @Updatable
    @Required
    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public void copyFrom(DataResource model) {
        setType(model.type());
        setValues(model.values());
    }

    @Override
    public String primaryKey() {
        return String.format("Type: %s, Values: %s", getType(), getValues().toString());
    }

    DataResource toDataResource() {
        return DataResource.builder().type(getType()).values(getValues()).build();
    }
}
