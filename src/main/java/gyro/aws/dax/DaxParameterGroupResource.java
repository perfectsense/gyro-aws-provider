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

package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.DescribeParameterGroupsResponse;
import software.amazon.awssdk.services.dax.model.ParameterGroup;

/**
 * Creates a DAX parameter group with the specified Name and Description.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::dax-parameter-group parameter-group
 *         name: "kenny-parameter-group"
 *         description: "parameter-group-test-description"
 *     end
 */
@Type("dax-parameter-group")
public class DaxParameterGroupResource extends AwsResource implements Copyable<ParameterGroup> {

    private String description;
    private String name;
    private List<DaxParameterNameValue> parameterNameValues;

    /**
     * The description of the parameter group.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name of the parameter group.
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
     * The list of name-value pairs for the parameters of the group.
     */
    @Updatable
    public List<DaxParameterNameValue> getParameterNameValues() {
        if (parameterNameValues == null) {
            parameterNameValues = new ArrayList<>();
        }

        return parameterNameValues;
    }

    public void setParameterNameValues(List<DaxParameterNameValue> parameterNameValues) {
        this.parameterNameValues = parameterNameValues;
    }

    @Override
    public void copyFrom(ParameterGroup model) {
        setDescription(model.description());
        setName(model.parameterGroupName());
    }

    @Override
    public boolean refresh() {
        DaxClient client = createClient(DaxClient.class);
        DescribeParameterGroupsResponse response;

        response = client.describeParameterGroups(r -> r.parameterGroupNames(getName()));

        if (response == null || response.parameterGroups().isEmpty()) {
            return false;
        }

        copyFrom(response.parameterGroups().get(0));
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.createParameterGroup(r -> r
            .description(getDescription())
            .parameterGroupName(getName()));

        refresh();
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.updateParameterGroup(r -> r
            .parameterGroupName(getName())
            .parameterNameValues(getParameterNameValues().stream()
                .map(DaxParameterNameValue::toParameterNameValues)
                .collect(
                    Collectors.toList()))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.deleteParameterGroup(r -> r.parameterGroupName(getName()));
    }
}
