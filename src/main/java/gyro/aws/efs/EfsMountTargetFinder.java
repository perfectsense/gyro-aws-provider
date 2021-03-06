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
import java.util.Map;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.efs.EfsClient;
import software.amazon.awssdk.services.efs.model.DescribeMountTargetsRequest;
import software.amazon.awssdk.services.efs.model.MountTargetDescription;
import software.amazon.awssdk.services.efs.model.MountTargetNotFoundException;

/**
 * Query point target.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    mount-target: $(external-query aws::efs-mount-target { file-system-id: 'fs-217883a3'})
 */
@Type("efs-mount-target")
public class EfsMountTargetFinder
    extends AwsFinder<EfsClient, MountTargetDescription, EfsMountTargetResource> {

    private String id;
    private String fileSystemId;

    /**
     * The ID of the mount target.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The ID of the file system to which the mount target is attached.
     */
    public String getFileSystemId() {
        return fileSystemId;
    }

    public void setFileSystemId(String fileSystemId) {
        this.fileSystemId = fileSystemId;
    }

    @Override
    protected List<MountTargetDescription> findAllAws(EfsClient client) {
        throw new IllegalArgumentException("Either 'id' or 'file-system-id' is required.");
    }

    @Override
    protected List<MountTargetDescription> findAws(
        EfsClient client, Map<String, String> filters) {
        DescribeMountTargetsRequest.Builder builder = DescribeMountTargetsRequest.builder();
        List<MountTargetDescription> mountTargets = new ArrayList<>();

        if ((!filters.containsKey("id") && !filters.containsKey("file-system-id")) ||
            (filters.containsKey("id") && filters.containsKey("file-system-id"))) {
            throw new IllegalArgumentException("Either 'id' or 'file-system-id' is required.");
        }

        if (filters.containsKey("id")) {
            builder = builder.mountTargetId(filters.get("id"));
        }

        if (filters.containsKey("file-system-id")) {
            builder = builder.fileSystemId(filters.get("file-system-id"));
        }

        try {
            mountTargets = client.describeMountTargets(builder.build()).mountTargets();

        } catch (MountTargetNotFoundException ex) {
            // ignore
        }

        return mountTargets;
    }
}
