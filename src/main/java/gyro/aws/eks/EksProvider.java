/*
 * Copyright 2020, Perfect Sense, Inc.
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
import gyro.aws.kms.KmsKeyResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.model.Provider;

public class EksProvider extends Diffable implements Copyable<Provider> {

    private KmsKeyResource key;

    /**
     * The Key Management Service (KMS) customer master key (CMK) for the cluster.
     */
    @Required
    public KmsKeyResource getKey() {
        return key;
    }

    public void setKey(KmsKeyResource key) {
        this.key = key;
    }

    @Override
    public void copyFrom(Provider model) {
        setKey(findById(KmsKeyResource.class, model.keyArn()));
    }

    @Override
    public String primaryKey() {
        return getKey().getArn();
    }

    Provider toProvider() {
        return Provider.builder().keyArn(getKey().getArn()).build();
    }
}
