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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.ProjectSourceVersion;

public class CodebuildProjectSourceVersion extends Diffable implements Copyable<ProjectSourceVersion> {

    private String sourceIdentifier;
    private String sourceVersion;

    /**
     * The identifier for the source in the build project.
     */
    @Updatable
    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }

    /**
     * The source version for the corresponding source identifier.
     */
    @Updatable
    public String getSourceVersion() {
        return sourceVersion;
    }

    public void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    @Override
    public void copyFrom(ProjectSourceVersion model) {
        setSourceIdentifier(model.sourceIdentifier());
        setSourceVersion(model.sourceVersion());
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getSourceIdentifier());
    }
}
