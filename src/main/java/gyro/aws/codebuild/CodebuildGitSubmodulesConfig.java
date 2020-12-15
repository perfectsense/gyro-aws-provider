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
import gyro.core.validation.Required;
import software.amazon.awssdk.services.codebuild.model.GitSubmodulesConfig;

public class CodebuildGitSubmodulesConfig extends Diffable implements Copyable<GitSubmodulesConfig> {

    private Boolean fetchSubmodules;

    /**
     * When set to ``true`` all Git submodules are fetched for the build project.
     */
    @Required
    @Updatable
    public Boolean getFetchSubmodules() {
        return fetchSubmodules;
    }

    public void setFetchSubmodules(Boolean fetchSubmodules) {
        this.fetchSubmodules = fetchSubmodules;
    }

    @Override
    public void copyFrom(GitSubmodulesConfig model) {
        setFetchSubmodules(model.fetchSubmodules());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public GitSubmodulesConfig toGitSubmodulesConfig() {
        return GitSubmodulesConfig.builder()
            .fetchSubmodules(getFetchSubmodules()).build();
    }
}
