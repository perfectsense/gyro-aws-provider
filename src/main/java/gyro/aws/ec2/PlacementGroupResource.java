/*
 * Copyright 2020, Perfect Sense, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribePlacementGroupsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.PlacementGroup;
import software.amazon.awssdk.services.ec2.model.PlacementStrategy;

/**
 * Creates a Placement Group with the specified name and placement strategy.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::placement-group example-placement-group
 *         name: "TestGroup"
 *         placement-strategy: "partition"
 *         partition-count: 3
 *     end
 */
@Type("placement-group")
public class PlacementGroupResource extends Ec2TaggableResource<PlacementGroup> implements Copyable<PlacementGroup> {

    private String name;
    private PlacementStrategy placementStrategy;
    private Integer partitionCount;

    // Read-only
    private String id;

    /**
     * The name of the Placement Group.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Approaches towards managing the placement of instances on the underlying hardware.
     * Defaults to the``cluster`` strategy.
     */
    @Required
    @ValidStrings({"cluster", "spread", "partition"})
    public PlacementStrategy getPlacementStrategy() {
        return placementStrategy;
    }

    public void setPlacementStrategy(PlacementStrategy placementStrategy) {
        this.placementStrategy = placementStrategy;
    }

    /**
     * The number of partitions comprising the Placement Group. Only required when strategy is set to ``partition``.
     */
    @Range(min= 1, max= 7)
    public Integer getPartitionCount() {
        return partitionCount;
    }

    public void setPartitionCount(Integer partitionCount) {
        this.partitionCount = partitionCount;
    }

    /**
     * The ID of the Placement Group.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(PlacementGroup model) {
        setId(model.groupId());
        setName(model.groupName());
        setPlacementStrategy(model.strategy());
        setPartitionCount(model.partitionCount());
        refreshTags();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load placement group.");
        }

        PlacementGroup group = getPlacementGroup(client);

        if (group == null) {
            return false;
        }

        copyFrom(group);
        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.createPlacementGroup(r -> r.groupName(getName())
                .strategy(getPlacementStrategy())
                .partitionCount(getPartitionCount()));

        PlacementGroup group = getPlacementGroup(client);

        if (group != null) {
            setId(group.groupId());
        }

    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);
        client.deletePlacementGroup(r -> r.groupName(getName()));
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if ((getPlacementStrategy() == PlacementStrategy.PARTITION) && (getPartitionCount() == null)) {
            ValidationError error = new ValidationError(this, "partition-count", "partition-count is required when strategy is set to 'partition'");
            errors.add(error);

        } else if ((getPlacementStrategy() != PlacementStrategy.PARTITION) && (getPartitionCount() != null)) {
            ValidationError error = new ValidationError(this, "partition-count", "partition-count must not be set when strategy is not set to 'partition'");
            errors.add(error);
        }

        return errors;
    }

    private PlacementGroup getPlacementGroup(Ec2Client client) {
        PlacementGroup group = null;
        try {
            DescribePlacementGroupsResponse describeResponse = client.describePlacementGroups(r -> r.groupNames(getName()));
            if (!describeResponse.placementGroups().isEmpty()) {
                group = describeResponse.placementGroups().get(0);
            }

        } catch (Ec2Exception ex) {
            if (ex.awsErrorDetails() == null || !ex.awsErrorDetails().errorCode().equals("InvalidPlacementGroup.Unknown")) {
                throw ex;
            }
        }

        return group;
    }
}
