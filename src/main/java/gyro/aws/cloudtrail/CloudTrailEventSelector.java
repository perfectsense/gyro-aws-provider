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

package gyro.aws.cloudtrail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.cloudtrail.model.EventSelector;
import software.amazon.awssdk.services.cloudtrail.model.ReadWriteType;

public class CloudTrailEventSelector extends Diffable implements Copyable<EventSelector> {

    private List<CloudTrailDataResource> dataResource;
    private List<String> managementEventSourcesToExclude;
    private Boolean includeManagementEvents;
    private ReadWriteType readWriteType;

    /**
     * The Amazon S3 buckets or AWS Lambda functions that you specify in your event selectors for your trail to log data events. (Required)
     */
    @Required
    public List<CloudTrailDataResource> getDataResource() {
        if (dataResource == null) {
            dataResource = new ArrayList<>();
        }

        return dataResource;
    }

    public void setDataResource(List<CloudTrailDataResource> dataResource) {
        this.dataResource = dataResource;
    }

    /**
     * The list of service event sources from which management events should not be logged.
     */
    public List<String> getManagementEventSourcesToExclude() {
        if (managementEventSourcesToExclude == null) {
            managementEventSourcesToExclude = new ArrayList<>();
        }

        return managementEventSourcesToExclude;
    }

    public void setManagementEventSourcesToExclude(List<String> managementEventSourcesToExclude) {
        this.managementEventSourcesToExclude = managementEventSourcesToExclude;
    }

    /**
     * Option to specify if the event selector should include management events.
     */
    @Updatable
    public Boolean getIncludeManagementEvents() {
        return includeManagementEvents;
    }

    public void setIncludeManagementEvents(Boolean includeManagementEvents) {
        this.includeManagementEvents = includeManagementEvents;
    }

    /**
     * The type of events to be logged by the trail. (Required)
     */
    @Updatable
    public ReadWriteType getReadWriteType() {
        return readWriteType;
    }

    public void setReadWriteType(ReadWriteType readWriteType) {
        this.readWriteType = readWriteType;
    }

    @Override
    public void copyFrom(EventSelector model) {
        setManagementEventSourcesToExclude(model.excludeManagementEventSources());
        setIncludeManagementEvents(model.includeManagementEvents());
        setReadWriteType(model.readWriteType());
        setDataResource(model.dataResources().stream().map(r -> {
            CloudTrailDataResource cloudTrailDataResource = newSubresource(CloudTrailDataResource.class);
            cloudTrailDataResource.copyFrom(r);
            return cloudTrailDataResource;
        }).collect(Collectors.toList()));
    }

    @Override
    public String primaryKey() {
        return String.format("Data Resource: %s, Excluded Sources: %s", getDataResource().toString(), getManagementEventSourcesToExclude().toString());
    }

    EventSelector toEventSelector() {
        return EventSelector.builder().dataResources(getDataResource()
                .stream()
                .map(CloudTrailDataResource::toDataResource)
                .collect(Collectors.toList()))
                .excludeManagementEventSources(getManagementEventSourcesToExclude())
                .includeManagementEvents(getIncludeManagementEvents())
                .readWriteType(getReadWriteType()).build();
    }
}
