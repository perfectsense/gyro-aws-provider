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

package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.ProjectCache;

public class CodebuildProjectCache extends Diffable implements Copyable<ProjectCache> {

    private String type;
    private String location;
    private List<String> modes;

    /**
     * The type of cache used by the build project.
     */
    @Updatable
    @Required
    @ValidStrings({ "NO_CACHE", "S3", "LOCAL" })
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The cache location.
     */
    @Updatable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * The list of local cache modes.
     */
    @Updatable
    @ValidStrings({"LOCAL_DOCKER_LAYER_CACHE", "LOCAL_SOURCE_CACHE", "LOCAL_CUSTOM_CACHE"})
    public List<String> getModes() {
        if (modes == null) {
            modes = new ArrayList<>();
        }
        return modes;
    }

    public void setModes(List<String> modes) {
        this.modes = modes;
    }

    @Override
    public void copyFrom(ProjectCache model) {
        setType(model.typeAsString());
        setLocation(model.location());
        setModes(model.modesAsStrings());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public ProjectCache toProjectCache() {
        return ProjectCache.builder()
            .type(getType())
            .location(getLocation())
            .modesWithStrings(getModes())
            .build();
    }
}
