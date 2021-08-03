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

package gyro.aws.efs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.efs.EfsClient;
import software.amazon.awssdk.services.efs.model.CreateMountTargetResponse;
import software.amazon.awssdk.services.efs.model.EfsException;
import software.amazon.awssdk.services.efs.model.LifeCycleState;
import software.amazon.awssdk.services.efs.model.MountTargetDescription;

/**
 * Creates an EFS Mount Target.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::efs-mount-target efs-mount-target
 *         file-system: $(aws::efs-file-system efs-file-system)
 *         ip-address: "10.0.0.16"
 *         subnet: $(aws::subnet subnet-efs)
 *
 *         security-groups: [
 *             $(aws::security-group security-group-efs)
 *         ]
 *     end
 */
@Type("efs-mount-target")
public class EfsMountTargetResource extends AwsResource implements Copyable<MountTargetDescription> {

    private FileSystemResource fileSystem;
    private String ipAddress;
    private SubnetResource subnet;
    private List<SecurityGroupResource> securityGroups;

    // Output
    private String id;

    /**
     * The file system for which to attach a mount target.
     */
    @Required
    public FileSystemResource getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(FileSystemResource fileSystem) {
        this.fileSystem = fileSystem;
    }

    /**
     * The valid IPv4 address within the address range of the specified subnet.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * The subnet to add the mount target in.
     */
    @Required
    public SubnetResource getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetResource subnet) {
        this.subnet = subnet;
    }

    /**
     * The security groups currently in effect for a mount target.
     */
    @CollectionMax(5)
    @Updatable
    @Required
    public List<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new ArrayList<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * The ID of the mount target.
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
    public void copyFrom(MountTargetDescription model) {
        setFileSystem(findById(FileSystemResource.class, model.fileSystemId()));
        setIpAddress(model.ipAddress());
        setSubnet(findById(SubnetResource.class, model.subnetId()));
        setId(model.mountTargetId());

        EfsClient client = createClient(EfsClient.class);
        setSecurityGroups(client.describeMountTargetSecurityGroups(r -> r.mountTargetId(getId()))
            .securityGroups()
            .stream()
            .map(s -> findById(SecurityGroupResource.class, s))
            .collect(Collectors.toList()));
    }

    @Override
    public boolean refresh() {
        EfsClient client = createClient(EfsClient.class);

        MountTargetDescription mountTarget = getMountTarget(client);

        if (mountTarget == null) {
            return false;
        }

        copyFrom(mountTarget);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EfsClient client = createClient(EfsClient.class);

        CreateMountTargetResponse mountTarget = client.createMountTarget(r -> r.fileSystemId(getFileSystem().getId())
            .ipAddress(getIpAddress())
            .subnetId(getSubnet().getId())
            .securityGroups(getSecurityGroups().stream()
                .map(SecurityGroupResource::getId)
                .collect(Collectors.toList())));

        setId(mountTarget.mountTargetId());

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .prompt(false)
            .until(() -> {
                MountTargetDescription mountTargetDescription = getMountTarget(client);
                return (mountTargetDescription != null && mountTargetDescription.lifeCycleState()
                    .equals(LifeCycleState.AVAILABLE));
            });

        setId(mountTarget.mountTargetId());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EfsClient client = createClient(EfsClient.class);

        client.modifyMountTargetSecurityGroups(r -> r.mountTargetId(getId())
            .securityGroups(getSecurityGroups().stream()
                .map(SecurityGroupResource::getId)
                .collect(Collectors.toList())));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EfsClient client = createClient(EfsClient.class);

        client.deleteMountTarget(r -> r.mountTargetId(getId()));

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .prompt(false)
            .until(() -> {
                MountTargetDescription mountTargetDescription = getMountTarget(client);
                return (mountTargetDescription == null || mountTargetDescription.lifeCycleState()
                    .equals(LifeCycleState.DELETED));
            });
    }

    private MountTargetDescription getMountTarget(EfsClient client) {
        MountTargetDescription mountTarget = null;

        try {
            List<MountTargetDescription> mountTargets = client.describeMountTargets(r -> r.mountTargetId(getId()))
                .mountTargets();
            if (!mountTargets.isEmpty()) {
                mountTarget = mountTargets.get(0);
            }

        } catch (EfsException ex) {
            if (!ex.awsErrorDetails().errorCode().equals("MountTargetNotFound")) {
                throw ex;
            }
        }

        return mountTarget;
    }
}
