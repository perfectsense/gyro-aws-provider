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

package gyro.aws.opensearch;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.opensearch.model.AWSDomainInformation;

public class OpenSearchAWSDomainInformation extends Diffable implements Copyable<AWSDomainInformation> {

    private OpenSearchDomainResource domain;
    private String ownerId;
    private String region;

    /**
     * The opensearch service domain.
     */
    @Required
    public OpenSearchDomainResource getDomain() {
        return domain;
    }

    public void setDomain(OpenSearchDomainResource domain) {
        this.domain = domain;
    }

    /**
     * An override for the region in which the domain is located. This value will be calculated from {@link #domain} if not set.
     */
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * An override for the AWS account ID of the domain owner. This value will be calculated from {@link #domain} if not set.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public void copyFrom(AWSDomainInformation model) {
        setRegion(model.region());
        setOwnerId(model.ownerId());
        setDomain(findById(OpenSearchDomainResource.class, getArnFormat(
            model.domainName(),
            model.region(),
            model.ownerId())));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    AWSDomainInformation toAWSDomainInformation() {
        return AWSDomainInformation.builder().domainName(getDomain().getDomainName())
            .region(getRegion() != null ? getRegion() : getDomain().getRegion())
            .ownerId(getOwnerId() != null ? getOwnerId() : getDomain().getOwnerId()).build();
    }

    public static String getArnFormat(String domainName, String region, String accountId) {
        return String.format("arn:aws:es:%s:%s:domain/%s", region, accountId, domainName);
    }
}
