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

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.kms.model.MultiRegionConfiguration;
import software.amazon.awssdk.services.kms.model.MultiRegionKey;
import software.amazon.awssdk.services.kms.model.MultiRegionKeyType;

public class KmsKeyMultiRegionConfiguration extends Diffable implements Copyable<MultiRegionConfiguration> {

    private MultiRegionKeyType multiRegionKeyType;
    private KmsKeyMultiRegionKey primaryKmsKey;
    private List<KmsKeyMultiRegionKey> replicaKeys;

    /**
     * The type of multi-region key.
     */
    @Output
    @ValidStrings({ "PRIMARY", "REPLICA" })
    public MultiRegionKeyType getMultiRegionKeyType() {
        return multiRegionKeyType;
    }

    public void setMultiRegionKeyType(MultiRegionKeyType multiRegionKeyType) {
        this.multiRegionKeyType = multiRegionKeyType;
    }

    /**
     * The primary key.
     */
    @Output
    public KmsKeyMultiRegionKey getPrimaryKmsKey() {
        return primaryKmsKey;
    }

    public void setPrimaryKmsKey(KmsKeyMultiRegionKey primaryKmsKey) {
        this.primaryKmsKey = primaryKmsKey;
    }

    /**
     * The list of replica keys.
     */
    @Output
    public List<KmsKeyMultiRegionKey> getReplicaKeys() {
        return replicaKeys;
    }

    public void setReplicaKeys(List<KmsKeyMultiRegionKey> replicaKeys) {
        this.replicaKeys = replicaKeys;
    }

    @Override
    public void copyFrom(MultiRegionConfiguration model) {
        setPrimaryKmsKey(null);
        if (model.primaryKey() != null) {
            KmsKeyMultiRegionKey multiRegionKey = newSubresource(KmsKeyMultiRegionKey.class);
            multiRegionKey.copyFrom(model.primaryKey());
            setPrimaryKmsKey(multiRegionKey);
        }

        List<KmsKeyMultiRegionKey> replicaKeys = new ArrayList<>();
        for (MultiRegionKey replicaKey : model.replicaKeys()) {
            KmsKeyMultiRegionKey regionSpecificKey = newSubresource(KmsKeyMultiRegionKey.class);
            regionSpecificKey.copyFrom(replicaKey);
            replicaKeys.add(regionSpecificKey);
        }

        setReplicaKeys(replicaKeys);
        setMultiRegionKeyType(model.multiRegionKeyType());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
