/*
 * Copyright 2021, Brightspot, Inc.
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

package gyro.aws.ecr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.GetRepositoryPolicyResponse;
import software.amazon.awssdk.utils.IoUtils;

public class EcrRepositoryPolicyResource extends AwsResource implements Copyable<GetRepositoryPolicyResponse> {

    private String policy;
    private Boolean force;

    /**
     * The JSON repository policy to apply to the repository.
     */
    @Required
    @Updatable
    public String getPolicy() {
        if (this.policy != null && this.policy.contains(".json")) {
            try (InputStream input = openInput(this.policy)) {
                this.policy = formatPolicy(IoUtils.toUtf8String(input));
                return this.policy;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return formatPolicy(this.policy);
        }
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    /**
     * When set to ``true``, the policy is forcefully changed.
     */
    public Boolean getForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    @Override
    public void copyFrom(GetRepositoryPolicyResponse model) {
        setPolicy(model.policyText());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public String primaryKey() {
        return "repository policy";
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        setPolicy();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        setPolicy();
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EcrClient client = createClient(EcrClient.class);

        client.deleteRepositoryPolicy(r -> r.repositoryName(((EcrRepositoryResource) parentResource()).getRepositoryName()));

    }

    private String formatPolicy(String policy) {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ").replaceAll("\n", "")
            .replaceAll("\t", " ").trim().replaceAll(" ", "") : policy;
    }

    private void setPolicy() {
        EcrClient client = createClient(EcrClient.class);

        client.setRepositoryPolicy(r -> r.force(getForce()).policyText(getPolicy())
            .repositoryName(((EcrRepositoryResource) parentResource()).getRepositoryName()));
    }
}
