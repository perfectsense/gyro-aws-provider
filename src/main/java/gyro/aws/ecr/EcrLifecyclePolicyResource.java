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
import software.amazon.awssdk.services.ecr.model.GetLifecyclePolicyResponse;
import software.amazon.awssdk.utils.IoUtils;

public class EcrLifecyclePolicyResource extends AwsResource implements Copyable<GetLifecyclePolicyResponse> {

    private String lifecyclePolicy;

    /**
     * The JSON policy to apply to the repository.
     */
    @Required
    @Updatable
    public String getLifecyclePolicy() {
        if (this.lifecyclePolicy != null && this.lifecyclePolicy.contains(".json")) {
            try (InputStream input = openInput(this.lifecyclePolicy)) {
                this.lifecyclePolicy = formatPolicy(IoUtils.toUtf8String(input));
                return this.lifecyclePolicy;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return this.lifecyclePolicy;
        }
    }

    public void setLifecyclePolicy(String lifecyclePolicy) {
        this.lifecyclePolicy = lifecyclePolicy;
    }

    @Override
    public void copyFrom(GetLifecyclePolicyResponse model) {
        setLifecyclePolicy(model.lifecyclePolicyText());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        putLifecyclePolicy();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        putLifecyclePolicy();
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EcrClient client = createClient(EcrClient.class);

        client.deleteLifecyclePolicy(r -> r.repositoryName(((EcrRepositoryResource) parentResource()).getRepositoryName()));
    }

    private String formatPolicy(String policy) {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ")
            .replaceAll("\t", " ").trim().replaceAll(" ", "") : policy;
    }

    private void putLifecyclePolicy() {
        EcrClient client = createClient(EcrClient.class);

        client.putLifecyclePolicy(r -> r.lifecyclePolicyText(
            getLifecyclePolicy()).repositoryName(((EcrRepositoryResource) parentResource()).getRepositoryName()));
    }
}
