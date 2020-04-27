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
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.model.EncryptionConfig;

public class EksEncryptionConfig extends Diffable implements Copyable<EncryptionConfig> {

    private EksProvider provider;

    /**
     * The provider to use for the cluster. (Required)
     */
    @Required
    public EksProvider getProvider() {
        return provider;
    }

    public void setProvider(EksProvider provider) {
        this.provider = provider;
    }

    @Override
    public void copyFrom(EncryptionConfig model) {
        EksProvider eksProvider = newSubresource(EksProvider.class);
        eksProvider.copyFrom(model.provider());
        setProvider(eksProvider);
    }

    @Override
    public String primaryKey() {
        return null;
    }

    EncryptionConfig toEncryptionConfig() {
        return EncryptionConfig.builder().provider(getProvider().toProvider()).resources("secrets").build();
    }
}
