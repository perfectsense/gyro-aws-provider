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
import gyro.core.resource.Output;
import software.amazon.awssdk.services.acm.model.RenewalSummary;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AcmRenewalSummary extends Diffable implements Copyable<RenewalSummary> {
    private Set<AcmDomainValidationOption> domainValidationOptions;
    private String renewalStatus;
    private String renewalStatusReason;
    private Date updatedAt;

    /**
     * Information about the validation of each domain name in the certificate.
     *
     * @subresource gyro.aws.acm.AcmDomainValidationOption
     */
    @Output
    public Set<AcmDomainValidationOption> getDomainValidationOptions() {
        if (domainValidationOptions == null) {
            domainValidationOptions = new HashSet<>();
        }

        return domainValidationOptions;
    }

    public void setDomainValidationOptions(Set<AcmDomainValidationOption> domainValidationOptions) {
        this.domainValidationOptions = domainValidationOptions;
    }

    /**
     * The status of ACM's managed renewal of the certificate.
     */
    @Output
    public String getRenewalStatus() {
        return renewalStatus;
    }

    public void setRenewalStatus(String renewalStatus) {
        this.renewalStatus = renewalStatus;
    }

    /**
     * The reason that a renewal request was unsuccessful.
     */
    @Output
    public String getRenewalStatusReason() {
        return renewalStatusReason;
    }

    public void setRenewalStatusReason(String renewalStatusReason) {
        this.renewalStatusReason = renewalStatusReason;
    }

    /**
     * The time at which the renewal summary was last updated.
     */
    @Output
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public void copyFrom(RenewalSummary renewalSummary) {
        setRenewalStatus(renewalSummary.renewalStatusAsString());
        setRenewalStatusReason(renewalSummary.renewalStatusReasonAsString());
        setUpdatedAt(renewalSummary.updatedAt() != null ? Date.from(renewalSummary.updatedAt()) : null);
        setDomainValidationOptions(renewalSummary.domainValidationOptions().stream().map( o -> {
            AcmDomainValidationOption domainValidationOption = newSubresource(AcmDomainValidationOption.class);
            domainValidationOption.copyFrom(o);
            return domainValidationOption;
        }).collect(Collectors.toSet()));
    }

    @Override
    public String primaryKey() {
        return "renewal summary";
    }
}
