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
import software.amazon.awssdk.services.codebuild.model.BuildStatusConfig;

public class CodebuildBuildStatusConfig extends Diffable implements Copyable<BuildStatusConfig> {

    private String context;
    private String targetUrl;

    /**
     * The context of the build status CodeBuild sends to the source provider. See `Context Info
     * <https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/codebuild/model/BuildStatusConfig.html#getContext--/>`_.
     */
    @Updatable
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    /**
     * The target URL of the build status CodeBuild sends to the source provider. See `Target URL Info
     * <https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/codebuild/model/BuildStatusConfig.html#getTargetUrl--/>`_.
     */
    @Updatable
    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @Override
    public void copyFrom(BuildStatusConfig model) {
        setContext(model.context());
        setTargetUrl(model.targetUrl());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public BuildStatusConfig toBuildStatusConfig() {
        return BuildStatusConfig.builder()
            .context(getContext())
            .targetUrl(getTargetUrl())
            .build();
    }
}
