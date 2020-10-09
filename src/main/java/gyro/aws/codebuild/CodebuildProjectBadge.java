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
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.ProjectBadge;

public class CodebuildProjectBadge extends Diffable implements Copyable<ProjectBadge> {

    private Boolean badgeEnabled;
    private String badgeRequestUrl;

    /**
     * When set to ``true`` generates a publicly accessible URL for the project's build badge.
     */
    @Updatable
    public Boolean getBadgeEnabled() {
        return badgeEnabled;
    }

    public void setBadgeEnabled(Boolean badgeEnabled) {
        this.badgeEnabled = badgeEnabled;
    }

    /**
     * The publicly accessible URL for the project's build badge.
     */
    @Output
    public String getBadgeRequestUrl() {
        return badgeRequestUrl;
    }

    public void setBadgeRequestUrl(String badgeRequestUrl) {
        this.badgeRequestUrl = badgeRequestUrl;
    }

    @Override
    public void copyFrom(ProjectBadge model) {
        setBadgeEnabled(model.badgeEnabled());
        setBadgeRequestUrl(model.badgeRequestUrl());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
