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
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.acm.model.CertificateOptions;
import software.amazon.awssdk.services.acm.model.CertificateTransparencyLoggingPreference;

public class AcmCertificateOptions extends Diffable implements Copyable<CertificateOptions> {
    private CertificateTransparencyLoggingPreference preference;

    /**
     * Enable or Disable certificate transparency logging. Valid values are ``ENABLED`` or ``DISABLED``. Defaults to ``DISABLED``.
     */
    @Updatable
    public CertificateTransparencyLoggingPreference getPreference() {
        if (preference == null) {
            preference = CertificateTransparencyLoggingPreference.DISABLED;
        }

        return preference;
    }

    public void setPreference(CertificateTransparencyLoggingPreference preference) {
        this.preference = preference;
    }

    @Override
    public void copyFrom(CertificateOptions certificateOptions) {
        setPreference(certificateOptions.certificateTransparencyLoggingPreference());
    }

    @Override
    public String primaryKey() {
        return "certificate options";
    }

    CertificateOptions toCertificateOptions() {
        return CertificateOptions.builder()
            .certificateTransparencyLoggingPreference(getPreference())
            .build();
    }
}
