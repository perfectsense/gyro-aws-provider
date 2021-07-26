/*
 * Copyright 2021, Brightspot.
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

import java.util.Set;
import java.util.concurrent.TimeUnit;

import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.CertificateDetail;
import software.amazon.awssdk.services.acm.model.CertificateStatus;

/**
 * Wait for an ACM certificate to be issued.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::acm-certificate-wait wait-for-certificate-example
 *         certificate: $(aws::acm-certificate "example-certificate")
 *
 *         {@literal @}for record-set -in $(aws::record-set record-set-example-*)
 *             {@literal @}depends-on: $(record-set)
 *         {@literal @}end
 *     end
 */
@Type("acm-certificate-wait")
public class CertificateWait extends AwsResource {

    private AcmCertificateResource certificate;

    @Required
    public AcmCertificateResource getCertificate() {
        return certificate;
    }

    public void setCertificate(AcmCertificateResource certificate) {
        this.certificate = certificate;
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        waitForCertificateToBeIssued(TimeoutSettings.Action.CREATE);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {

    }

    private void waitForCertificateToBeIssued(TimeoutSettings.Action action) {
        AcmClient client = createClient(AcmClient.class);

        Wait.atMost(30, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, action)
            .until(() -> {
                CertificateDetail certificate = client.describeCertificate(r -> r.certificateArn(getCertificate().getArn()))
                    .certificate();
                return certificate != null && certificate.status().equals(CertificateStatus.ISSUED);
            });
    }
}
