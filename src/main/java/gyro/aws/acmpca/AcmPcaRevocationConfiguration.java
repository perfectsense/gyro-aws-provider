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

package gyro.aws.acmpca;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.acmpca.model.RevocationConfiguration;

public class AcmPcaRevocationConfiguration extends Diffable implements Copyable<RevocationConfiguration> {
    private AcmPcaCrlConfiguration crlConfiguration;

    /**
     * The Crl configuration.
     *
     * @subresource gyro.aws.acmpca.AcmPcaCrlConfiguration
     */
    @Updatable
    public AcmPcaCrlConfiguration getCrlConfiguration() {
        if (crlConfiguration == null) {
            crlConfiguration = newSubresource(AcmPcaCrlConfiguration.class);
        }

        return crlConfiguration;
    }

    public void setCrlConfiguration(AcmPcaCrlConfiguration crlConfiguration) {
        this.crlConfiguration = crlConfiguration;
    }

    @Override
    public void copyFrom(RevocationConfiguration revocationConfiguration) {
        AcmPcaCrlConfiguration crlConfiguration = newSubresource(AcmPcaCrlConfiguration.class);
        crlConfiguration.copyFrom(revocationConfiguration.crlConfiguration());
        setCrlConfiguration(crlConfiguration);
    }

    @Override
    public String primaryKey() {
        return "revocation configuration";
    }

    RevocationConfiguration toRevocationConfiguration() {
        return RevocationConfiguration.builder()
            .crlConfiguration(getCrlConfiguration().toCrlConfiguration())
            .build();
    }
}
