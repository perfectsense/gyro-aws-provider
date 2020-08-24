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

package gyro.aws.efs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 *    file-system: $(external-query aws::file-system { id: 'fs-217883a3'})
 */
@Type("file-system")
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
        return client.describeFileSystems().fileSystems();
    }

    @Override
    protected List<FileSystemDescription> findAws(EfsClient client, Map<String, String> filters) {
        List<FileSystemDescription> fileSystems = new ArrayList<>();

        try {
            fileSystems = client.describeFileSystems(r -> r.fileSystemId(filters.get("id"))).fileSystems();

        } catch (FileSystemNotFoundException ex) {
            // ignore
        }

        return fileSystems;
    }
}
