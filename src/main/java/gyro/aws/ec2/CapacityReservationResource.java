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

package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CapacityReservation;
import software.amazon.awssdk.services.ec2.model.CreateCapacityReservationResponse;
import software.amazon.awssdk.services.ec2.model.DescribeCapacityReservationsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Creates a EC2 Capacity Reservation.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::ec2-capacity-reservation capacity-reservation-example
 *         availability-zone: "us-west-2a"
 *         ebs-optimized: false
 *         end-date-type: "unlimited"
 *         ephemeral-storage: false
 *         instance-count: 2
 *         instance-match-criteria: "targeted"
 *         instance-platform: "Linux/UNIX"
 *         instance-type: "t2.micro"
 *         tenancy: "default"
 *     end
 */
@Type("ec2-capacity-reservation")
public class CapacityReservationResource extends Ec2TaggableResource<CapacityReservation> implements Copyable<CapacityReservation> {

    private String id;
    private String availabilityZone;
    private Boolean ebsOptimized;
    private Date endDate;
    private String endDateType;
    private Boolean ephemeralStorage;
    private String instanceMatchCriteria;
    private String instancePlatform;
    private String instanceType;
    private String tenancy;
    private Integer instanceCount;
    private Integer availableInstanceCount;
    private Date createDate;

    /**
     * The id of the capacity reservation.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The Availability Zone in which to create the Capacity Reservation.
     */
    @Required
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * Indicates whether the Capacity Reservation supports EBS-optimized instances.
     */
    @Required
    public Boolean getEbsOptimized() {
        return ebsOptimized;
    }

    public void setEbsOptimized(Boolean ebsOptimized) {
        this.ebsOptimized = ebsOptimized;
    }

    /**
     * The date and time at which the Capacity Reservation expires. Required if ``end-date-type`` set to ``limited``.
     */
    @Updatable
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Indicates the way in which the Capacity Reservation ends. Valid values are ``unlimited`` or ``limited``.
     */
    @Required
    @Updatable
    @ValidStrings({"unlimited", "limited"})
    public String getEndDateType() {
        return endDateType != null ? endDateType.toLowerCase() : null;
    }

    public void setEndDateType(String endDateType) {
        this.endDateType = endDateType;
    }

    /**
     * Indicates whether the Capacity Reservation supports instances with temporary, block-level storage.
     */
    @Required
    public Boolean getEphemeralStorage() {
        return ephemeralStorage;
    }

    public void setEphemeralStorage(Boolean ephemeralStorage) {
        this.ephemeralStorage = ephemeralStorage;
    }

    /**
     * Indicates the type of instance launches that the Capacity Reservation accepts. Valid values are ``open`` or ``targeted``.
     */
    @Required
    @ValidStrings({"open", "targeted"})
    public String getInstanceMatchCriteria() {
        return instanceMatchCriteria != null ? instanceMatchCriteria.toLowerCase() : null;
    }

    public void setInstanceMatchCriteria(String instanceMatchCriteria) {
        this.instanceMatchCriteria = instanceMatchCriteria;
    }

    /**
     * The type of operating system for which to reserve capacity.
     */
    @Required
    public String getInstancePlatform() {
        return instancePlatform;
    }

    public void setInstancePlatform(String instancePlatform) {
        this.instancePlatform = instancePlatform;
    }

    /**
     * The instance type for which to reserve capacity.
     */
    @Required
    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * Indicates the tenancy of the Capacity Reservation. Valid values are ``default`` or ``dedicated``.
     */
    @Required
    @ValidStrings({"default", "dedicated"})
    public String getTenancy() {
        return tenancy != null ? tenancy.toLowerCase() : null;
    }

    public void setTenancy(String tenancy) {
        this.tenancy = tenancy;
    }

    /**
     * The number of Instances for which to reserve capacity.
     */
    @Required
    @Updatable
    public Integer getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
    }

    /**
     * The count of available instances.
     */
    @Output
    public Integer getAvailableInstanceCount() {
        return availableInstanceCount;
    }

    public void setAvailableInstanceCount(Integer availableInstanceCount) {
        this.availableInstanceCount = availableInstanceCount;
    }

    /**
     * The Capacity Reservation creation date.
     */
    @Output
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(CapacityReservation capacityReservation) {
        setId(capacityReservation.capacityReservationId());
        setAvailabilityZone(capacityReservation.availabilityZone());
        setEbsOptimized(capacityReservation.ebsOptimized());
        setEndDate(capacityReservation.endDate() != null ? Date.from(capacityReservation.endDate()) : null);
        setEndDateType(capacityReservation.endDateTypeAsString());
        setEphemeralStorage(capacityReservation.ephemeralStorage());
        setInstanceMatchCriteria(capacityReservation.instanceMatchCriteriaAsString());
        setInstancePlatform(capacityReservation.instancePlatformAsString());
        setInstanceType(capacityReservation.instanceType());
        setTenancy(capacityReservation.tenancyAsString());
        setAvailableInstanceCount(capacityReservation.availableInstanceCount());
        setCreateDate(capacityReservation.createDate() != null ? Date.from(capacityReservation.createDate()) : null);
        setInstanceCount(capacityReservation.totalInstanceCount());

        refreshTags();
    }

    @Override
    public boolean doRefresh() {

        Ec2Client client = createClient(Ec2Client.class);

        CapacityReservation capacityReservation = getCapacityReservation(client);

        if (capacityReservation == null) {
            return false;
        }

        copyFrom(capacityReservation);

        return true;
    }

    @Override
    public void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        validate();

        CreateCapacityReservationResponse response = client.createCapacityReservation(
            r -> r.availabilityZone(getAvailabilityZone())
                .ebsOptimized(getEbsOptimized())
                .endDate(getEndDate() != null ? getEndDate().toInstant() : null)
                .endDateType(getEndDateType())
                .ephemeralStorage(getEphemeralStorage())
                .instanceCount(getInstanceCount())
                .instanceMatchCriteria(getInstanceMatchCriteria())
                .instancePlatform(getInstancePlatform())
                .instanceType(getInstanceType())
                .tenancy(getTenancy())
        );

        CapacityReservation capacityReservation = response.capacityReservation();

        setId(capacityReservation.capacityReservationId());
        setAvailableInstanceCount(capacityReservation.availableInstanceCount());
        setCreateDate(capacityReservation.createDate() != null ? Date.from(capacityReservation.createDate()) : null);

    }

    @Override
    public void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        validate();

        client.modifyCapacityReservation(
            r -> r.capacityReservationId(getId())
                .endDate(getEndDate() != null ? getEndDate().toInstant() : null)
                .endDateType(getEndDateType())
                .instanceCount(getInstanceCount())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.cancelCapacityReservation(
            r -> r.capacityReservationId(getId())
        );
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getEndDateType() == null || (!getEndDateType().equals("unlimited") && !getEndDateType().equals("limited"))) {
            errors.add(new ValidationError(this, null, "The value - (" + getEndDateType() + ") is invalid for parameter 'end-date-type'."
                + "Valid values [ 'unlimited', 'limited' ]"));
        }

        if (getEndDateType().equals("unlimited") && !ObjectUtils.isBlank(getEndDate())) {
            errors.add(new ValidationError(this, null, "The value - (" + getEndDate() + ") is invalid for parameter 'end-date' "
                + "when param 'end-date-type' is set to 'unlimited'."));
        }

        if (getEndDateType().equals("limited") && ObjectUtils.isBlank(getEndDate())) {
            errors.add(new ValidationError(this, null, "The value - (" + getEndDate() + ") is mandatory for parameter 'end-date' "
                + "when param 'end-date-type' is set to 'limited'."));
        }

        if (getInstanceMatchCriteria() == null || (!getInstanceMatchCriteria().equals("open") && !getInstanceMatchCriteria().equals("targeted"))) {
            errors.add(new ValidationError(this, null, "The value - (" + getInstanceMatchCriteria() + ") is invalid for parameter 'instance-match-criteria'."
                + "Valid values [ 'open', 'targeted' ]"));
        }

        if (getTenancy() == null || (!getTenancy().equals("default") && !getTenancy().equals("dedicated"))) {
            errors.add(new ValidationError(this, null, "The value - (" + getTenancy() + ") is invalid for parameter 'tenancy'."
                + "Valid values [ 'default', 'dedicated' ]"));
        }

        return errors;
    }

    private CapacityReservation getCapacityReservation(Ec2Client client) {
        CapacityReservation capacityReservation = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load capacity reservation.");
        }

        try {
            DescribeCapacityReservationsResponse response = client.describeCapacityReservations(
                r -> r.capacityReservationIds(Collections.singleton(getId()))
            );

            if (!response.capacityReservations().isEmpty()) {
                capacityReservation = response.capacityReservations().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return capacityReservation;
    }
}
