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
import software.amazon.awssdk.services.opensearch.model.DomainInformationContainer;

public class OpenSearchDomainInformationContainer extends Diffable implements Copyable<DomainInformationContainer> {

    private OpenSearchAWSDomainInformation awsDomainInformation;

    @Override
    public void copyFrom(DomainInformationContainer model) {
        setAwsDomainInformation(null);
        if (model.awsDomainInformation() != null) {
            OpenSearchAWSDomainInformation newAwsDomainInformation = newSubresource(OpenSearchAWSDomainInformation.class);
            awsDomainInformation.copyFrom(model.awsDomainInformation());
            setAwsDomainInformation(newAwsDomainInformation);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public OpenSearchAWSDomainInformation getAwsDomainInformation() {
        return awsDomainInformation;
    }

    public void setAwsDomainInformation(OpenSearchAWSDomainInformation awsDomainInformation) {
        this.awsDomainInformation = awsDomainInformation;
    }
}
