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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.cloudfront.model.FunctionConfig;
import software.amazon.awssdk.services.cloudfront.model.FunctionRuntime;
import software.amazon.awssdk.services.cloudfront.model.KeyValueStoreAssociation;
import software.amazon.awssdk.services.cloudfront.model.KeyValueStoreAssociations;

public class CloudFrontFunctionConfig extends Diffable implements Copyable<FunctionConfig> {

    private String comment;
    private FunctionRuntime runtime;
    private List<CloudFrontKeyValueStoreResource> keyValueStoreAssociations;

    /**
     * A comment to describe the function.
     */
    @Updatable
    public String getComment() {
        if (comment == null) {
            comment = "";
        }

        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * The runtime environment for the function. Defaults to ``cloudfront-js-1.0``.
     */
    @ValidStrings({"cloudfront-js-1.0", "cloudfront-js-2.0"})
    @Updatable
    public FunctionRuntime getRuntime() {
        if (runtime == null) {
            runtime = FunctionRuntime.CLOUDFRONT_JS_1_0;
        }

        return runtime;
    }

    public void setRuntime(FunctionRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * A list of key value stores to associate with the function.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontKeyValueStoreResource
     */
    @Updatable
    public List<CloudFrontKeyValueStoreResource> getKeyValueStoreAssociations() {
        if (keyValueStoreAssociations == null) {
            keyValueStoreAssociations = new ArrayList<>();
        }
        return keyValueStoreAssociations;
    }

    public void setKeyValueStoreAssociations(List<CloudFrontKeyValueStoreResource> keyValueStoreAssociations) {
        this.keyValueStoreAssociations = keyValueStoreAssociations;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(FunctionConfig model) {
        setComment(model.comment());
        setRuntime(model.runtime());

        getKeyValueStoreAssociations().clear();
        if (model.keyValueStoreAssociations() != null && model.keyValueStoreAssociations().items() != null) {
            model.keyValueStoreAssociations().items().forEach(kvsAssoc -> {
                CloudFrontKeyValueStoreResource kvs =
                    findById(CloudFrontKeyValueStoreResource.class, kvsAssoc.keyValueStoreARN());
                if (kvs != null) {
                    getKeyValueStoreAssociations().add(kvs);
                }
            });
        }
    }

    FunctionConfig toFunctionConfig() {
        FunctionConfig.Builder builder = FunctionConfig.builder()
            .comment(getComment())
            .runtime(getRuntime());

        if (!getKeyValueStoreAssociations().isEmpty()) {
            builder.keyValueStoreAssociations(
                KeyValueStoreAssociations.builder()
                    .quantity(getKeyValueStoreAssociations().size())
                    .items(getKeyValueStoreAssociations().stream()
                        .map(kvs -> KeyValueStoreAssociation.builder()
                            .keyValueStoreARN(kvs.getArn())
                            .build())
                        .collect(Collectors.toList()))
                    .build()
            );
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!getKeyValueStoreAssociations().isEmpty() && !FunctionRuntime.CLOUDFRONT_JS_2_0.equals(getRuntime())) {
            errors.add(new ValidationError(this, "key-value-store-associations",
                "Key-Value Store associations require runtime 'cloudfront-js-2.0'"));
        }

        if (getKeyValueStoreAssociations().size() > 1) {
            errors.add(new ValidationError(this, "key-value-store-associations",
                "Only one Key-Value Store can be associated with a CloudFront Function"));
        }

        return errors;
    }
}
