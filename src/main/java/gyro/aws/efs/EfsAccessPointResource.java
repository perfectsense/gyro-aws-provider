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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.efs.EfsClient;
import software.amazon.awssdk.services.efs.model.AccessPointDescription;
import software.amazon.awssdk.services.efs.model.AccessPointNotFoundException;
import software.amazon.awssdk.services.efs.model.CreateAccessPointRequest;
import software.amazon.awssdk.services.efs.model.CreateAccessPointResponse;
import software.amazon.awssdk.services.efs.model.DescribeAccessPointsResponse;
import software.amazon.awssdk.services.efs.model.LifeCycleState;
import software.amazon.awssdk.services.efs.model.Tag;

/**
 * Creates an EFS Access Point.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::efs-access-point efs-access-point
 *         file-system: $(aws::efs-file-system efs-file-system)
 *
 *         posix-user
 *             user-id: 42950
 *             group-id: 42949
 *         end
 *
 *         tags: {
 *             "Name": "example-efs-access-point"
 *         }
 *     end
 */
@Type("efs-access-point")
public class EfsAccessPointResource extends AwsResource implements Copyable<AccessPointDescription> {

    private FileSystemResource fileSystem;
    private EfsPosixUser posixUser;
    private EfsRootDirectory rootDirectory;
    private Map<String, String> tags;

    // Output
    private String id;

    /**
     * The file system that the access point provides access to. (Required)
     */
    @Required
    public FileSystemResource getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(FileSystemResource fileSystem) {
        this.fileSystem = fileSystem;
    }

    /**
     * The operating system user and group applied to all file system requests made using the access point.
     *
     * @subresource gyro.aws.efs.EfsPosixUser
     */
    public EfsPosixUser getPosixUser() {
        return posixUser;
    }

    public void setPosixUser(EfsPosixUser posixUser) {
        this.posixUser = posixUser;
    }

    /**
     * The directory on the file system that the access point exposes as its root to NFS clients using the access point.
     *
     * @subresource gyro.aws.efs.EfsRootDirectory
     */
    public EfsRootDirectory getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(EfsRootDirectory rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    /**
     * The tags for the access point.
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
     * The ID of the access point.
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
    public void copyFrom(AccessPointDescription model) {
        setId(model.accessPointId());
        setFileSystem(findById(FileSystemResource.class, model.fileSystemId()));

        if (model.posixUser() != null) {
            EfsPosixUser efsPosixUser = newSubresource(EfsPosixUser.class);
            efsPosixUser.copyFrom(model.posixUser());
            setPosixUser(efsPosixUser);
        }

        if (model.rootDirectory() != null) {
            EfsRootDirectory efsRootDirectory = newSubresource(EfsRootDirectory.class);
            efsRootDirectory.copyFrom(model.rootDirectory());
            setRootDirectory(efsRootDirectory);
        }

        getTags().clear();
        if (model.hasTags()) {
            for (Tag tag : model.tags()) {
                getTags().put(tag.key(), tag.value());
            }
        }
    }

    @Override
    public boolean refresh() {
        EfsClient client = createClient(EfsClient.class);

        AccessPointDescription accessPoint = getAccessPoint(client);

        if (accessPoint == null) {
            return false;
        }

        copyFrom(accessPoint);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EfsClient client = createClient(EfsClient.class);

        CreateAccessPointRequest.Builder builder = CreateAccessPointRequest.builder()
            .fileSystemId(getFileSystem().getId())
            .tags(getTags().entrySet()
                .stream()
                .map(t -> Tag.builder().key(t.getKey()).value(t.getValue()).build())
                .collect(Collectors.toList()));

        if (getPosixUser() != null) {
            builder = builder.posixUser(getPosixUser().toPosixUser());
        }

        if (getRootDirectory() != null) {
            builder = builder.rootDirectory(getRootDirectory().toRootDirectory());
        }

        CreateAccessPointResponse accessPoint = client.createAccessPoint(builder.build());

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> {
                AccessPointDescription accessPointDescription = getAccessPoint(client);
                return (accessPointDescription != null && accessPointDescription.lifeCycleState()
                    .equals(LifeCycleState.AVAILABLE));
            });

        setId(accessPoint.accessPointId());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EfsClient client = createClient(EfsClient.class);
        EfsAccessPointResource currentResource = (EfsAccessPointResource) current;

        if (!currentResource.getTags().isEmpty()) {
            client.untagResource(r -> r.resourceId(getId())
                .tagKeys(currentResource.getTags().keySet()));
        }

        client.tagResource(r -> r.resourceId(getId()).tags(getTags().entrySet().stream()
            .map(t -> Tag.builder().key(t.getKey()).value(t.getValue()).build()).collect(Collectors.toList())));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EfsClient client = createClient(EfsClient.class);

        client.deleteAccessPoint(r -> r.accessPointId(getId()));

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> {
                AccessPointDescription accessPoint = getAccessPoint(client);
                return (accessPoint == null || accessPoint.lifeCycleState().equals(LifeCycleState.DELETED));
            });
    }

    private AccessPointDescription getAccessPoint(EfsClient client) {
        AccessPointDescription accessPoint = null;

        try {
            DescribeAccessPointsResponse response = client.describeAccessPoints(r -> r.accessPointId(getId()));

            if (response.hasAccessPoints()) {
                List<AccessPointDescription> accessPointDescriptions = response.accessPoints();

                if (!accessPointDescriptions.isEmpty()) {
                    accessPoint = accessPointDescriptions.get(0);
                }
            }

        } catch (AccessPointNotFoundException ex) {
            // ignore
        }

        return accessPoint;
    }
}
