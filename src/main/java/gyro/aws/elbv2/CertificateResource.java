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

package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.diff.Delete;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Certificate;

import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     certificate
 *         arn: "arn:aws:acm:us-east-2:acct-number:certificate/certificatearn"
 *     end
 */
public class CertificateResource extends AwsResource {

    private String arn;
    private Boolean isDefault;

    /**
     *  ARN of the certificate. (Required)
     */
    @Required
    @Updatable
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     *  Determines if the certificate is default. (Optional)
     */
    @Updatable
    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getListenerArn() {
        ListenerResource parent;

        if (parentResource() instanceof ApplicationLoadBalancerListenerResource) {
            parent = (ApplicationLoadBalancerListenerResource) parentResource();
        } else {
            parent = (ApplicationLoadBalancerListenerResource) parentResource();
        }

        if (parent != null) {
            return parent.getArn();
        }

        return null;
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getArn());
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        client.addListenerCertificates(r -> r.certificates(toCertificate())
                                            .listenerArn(getListenerArn()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {}

    @Override
    public void delete(GyroUI ui, State state) {
        if (DiffableInternals.getChange(parentResource()) instanceof Delete) {
            return;
        }

        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.removeListenerCertificates(r -> r.certificates(toCertificate())
                .listenerArn(getListenerArn()));
    }

    private Certificate toCertificate() {
        return Certificate.builder()
                .certificateArn(getArn())
                .build();
    }
}
