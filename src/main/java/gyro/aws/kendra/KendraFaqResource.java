/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.kendra;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.KendraClient;
import software.amazon.awssdk.services.kendra.model.CreateFaqResponse;
import software.amazon.awssdk.services.kendra.model.DescribeFaqResponse;
import software.amazon.awssdk.services.kendra.model.FaqFileFormat;
import software.amazon.awssdk.services.kendra.model.FaqStatus;
import software.amazon.awssdk.services.kendra.model.ResourceNotFoundException;
import software.amazon.awssdk.services.kendra.model.Tag;
import software.amazon.awssdk.services.kendra.model.TagResourceRequest;
import software.amazon.awssdk.services.kendra.model.UntagResourceRequest;

/**
 * Creates a VPC with the specified IPv4 CIDR block.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::kendra-faq kendra-faq-example
 *         description: "example-faq-desc"
 *         index: $(aws::kendra-index index-example-enter)
 *         name: "example-faq"
 *         role: "arn:aws:iam::242040583208:role/service-role/AmazonKendra-faq"
 *         file-format: JSON
 *
 *         s3-path
 *             bucket: "example-kendra"
 *             key: "example-file"
 *         end
 *
 *         tags: {
 *             "example-key-1": "example-value-1"
 *         }
 *     end
 */
@Type("kendra-faq")
public class KendraFaqResource extends AwsResource implements Copyable<DescribeFaqResponse> {

    private String description;
    private KendraIndexResource index;
    private String name;
    private RoleResource role;
    private KendraS3Path s3Path;
    private FaqFileFormat fileFormat;
    private Map<String, String> tags;

    // Output
    private String id;

    /**
     * The description of the FAQ.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The index associated to the FAQ.
     */
    @Required
    public KendraIndexResource getIndex() {
        return index;
    }

    public void setIndex(KendraIndexResource index) {
        this.index = index;
    }

    /**
     * The name of the FAQ.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The role with permission to access the S3 bucket that contains the FAQs.
     */
    @Required
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    /**
     * The S3 location of the FAQ input data.
     */
    @Required
    public KendraS3Path getS3Path() {
        return s3Path;
    }

    public void setS3Path(KendraS3Path s3Path) {
        this.s3Path = s3Path;
    }

    /**
     * The format of the input file.
     */
    @Required
    public FaqFileFormat getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(FaqFileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }

    /**
     * The tags associated with the FAQ.
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
     * The ID of the FAQ.
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(DescribeFaqResponse model) {
        setDescription(model.description());
        setIndex(findById(KendraIndexResource.class, model.indexId()));
        setName(model.name());
        setRole(findById(RoleResource.class, model.roleArn()));
        setFileFormat(model.fileFormat());
        setId(model.id());

        KendraS3Path path = newSubresource(KendraS3Path.class);
        path.copyFrom(model.s3Path());
        setS3Path(path);

        getTags().clear();
        KendraClient client = createClient(KendraClient.class);
        client.listTagsForResource(r -> r.resourceARN(getArn())).tags().forEach(t -> getTags().put(t.key(), t.value()));
    }

    @Override
    public boolean refresh() {
        KendraClient client = createClient(KendraClient.class);

        DescribeFaqResponse faq = getFaq(client);

        if (faq == null) {
            return false;
        }

        copyFrom(faq);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        KendraClient client = createClient(KendraClient.class);

        CreateFaqResponse faq = client.createFaq(r -> r.description(getDescription())
            .fileFormat(getFileFormat())
            .indexId(getIndex().getId())
            .name(getName())
            .roleArn(getRole().getArn())
            .s3Path(getS3Path().toS3Path())
            .tags(getTags().entrySet().stream().map(e -> Tag.builder().key(e.getKey())
                .value(e.getValue()).build()).collect(Collectors.toList())));

        setId(faq.id());

        Wait.atMost(5, TimeUnit.MINUTES)
            .checkEvery(1, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getFaq(client).status().equals(FaqStatus.ACTIVE));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        if (changedFieldNames.contains("tags")) {
            KendraClient client = createClient(KendraClient.class);
            KendraFaqResource currentResource = (KendraFaqResource) current;

            if (!currentResource.getTags().isEmpty()) {
                client.untagResource(UntagResourceRequest.builder()
                    .resourceARN(getArn())
                    .tagKeys(currentResource.getTags().keySet())
                    .build());
            }

            client.tagResource(TagResourceRequest.builder()
                .resourceARN(getArn())
                .tags(getTags().entrySet()
                    .stream()
                    .map(t -> Tag.builder().key(t.getKey()).value(t.getValue()).build())
                    .collect(Collectors.toList()))
                .build());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        KendraClient client = createClient(KendraClient.class);

        client.deleteFaq(r -> r.id(getId()).indexId(getIndex().getId()));

        Wait.atMost(5, TimeUnit.MINUTES)
            .checkEvery(1, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getFaq(client) == null);
    }

    private DescribeFaqResponse getFaq(KendraClient client) {
        DescribeFaqResponse faq = null;

        try {
            faq = client.describeFaq(r -> r.id(getId()).indexId(getIndex().getId()));

        } catch (ResourceNotFoundException ignore) {
            // ignore
        }

        return faq;
    }

    private String getArn() {
        return String.format(
            "arn:aws:kendra:%s:%s:index/%s/faq/%s",
            credentials(AwsCredentials.class).getRegion(),
            getRole().getArn().split(":")[4],
            getIndex().getId(),
            getId());
    }
}
