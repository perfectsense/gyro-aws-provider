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

package gyro.aws.kendra;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.DocumentsMetadataConfiguration;

public class KendraDocumentsMetadataConfiguration extends Diffable implements Copyable<DocumentsMetadataConfiguration> {

    private String s3Prefix;

    /**
     * The prefix used to filter metadata configuration files in the AWS S3 bucket. (Required)
     */
    @Updatable
    @Required
    public String getS3Prefix() {
        return s3Prefix;
    }

    public void setS3Prefix(String s3Prefix) {
        this.s3Prefix = s3Prefix;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(DocumentsMetadataConfiguration model) {
        setS3Prefix(model.s3Prefix());
    }

    public DocumentsMetadataConfiguration toDocumentsMetadataConfiguration() {
        return DocumentsMetadataConfiguration.builder().s3Prefix(getS3Prefix()).build();
    }
}
