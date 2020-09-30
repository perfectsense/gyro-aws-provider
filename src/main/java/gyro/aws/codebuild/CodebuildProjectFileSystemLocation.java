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

package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.ProjectFileSystemLocation;

public class CodebuildProjectFileSystemLocation extends Diffable implements Copyable<ProjectFileSystemLocation> {

    private String identifier;
    private String location;
    private String mountOptions;
    private String mountPoint;
    private String type;

    /**
     * The name used to access a file system created by Amazon EFS.
     */
    @Updatable
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The location of the file system created by Amazon EFS.
     */
    @Updatable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * The mount options for a file system created by Amazon EFS.
     */
    @Updatable
    public String getMountOptions() {
        return mountOptions;
    }

    public void setMountOptions(String mountOptions) {
        this.mountOptions = mountOptions;
    }

    /**
     * The location in the container where the file system is mounted.
     */
    @Updatable
    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    /**
     * The type of the file system.
     */
    @Updatable
    @ValidStrings("EFS")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(ProjectFileSystemLocation model) {
        setIdentifier(model.identifier());
        setLocation(model.location());
        setMountOptions(model.mountOptions());
        setMountPoint(model.mountPoint());
        setType(model.typeAsString());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public ProjectFileSystemLocation toProjectFileSystemLocation() {
        return ProjectFileSystemLocation.builder()
            .identifier(getIdentifier())
            .location(getLocation())
            .mountOptions(getMountOptions())
            .mountPoint(getMountPoint())
            .type(getType())
            .build();
    }
}
