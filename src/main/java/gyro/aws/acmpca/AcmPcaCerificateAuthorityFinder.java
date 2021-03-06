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

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.acmpca.AcmPcaClient;
import software.amazon.awssdk.services.acmpca.model.CertificateAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query certificate authority.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    certificate-authority: $(external-query aws::acmpca-certificate-authority {})
 */
@Type("acmpca-certificate-authority")
public class AcmPcaCerificateAuthorityFinder extends AwsFinder<AcmPcaClient, CertificateAuthority, AcmPcaCertificateAuthority> {
    private String arn;

    /**
     * The arn of a Certificate Authority.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<CertificateAuthority> findAllAws(AcmPcaClient client) {
        return client.listCertificateAuthoritiesPaginator().certificateAuthorities().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CertificateAuthority> findAws(AcmPcaClient client, Map<String, String> filters) {
        return Collections.singletonList(client.describeCertificateAuthority(r -> r.certificateAuthorityArn(filters.get("arn"))).certificateAuthority());
    }
}
