/*
 * Copyright 2023, Brightspot.
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

package gyro.aws.cloudfront;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import org.apache.commons.codec.digest.DigestUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CreateFunctionResponse;
import software.amazon.awssdk.services.cloudfront.model.DescribeFunctionResponse;
import software.amazon.awssdk.services.cloudfront.model.FunctionStage;
import software.amazon.awssdk.services.cloudfront.model.FunctionSummary;
import software.amazon.awssdk.services.cloudfront.model.GetFunctionResponse;
import software.amazon.awssdk.services.cloudfront.model.NoSuchFunctionExistsException;
import software.amazon.awssdk.services.cloudfront.model.PublishFunctionResponse;
import software.amazon.awssdk.services.cloudfront.model.UpdateFunctionResponse;

@Type("cloudfront-function")
public class CloudFrontFunctionResource extends AwsResource implements Copyable<FunctionSummary> {

    private String name;
    private CloudFrontFunctionConfig config;
    private String contentZipPath;
    private Boolean publish;

    private String createdTime;
    private String lastModifiedTime;
    private String arn;
    private FunctionStage stage;
    private String status;

    /**
     * The name of the function.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Configuration of the function.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontFunctionConfig
     */
    @Required
    @Updatable
    public CloudFrontFunctionConfig getConfig() {
        return config;
    }

    public void setConfig(CloudFrontFunctionConfig config) {
        this.config = config;
    }

    /**
     * If set to ``true`` then the function will be published. Defaults to ``false``.
     */
    @Updatable
    public Boolean getPublish() {
        if (publish == null) {
            publish = false;
        }

        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    /**
     * The time when the function was created.
     */
    @Output
    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * The time when the function was last modified.
     */
    @Output
    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * The ARN of the function.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The stage of the function. Can be ``DEVELOPMENT`` or ``LIVE``.
     */
    @Output
    public FunctionStage getStage() {
        return stage;
    }

    public void setStage(FunctionStage stage) {
        this.stage = stage;
    }

    /**
     * The status of the function.
     */
    @Output
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Required
    @Updatable
    public String getContentZipPath() {
        if (!ObjectUtils.isBlank(contentZipPath) && !contentZipPath.startsWith("Path:")) {
            return String.format("Path: %s, Hash: %s", contentZipPath, getFileHashFromPath());
        } else {
            return contentZipPath;
        }
    }

    private String getContentZipPathRaw() {
        return contentZipPath;
    }

    public void setContentZipPath(String contentZipPath) {
        this.contentZipPath = contentZipPath;
    }

    @Override
    public void copyFrom(FunctionSummary model) {
        setName(model.name());

        CloudFrontFunctionConfig config = newSubresource(CloudFrontFunctionConfig.class);
        config.copyFrom(model.functionConfig());
        setConfig(config);

        setArn(model.functionMetadata().functionARN());
        setCreatedTime(model.functionMetadata().createdTime().toString());
        setLastModifiedTime(model.functionMetadata().lastModifiedTime().toString());
        setStage(model.functionMetadata().stage());
        setStatus(model.status());

        if (model.functionMetadata().stage().equals(FunctionStage.DEVELOPMENT)) {
            setPublish(false);
        } else {
            setPublish(true);
        }
    }

    @Override
    public boolean refresh() {
        CloudFrontClient client = createClient(
            CloudFrontClient.class,
            "us-east-1",
            "https://cloudfront.amazonaws.com");

        try {
            DescribeFunctionResponse response = client.describeFunction(r -> r.name(getName()));

            copyFrom(response.functionSummary());

            return true;
        } catch (NoSuchFunctionExistsException ex) {
            return false;
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = createClient(
            CloudFrontClient.class,
            "us-east-1",
            "https://cloudfront.amazonaws.com");

        CreateFunctionResponse function = client.createFunction(r -> r
            .name(getName())
            .functionCode(getZipFile())
            .functionConfig(getConfig().toFunctionConfig())
            .build());

        copyFrom(function.functionSummary());

        if (getPublish()) {
            PublishFunctionResponse response = client.publishFunction(r -> r.name(getName())
                .ifMatch(function.eTag()));

            copyFrom(response.functionSummary());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CloudFrontFunctionResource functionResource = (CloudFrontFunctionResource) current;
        if (changedFieldNames.contains("publish")) {
            if (functionResource.getPublish() && !getPublish()) {
                throw new GyroException("'publish' cannot be turned to false once set to true");
            }
        }

        CloudFrontClient client = createClient(
            CloudFrontClient.class,
            "us-east-1",
            "https://cloudfront.amazonaws.com");

        GetFunctionResponse fn = getFunction(client);

        UpdateFunctionResponse function = client.updateFunction(r -> r.name(getName())
            .functionCode(getZipFile())
            .functionConfig(getConfig().toFunctionConfig())
            .ifMatch(fn.eTag())
            .build());

        if (getPublish()) {
            PublishFunctionResponse response = client.publishFunction(r -> r.name(getName())
                .ifMatch(function.eTag()));

            copyFrom(response.functionSummary());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = createClient(
            CloudFrontClient.class,
            "us-east-1",
            "https://cloudfront.amazonaws.com");

        GetFunctionResponse fn = getFunction(client);

        if (fn != null) {
            try {
                client.deleteFunction(r -> r.name(getName()).ifMatch(fn.eTag()));
            } catch (NoSuchFunctionExistsException ex) {
                // ignore
            }
        }
    }

    private GetFunctionResponse getFunction(CloudFrontClient client) {
        GetFunctionResponse function = null;

        try {
            function = client.getFunction(r -> r.name(getName()));
        } catch (NoSuchFunctionExistsException ex) {
            // ignore
        }

        return function;
    }

    private SdkBytes getZipFile() {
        try (InputStream input = openInput(getContentZipPathRaw())) {
            return SdkBytes.fromInputStream(input);

        } catch (IOException ex) {
            throw new GyroException(String.format("File not found - %s",getContentZipPathRaw()));
        }
    }

    private String getFileHashFromPath() {
        String hash = "";
        try (InputStream input = openInput(getContentZipPathRaw())) {
            hash = DigestUtils.sha256Hex(input);

        } catch (Exception ignore) {
            // ignore
        }

        return hash;
    }
}
