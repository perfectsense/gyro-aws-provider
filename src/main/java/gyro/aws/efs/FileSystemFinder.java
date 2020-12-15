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
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.efs.EfsClient;
import software.amazon.awssdk.services.efs.model.FileSystemDescription;
import software.amazon.awssdk.services.efs.model.FileSystemNotFoundException;

/**
 * Query file system.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    file-system: $(external-query aws::efs-file-system { id: 'fs-217883a3'})
 */
@Type("efs-file-system")
public class FileSystemFinder extends AwsFinder<EfsClient, FileSystemDescription, FileSystemResource> {

    /**
     * The ID of the file system.
     */
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<FileSystemDescription> findAllAws(EfsClient client) {
        return client.describeFileSystemsPaginator().stream()
            .flatMap(r -> r.fileSystems().stream()).collect(Collectors.toList());
    }

    @Override
    protected List<FileSystemDescription> findAws(EfsClient client, Map<String, String> filters) {
        List<FileSystemDescription> fileSystems = new ArrayList<>();

        try {
            client.describeFileSystemsPaginator(r -> r.fileSystemId(filters.get("id"))).forEach(f -> {
                if (f.hasFileSystems()) {
                    fileSystems.addAll(f.fileSystems());
                }
            });

        } catch (FileSystemNotFoundException ex) {
            // ignore
        }

        return fileSystems;
    }
}
