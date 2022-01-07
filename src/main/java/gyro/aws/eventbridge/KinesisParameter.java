/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eventbridge.model.KinesisParameters;

public class KinesisParameter extends Diffable implements Copyable<KinesisParameters> {

    private String partitionKeyPath;

    /**
     * The JSON path to be extracted from the event and used as the partition key.
     */
    @Required
    public String getPartitionKeyPath() {
        return partitionKeyPath;
    }

    public void setPartitionKeyPath(String partitionKeyPath) {
        this.partitionKeyPath = partitionKeyPath;
    }

    @Override
    public void copyFrom(KinesisParameters model) {
        setPartitionKeyPath(model.partitionKeyPath());
    }

    @Override
    public String primaryKey() {
        return getPartitionKeyPath();
    }

    protected KinesisParameters toKinesisParameters() {
        return KinesisParameters.builder().partitionKeyPath(getPartitionKeyPath()).build();
    }
}
