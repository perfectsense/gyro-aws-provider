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

package gyro.aws.autoscaling;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.autoscalingplans.model.ApplicationSource;

public class AutoScalingApplicationSource extends Diffable implements Copyable<ApplicationSource> {

    private String cloudFormationStackArn;
    private List<AutoScalingTagFilter> tagFilter;

    /**
     * The arn of a CloudFormation stack.
     */
    @Updatable
    @ConflictsWith("tag-filter")
    public String getCloudFormationStackArn() {
        return cloudFormationStackArn;
    }

    public void setCloudFormationStackArn(String cloudFormationStackArn) {
        this.cloudFormationStackArn = cloudFormationStackArn;
    }

    /**
     * The tag filters for the application source.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingTagFilter
     */
    @Updatable
    @CollectionMax(50)
    @ConflictsWith("cloud-formation-stack-arn")
    public List<AutoScalingTagFilter> getTagFilter() {
        if (tagFilter == null) {
            tagFilter = new ArrayList<>();
        }

        return tagFilter;
    }

    public void setTagFilter(List<AutoScalingTagFilter> tagFilter) {
        this.tagFilter = tagFilter;
    }

    @Override
    public void copyFrom(ApplicationSource model) {
        setCloudFormationStackArn(model.cloudFormationStackARN());

        getTagFilter().clear();
        if (model.tagFilters() != null) {
            model.tagFilters().forEach(filter -> {
                AutoScalingTagFilter tagFilter = newSubresource(AutoScalingTagFilter.class);
                tagFilter.copyFrom(filter);
                getTagFilter().add(tagFilter);
            });
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!configuredFields.contains("tag-filter") && !configuredFields.contains("cloud-formation-stack-arn")) {
            errors.add(new ValidationError(
                this,
                null,
                "Either 'tag-filter' or 'cloud-formation-stack-arn' is required."
            ));
        }

        return errors;
    }

    public ApplicationSource toApplicationSource() {
        return ApplicationSource.builder()
            .cloudFormationStackARN(getCloudFormationStackArn())
            .tagFilters(getTagFilter().stream()
                .map(AutoScalingTagFilter::toTagFilter)
                .collect(Collectors.toList()))
            .build();
    }
}
