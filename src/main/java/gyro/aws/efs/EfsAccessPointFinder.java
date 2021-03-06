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
import software.amazon.awssdk.services.efs.model.AccessPointDescription;
import software.amazon.awssdk.services.efs.model.AccessPointNotFoundException;
import software.amazon.awssdk.services.efs.model.DescribeAccessPointsRequest;
import software.amazon.awssdk.services.efs.model.FileSystemNotFoundException;

/**
 * Query access point.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    access-point: $(external-query aws::efs-access-point { file-system-id: 'fs-217883a3'})
 */
@Type("efs-access-point")
public class EfsAccessPointFinder extends AwsFinder<EfsClient, AccessPointDescription, EfsAccessPointResource> {

    private String id;
    private String fileSystemId;

    /**
     * The ID of the access point.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The ID of the file system that the access point provides access to.
     */
    public String getFileSystemId() {
        return fileSystemId;
    }

    public void setFileSystemId(String fileSystemId) {
        this.fileSystemId = fileSystemId;
    }

    @Override
    protected List<AccessPointDescription> findAllAws(EfsClient client) {
        throw new IllegalArgumentException("Cannot query 'efs-access-point' without 'id' or 'file-system-id.");
    }

    @Override
    protected List<AccessPointDescription> findAws(EfsClient client, Map<String, String> filters) {
        DescribeAccessPointsRequest.Builder builder = DescribeAccessPointsRequest.builder();
        List<AccessPointDescription> accessPoints = new ArrayList<>();

        if ((!filters.containsKey("id") && !filters.containsKey("file-system-id")) ||
            (filters.containsKey("id") && filters.containsKey("file-system-id"))) {
            throw new IllegalArgumentException("Exactly one of 'id' or 'file-system-id' is required.");
        }

        if (filters.containsKey("id")) {
            builder = builder.accessPointId(filters.get("id"));
        }

        if (filters.containsKey("file-system-id")) {
            builder = builder.fileSystemId(filters.get("file-system-id"));
        }

        try {
            client.describeAccessPointsPaginator(builder.build()).forEach(r -> {
                if (r.hasAccessPoints()) {
                    accessPoints.addAll(r.accessPoints());
                }
            });

        } catch (AccessPointNotFoundException | FileSystemNotFoundException ex) {
            // ignore
        }

        return accessPoints;
    }
}
