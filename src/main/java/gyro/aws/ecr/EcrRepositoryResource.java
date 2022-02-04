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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.PolicyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.CreateRepositoryResponse;
import software.amazon.awssdk.services.ecr.model.DescribeRepositoriesResponse;
import software.amazon.awssdk.services.ecr.model.GetLifecyclePolicyResponse;
import software.amazon.awssdk.services.ecr.model.GetRepositoryPolicyResponse;
import software.amazon.awssdk.services.ecr.model.ImageTagMutability;
import software.amazon.awssdk.services.ecr.model.LifecyclePolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.ecr.model.Repository;
import software.amazon.awssdk.services.ecr.model.RepositoryNotFoundException;
import software.amazon.awssdk.services.ecr.model.RepositoryPolicyNotFoundException;
import software.amazon.awssdk.services.ecr.model.Tag;
import software.amazon.awssdk.services.ecr.model.TagResourceRequest;
import software.amazon.awssdk.services.ecr.model.UntagResourceRequest;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Create ECR repository.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::ecr-repository example-repo
 *         repository-name: "example-repo"
 *         lifecycle-policy: 'lifecycle-policy.json'
 *
 *         repository-policy
 *             policy: 'repository-policy.json'
 *         end
 *
 *         image-scanning-configuration
 *             scan-on-push: false
 *         end
 *     end
 */
@Type("ecr-repository")
public class EcrRepositoryResource extends AwsResource implements Copyable<Repository> {

    private EcrEncryptionConfiguration encryptionConfiguration;
    private EcrImageScanningConfiguration imageScanningConfiguration;
    private ImageTagMutability imageTagMutability;
    private String repositoryName;
    private String lifecyclePolicy;
    private EcrRepositoryPolicyResource repositoryPolicy;
    private Map<String, String> tags;

    // Read-only
    private String arn;
    private String uri;

    /**
     * The encryption configuration for the repository.
     *
     * @subresource gyro.aws.ecr.EcrEncryptionConfiguration
     */
    public EcrEncryptionConfiguration getEncryptionConfiguration() {
        return encryptionConfiguration;
    }

    public void setEncryptionConfiguration(EcrEncryptionConfiguration encryptionConfiguration) {
        this.encryptionConfiguration = encryptionConfiguration;
    }

    /**
     * The image scanning configuration for the repository.
     *
     * @subresource gyro.aws.ecr.EcrImageScanningConfiguration
     */
    @Updatable
    public EcrImageScanningConfiguration getImageScanningConfiguration() {
        return imageScanningConfiguration;
    }

    public void setImageScanningConfiguration(EcrImageScanningConfiguration imageScanningConfiguration) {
        this.imageScanningConfiguration = imageScanningConfiguration;
    }

    /**
     * The tag mutability setting for the repository. Defaults to ``MUTABLE``.
     */
    @Updatable
    @ValidStrings({ "MUTABLE", "IMMUTABLE" })
    public ImageTagMutability getImageTagMutability() {
        if (imageTagMutability == null) {
            imageTagMutability = ImageTagMutability.MUTABLE;
        }

        return imageTagMutability;
    }

    public void setImageTagMutability(ImageTagMutability imageTagMutability) {
        this.imageTagMutability = imageTagMutability;
    }

    /**
     * The name of the repository.
     */
    @Required
    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    /**
     * The lifecycle policy for the repository.
     */
    @Updatable
    public String getLifecyclePolicy() {
        if (this.lifecyclePolicy != null && this.lifecyclePolicy.contains(".json")) {
            try (InputStream input = openInput(this.lifecyclePolicy)) {
                this.lifecyclePolicy = PolicyResource.formatPolicy(IoUtils.toUtf8String(input));
                return this.lifecyclePolicy;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return PolicyResource.formatPolicy(this.lifecyclePolicy);
        }
    }

    public void setLifecyclePolicy(String lifecyclePolicy) {
        this.lifecyclePolicy = lifecyclePolicy;
    }

    /**
     * The policy for the repository.
     *
     * @subresource gyro.aws.ecr.EcrRepositoryPolicyResource
     */
    public EcrRepositoryPolicyResource getRepositoryPolicy() {
        return repositoryPolicy;
    }

    public void setRepositoryPolicy(EcrRepositoryPolicyResource repositoryPolicy) {
        this.repositoryPolicy = repositoryPolicy;
    }

    /**
     * The list of tags for the repository.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The ARN of the repository.
     */
    @Output
    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The URI of the repository.
     */
    @Output
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public void copyFrom(Repository model) {
        setArn(model.repositoryArn());
        setRepositoryName(model.repositoryName());
        setImageTagMutability(model.imageTagMutability());
        setUri(model.repositoryUri());

        setEncryptionConfiguration(null);
        if (model.encryptionConfiguration() != null) {
            EcrEncryptionConfiguration config = newSubresource(EcrEncryptionConfiguration.class);
            config.copyFrom(model.encryptionConfiguration());
            setEncryptionConfiguration(config);
        }

        setImageScanningConfiguration(null);
        if (model.encryptionConfiguration() != null) {
            EcrImageScanningConfiguration config = newSubresource(EcrImageScanningConfiguration.class);
            config.copyFrom(model.imageScanningConfiguration());
            setImageScanningConfiguration(config);
        }

        EcrClient client = createClient(EcrClient.class);

        getTags().clear();
        ListTagsForResourceResponse response = client.listTagsForResource(r -> r.resourceArn(getArn()));
        if (response.hasTags()) {
            response.tags().forEach(r -> getTags().put(r.key(), r.value()));
        }

        try {
            GetRepositoryPolicyResponse policyResponse = client.getRepositoryPolicy(r -> r.repositoryName(
                getRepositoryName()));
            if (policyResponse != null) {
                EcrRepositoryPolicyResource policy = newSubresource(EcrRepositoryPolicyResource.class);
                policy.copyFrom(policyResponse);
                setRepositoryPolicy(policy);
            }
        } catch (RepositoryPolicyNotFoundException ex) {
            // ignore
        }

        try {
            GetLifecyclePolicyResponse lifecyclePolicyResponse = client.getLifecyclePolicy(r -> r.repositoryName(
                getRepositoryName()));
            if (lifecyclePolicyResponse != null) {
                setLifecyclePolicy(lifecyclePolicyResponse.lifecyclePolicyText());
            }
        } catch (LifecyclePolicyNotFoundException ex) {
            // ignore
        }
    }

    @Override
    public boolean refresh() {
        EcrClient client = createClient(EcrClient.class);

        Repository repository = getRepository(client);

        if (repository == null) {
            return false;
        }

        copyFrom(repository);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EcrClient client = createClient(EcrClient.class);

        CreateRepositoryResponse response = client.createRepository(r -> r.repositoryName(getRepositoryName())
            .encryptionConfiguration(getEncryptionConfiguration() == null
                ? null : getEncryptionConfiguration().toEncryptionConfiguration())
            .imageScanningConfiguration(getImageScanningConfiguration() == null
                ? null : getImageScanningConfiguration().toImageScanningConfiguration())
            .imageTagMutability(getImageTagMutability())
            .tags(getTags().entrySet()
                .stream()
                .map(t -> Tag.builder().key(t.getKey()).value(t.getValue()).build())
                .collect(Collectors.toList())));

        setArn(response.repository().repositoryArn());

        state.save();

        Wait.atMost(10, TimeUnit.SECONDS)
            .checkEvery(2, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .until(() -> getRepository(client) != null);

        if (getLifecyclePolicy() != null) {
            putLifecyclePolicy(client);
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EcrClient client = createClient(EcrClient.class);

        if (changedFieldNames.contains("tags")) {
            EcrRepositoryResource currentResource = (EcrRepositoryResource) current;

            if (!currentResource.getTags().isEmpty()) {
                client.untagResource(UntagResourceRequest.builder()
                    .resourceArn(getArn())
                    .tagKeys(currentResource.getTags().keySet())
                    .build());
            }

            client.tagResource(TagResourceRequest.builder().resourceArn(getArn()).tags(getTags().entrySet()
                .stream().map(t -> Tag.builder().key(t.getKey()).value(t.getValue()).build())
                .collect(Collectors.toList())).build());
        }

        if (changedFieldNames.contains("lifecycle-policy")) {
            if (getLifecyclePolicy() == null) {
                client.deleteLifecyclePolicy(r -> r.repositoryName((getRepositoryName())));

            } else {
                putLifecyclePolicy(client);
            }
        }

        if (changedFieldNames.contains("image-tag-mutability")) {
            client.putImageTagMutability(r -> r.repositoryName(getRepositoryName())
                .imageTagMutability(getImageTagMutability()));
        }

        if (changedFieldNames.contains("image-scanning-configuration")) {
            client.putImageScanningConfiguration(r -> r.repositoryName(getRepositoryName())
                .imageScanningConfiguration(getImageScanningConfiguration() != null
                    ? getImageScanningConfiguration().toImageScanningConfiguration()
                    : newSubresource(EcrImageScanningConfiguration.class).toImageScanningConfiguration()));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EcrClient client = createClient(EcrClient.class);

        client.deleteRepository(r -> r.repositoryName(getRepositoryName()));
    }

    private Repository getRepository(EcrClient client) {
        Repository repository = null;

        try {
            DescribeRepositoriesResponse response = client.describeRepositories(r -> r.repositoryNames(getRepositoryName()));
            repository = response.repositories().isEmpty() ? null : response.repositories().get(0);

        } catch (RepositoryNotFoundException ex) {
            // ignore
        }

        return repository;
    }

    private void putLifecyclePolicy(EcrClient client) {
        client.putLifecyclePolicy(r -> r.lifecyclePolicyText(
            getLifecyclePolicy()).repositoryName(getRepositoryName()));
    }
}
