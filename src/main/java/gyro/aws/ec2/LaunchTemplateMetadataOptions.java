/*
 * Copyright 2021, Perfect Sense.
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

package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateHttpTokensState;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateInstanceMetadataEndpointState;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateInstanceMetadataOptions;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateInstanceMetadataOptionsRequest;

public class LaunchTemplateMetadataOptions extends Diffable implements Copyable<LaunchTemplateInstanceMetadataOptions> {

    private LaunchTemplateInstanceMetadataEndpointState httpEndpoint;
    private Integer httpPutResponseHopLimit;
    private LaunchTemplateHttpTokensState httpTokens;

    @Override
    public String primaryKey() {
        return null;
    }

    public LaunchTemplateInstanceMetadataEndpointState getHttpEndpoint() {
        return httpEndpoint;
    }

    public void setHttpEndpoint(LaunchTemplateInstanceMetadataEndpointState httpEndpoint) {
        this.httpEndpoint = httpEndpoint;
    }

    public Integer getHttpPutResponseHopLimit() {
        return httpPutResponseHopLimit;
    }

    public void setHttpPutResponseHopLimit(Integer httpPutResponseHopLimit) {
        this.httpPutResponseHopLimit = httpPutResponseHopLimit;
    }

    public LaunchTemplateHttpTokensState getHttpTokens() {
        return httpTokens;
    }

    public void setHttpTokens(LaunchTemplateHttpTokensState httpTokens) {
        this.httpTokens = httpTokens;
    }

    public LaunchTemplateInstanceMetadataOptionsRequest toMetadataOptions() {
        return LaunchTemplateInstanceMetadataOptionsRequest.builder()
            .httpEndpoint(getHttpEndpoint())
            .httpPutResponseHopLimit(getHttpPutResponseHopLimit())
            .httpTokens(getHttpTokens())
            .build();
    }

    @Override
    public void copyFrom(LaunchTemplateInstanceMetadataOptions model) {
        setHttpEndpoint(model.httpEndpoint());
        setHttpTokens(model.httpTokens());
        setHttpPutResponseHopLimit(model.httpPutResponseHopLimit());
    }
}
