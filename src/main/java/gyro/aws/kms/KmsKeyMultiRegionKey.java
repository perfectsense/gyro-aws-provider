/*
 * Copyright 2025, Brightspot.
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
package gyro.aws.kms;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.kms.model.MultiRegionKey;

public class KmsKeyMultiRegionKey extends Diffable implements Copyable<MultiRegionKey> {

    private String arn;
    private String region;

    /**
     * The Amazon Resource Name (ARN) associated with the key.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The AWS region associated with the key.
     */
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public void copyFrom(MultiRegionKey model) {
        setArn(model.arn());
        setRegion(model.region());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
