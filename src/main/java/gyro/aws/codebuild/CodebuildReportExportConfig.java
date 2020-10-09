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
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.ReportExportConfig;

public class CodebuildReportExportConfig extends Diffable implements Copyable<ReportExportConfig> {

    private String exportConfigType;
    private CodebuildS3ReportExportConfig s3ReportExportConfig;

    /**
     * The export configuration type.
     */
    @Updatable
    @ValidStrings({ "S3", "NO_EXPORT" })
    public String getExportConfigType() {
        return exportConfigType;
    }

    public void setExportConfigType(String exportConfigType) {
        this.exportConfigType = exportConfigType;
    }

    /**
     * The S3 bucket where the run of a export is exported.
     *
     * @subresource gyro.aws.codebuild.CodebuildS3ReportExportConfig
     */
    @Updatable
    public CodebuildS3ReportExportConfig getS3ReportExportConfig() {
        return s3ReportExportConfig;
    }

    public void setS3ReportExportConfig(CodebuildS3ReportExportConfig s3ReportExportConfig) {
        this.s3ReportExportConfig = s3ReportExportConfig;
    }

    @Override
    public void copyFrom(ReportExportConfig model) {
        setExportConfigType(model.exportConfigTypeAsString());

        if (model.s3Destination() != null) {
            CodebuildS3ReportExportConfig s3ReportExportConfig = newSubresource(CodebuildS3ReportExportConfig.class);
            s3ReportExportConfig.copyFrom(model.s3Destination());
            setS3ReportExportConfig(s3ReportExportConfig);
        } else {
            setS3ReportExportConfig(null);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public ReportExportConfig toReportExportConfig() {
        return ReportExportConfig.builder()
            .exportConfigType(getExportConfigType())
            .s3Destination(
                getS3ReportExportConfig() != null ? getS3ReportExportConfig().toS3ReportExportConfig() : null)
            .build();
    }
}
