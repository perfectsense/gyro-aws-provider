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
import software.amazon.awssdk.services.kms.model.MultiRegionConfiguration;
import software.amazon.awssdk.services.kms.model.MultiRegionKey;

import java.util.ArrayList;
import java.util.List;

public class KmsKeyMultiRegionConfiguration extends Diffable implements Copyable<MultiRegionConfiguration> {

    private String multiRegionKeyType;

    private KmsKeyMultiRegionKey primaryKey;

    private List<KmsKeyMultiRegionKey> replicaKeys;

    public String getMultiRegionKeyType() {
        return multiRegionKeyType;
    }

    public void setMultiRegionKeyType(String multiRegionKeyType) {
        this.multiRegionKeyType = multiRegionKeyType;
    }

    public KmsKeyMultiRegionKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(KmsKeyMultiRegionKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<KmsKeyMultiRegionKey> getReplicaKeys() {
        return replicaKeys;
    }

    public void setReplicaKeys(List<KmsKeyMultiRegionKey> replicaKeys) {
        this.replicaKeys = replicaKeys;
    }

    @Override
    public void copyFrom(MultiRegionConfiguration model) {
        KmsKeyMultiRegionKey multiRegionKey = newSubresource(KmsKeyMultiRegionKey.class);
        multiRegionKey.copyFrom(model.primaryKey());

        setPrimaryKey(multiRegionKey);

        List<KmsKeyMultiRegionKey> replicaKeys = new ArrayList<>();
        for (MultiRegionKey replicaKey : model.replicaKeys()) {
            KmsKeyMultiRegionKey regionSpecificKey = newSubresource(KmsKeyMultiRegionKey.class);
            regionSpecificKey.copyFrom(replicaKey);
            replicaKeys.add(regionSpecificKey);
        }

        setReplicaKeys(replicaKeys);
        setMultiRegionKeyType(model.multiRegionKeyTypeAsString());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
