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
import software.amazon.awssdk.services.codebuild.model.ReportPackagingType;
import software.amazon.awssdk.services.codebuild.model.S3ReportExportConfig;

public class CodebuildS3ReportExportConfig extends Diffable implements Copyable<S3ReportExportConfig> {

    private String bucket;
    private Boolean encryptionDisabled;
    private String encryptionKey;
    private ReportPackagingType packaging;
    private String path;

    /**
     * The name of the S3 bucket where the raw data of a report are exported.
     */
    @Updatable
    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * When set to ``true`` the results of a report are not encrypted.
     */
    @Updatable
    public Boolean getEncryptionDisabled() {
        return encryptionDisabled;
    }

    public void setEncryptionDisabled(Boolean encryptionDisabled) {
        this.encryptionDisabled = encryptionDisabled;
    }

    /**
     * The encryption key for the report's encrypted raw data.
     */
    @Updatable
    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    /**
     * The type of build output artifact to create. Valid values are ``ZIP`` or ``NONE``.
     */
    @Updatable
    public ReportPackagingType getPackaging() {
        return packaging;
    }

    public void setPackaging(ReportPackagingType packaging) {
        this.packaging = packaging;
    }

    /**
     * The path to the exported report's raw data results.
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void copyFrom(S3ReportExportConfig model) {
        setBucket(model.bucket());
        setEncryptionDisabled(model.encryptionDisabled());
        setEncryptionKey(model.encryptionKey());
        setPackaging(model.packaging());
        setPath(model.path());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public S3ReportExportConfig toS3ReportExportConfig() {
        return S3ReportExportConfig.builder()
            .bucket(getBucket())
            .encryptionDisabled(getEncryptionDisabled())
            .encryptionKey(getEncryptionKey())
            .packaging(getPackaging())
            .path(getPath())
            .build();
    }
}
