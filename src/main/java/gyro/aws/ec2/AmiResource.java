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

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateImageRequest;
import software.amazon.awssdk.services.ec2.model.CreateImageResponse;
import software.amazon.awssdk.services.ec2.model.DescribeImageAttributeResponse;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSnapshotsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.ImageAttributeName;
import software.amazon.awssdk.services.ec2.model.ImageState;
import software.amazon.awssdk.services.ec2.model.OperationType;
import software.amazon.awssdk.services.ec2.model.PermissionGroup;
import software.amazon.awssdk.services.ec2.model.ProductCode;
import software.amazon.awssdk.services.ec2.model.Snapshot;
import software.amazon.awssdk.utils.builder.SdkBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Creates an AMI with the specified instance.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::ami ami-example
 *         name: "ami-example"
 *         description: "ami-example"
 *         instance: $(aws::instance instance-example-source)
 *
 *         launch-permission
 *             user-id: "AWS Account ID"
 *         end
 *
 *         tags: {
 *             Name: "ami-example"
 *         }
 *     end
 */
@Type("ami")
public class AmiResource extends Ec2TaggableResource<Image> implements Copyable<Image> {
    private String name;
    private String description;
    private InstanceResource instance;
    private Boolean noReboot;
    private Set<BlockDeviceMappingResource> blockDeviceMapping;
    private Set<AmiLaunchPermission> launchPermission;
    private Set<String> productCodes;
    private Boolean publicLaunchPermission;

    private String id;

    private static final String ATTRIBUTE_DESCRIPTION = "description";
    private static final String ATTRIBUTE_PRODUCT_CODE = "productCodes";
    private static final String ATTRIBUTE_LAUNCH_PERMISSION = "launchPermission";

    /**
     * The name of the AMI. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the AMI.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The Instance from which the AMI is going to be created. (Required)
     */
    @Required
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
    }

    /**
     * Specify if the Instance is rebooted when creating the image or not.
     */
    public Boolean getNoReboot() {
        if (noReboot == null) {
            noReboot = false;
        }

        return noReboot;
    }

    public void setNoReboot(Boolean noReboot) {
        this.noReboot = noReboot;
    }

    /**
     * A set of Block Device Mappings to be associated with the instances created by this AMI.
     */
    public Set<BlockDeviceMappingResource> getBlockDeviceMapping() {
        if (blockDeviceMapping == null) {
            blockDeviceMapping = new HashSet<>();
        }

        return blockDeviceMapping;
    }

    public void setBlockDeviceMapping(Set<BlockDeviceMappingResource> blockDeviceMapping) {
        this.blockDeviceMapping = blockDeviceMapping;
    }

    /**
     * A set of Launch Permission for the AMI. Cannot be set if ``public-launch-permission`` is set to ``true``.
     */
    @Updatable
    public Set<AmiLaunchPermission> getLaunchPermission() {
        if (launchPermission == null) {
            launchPermission = new HashSet<>();
        }

        return launchPermission;
    }

    public void setLaunchPermission(Set<AmiLaunchPermission> launchPermission) {
        this.launchPermission = launchPermission;
    }

    /**
     * A set of Product Codes for the AMI. Added Product Codes can't be removed.
     */
    @Updatable
    public Set<String> getProductCodes() {
        if (productCodes == null) {
            productCodes = new HashSet<>();
        }

        return productCodes;
    }

    public void setProductCodes(Set<String> productCodes) {
        this.productCodes = productCodes;
    }

    /**
     * Make the the AMI publicly available for launch. Defaults to ``false``.
     */
    @Updatable
    public Boolean getPublicLaunchPermission() {
        if (publicLaunchPermission == null) {
            publicLaunchPermission = false;
        }

        return publicLaunchPermission;
    }

    public void setPublicLaunchPermission(Boolean publicLaunchPermission) {
        this.publicLaunchPermission = publicLaunchPermission;
    }

    /**
     * The ID of the AMI.
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
    public void copyFrom(Image image) {
        setName(image.name());
        setDescription(image.description());
        setId(image.imageId());
        setPublicLaunchPermission(image.publicLaunchPermissions());

        Ec2Client client = createClient(Ec2Client.class);

        try {
            DescribeImageAttributeResponse response = client.describeImageAttribute(r -> r.imageId(getId()).attribute(ImageAttributeName.LAUNCH_PERMISSION));

            setLaunchPermission(response.launchPermissions().stream().filter(o -> o.userId() != null).map(o -> {
                AmiLaunchPermission launchPermission = newSubresource(AmiLaunchPermission.class);
                launchPermission.copyFrom(o);
                return launchPermission;
            }).collect(Collectors.toSet()));

            response = client.describeImageAttribute(r -> r.imageId(getId()).attribute(ImageAttributeName.PRODUCT_CODES));

            setProductCodes(response.productCodes().stream().map(ProductCode::productCodeId).collect(Collectors.toSet()));
        } catch (Ec2Exception ex) {
            if (!ex.awsErrorDetails().errorCode().equals("AuthFailure")) {
                throw ex;
            }
        }

        refreshTags();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        Image image = getImage(client);

        if (image == null) {
            return false;
        }

        copyFrom(image);

        return true;
    }

    @Override
    public void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateImageRequest.Builder builder = CreateImageRequest.builder();

        builder = builder.name(getName())
            .description(getDescription())
            .instanceId(getInstance().getId())
            .noReboot(getNoReboot());

        if (getBlockDeviceMapping().isEmpty()) {
            builder = builder.blockDeviceMappings(SdkBuilder::build);
        } else {
            builder = builder.blockDeviceMappings(getBlockDeviceMapping().stream().map(BlockDeviceMappingResource::getBlockDeviceMapping).collect(Collectors.toSet()));
        }

        CreateImageResponse response = client.createImage(builder.build());

        setId(response.imageId());

        state.save();

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .until(() -> getImage(client).state().equals(ImageState.AVAILABLE));

        if (getPublicLaunchPermission()) {
            client.modifyImageAttribute(
                r -> r.imageId(getId())
                    .attribute(ATTRIBUTE_LAUNCH_PERMISSION).launchPermission(
                        lp -> lp.add(p -> p.group(PermissionGroup.ALL).build()))
            );
        } else if (!getLaunchPermission().isEmpty()) {
            client.modifyImageAttribute(
                r -> r.imageId(getId())
                    .attribute(ATTRIBUTE_LAUNCH_PERMISSION).launchPermission(
                    lp -> lp.add(getLaunchPermission().stream()
                        .map(AmiLaunchPermission::toLaunchPermission)
                        .collect(Collectors.toList()))
                ));
        }

        state.save();

        if (!getProductCodes().isEmpty()) {
            client.modifyImageAttribute(
                r -> r.imageId(getId())
                    .attribute(ATTRIBUTE_PRODUCT_CODE)
                    .productCodes(getProductCodes())
            );
        }
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        if (changedProperties.contains("description")) {
            client.modifyImageAttribute(
                r -> r.imageId(getId())
                    .attribute(ATTRIBUTE_DESCRIPTION)
                    .value(getDescription())
            );
        }

        if (changedProperties.contains("public-launch-permission")) {
            if (getPublicLaunchPermission()) {
                client.modifyImageAttribute(
                    r -> r.imageId(getId())
                        .attribute(ATTRIBUTE_LAUNCH_PERMISSION).launchPermission(
                            lp -> lp.add(p -> p.group(PermissionGroup.ALL).build()))
                );
            } else {
                client.modifyImageAttribute(
                    r -> r.imageId(getId())
                        .attribute(ATTRIBUTE_LAUNCH_PERMISSION).launchPermission(
                            lp -> lp.remove(p -> p.group(PermissionGroup.ALL).build()))
                );
            }
        }

        if (changedProperties.contains("launch-permission")) {
            if (!getLaunchPermission().isEmpty()) {
                client.modifyImageAttribute(
                    r -> r.imageId(getId())
                        .operationType(OperationType.ADD)
                        .attribute(ATTRIBUTE_LAUNCH_PERMISSION).launchPermission(
                            lp -> lp.add(getLaunchPermission().stream()
                                .map(AmiLaunchPermission::toLaunchPermission)
                                .collect(Collectors.toList()))
                        ));
            }

            AmiResource oldAmiResource = (AmiResource) config;

            if (!oldAmiResource.getLaunchPermission().isEmpty()) {
                client.modifyImageAttribute(
                    r -> r.imageId(getId())
                        .operationType(OperationType.REMOVE)
                        .attribute(ATTRIBUTE_LAUNCH_PERMISSION).launchPermission(
                            lp -> lp.remove(oldAmiResource.getLaunchPermission().stream()
                                .map(AmiLaunchPermission::toLaunchPermission)
                                .collect(Collectors.toList()))
                        ));
            }
        }

        if (changedProperties.contains("product-codes")) {
            client.modifyImageAttribute(
                r -> r.imageId(getId())
                    .attribute(ATTRIBUTE_PRODUCT_CODE)
                    .productCodes(getProductCodes())
            );
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deregisterImage(r -> r.imageId(getId()));

        getSnapshots(client).forEach(o -> {
            client.deleteSnapshot(r -> r.snapshotId(o.snapshotId()));
        });
    }

    private Image getImage(Ec2Client client) {
        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("Id is missing, unable to load AMI.");
        }

        try {
            DescribeImagesResponse response = client.describeImages(r -> r.imageIds(getId()));

            if (!response.images().isEmpty()) {
                return response.images().get(0);
            }
        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return null;
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getPublicLaunchPermission() && !getLaunchPermission().isEmpty()) {
            errors.add(new ValidationError(this, "launch-permission", "When 'public-launch-permission' is set to 'true', 'launch-permission' cannot be set."));
        }

        return errors;
    }

    private List<Snapshot> getSnapshots(Ec2Client client) {
        DescribeSnapshotsResponse response = client.describeSnapshots(
            r -> r.filters(Collections.singleton(
                Filter.builder()
                    .name("description")
                    .values(String.format("Created by CreateImage(*) for %s from *", getId()))
                    .build()
            )));

        return response.snapshots();
    }
}
