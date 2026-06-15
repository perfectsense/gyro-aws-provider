/*
 * Copyright 2026, Brightspot.
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

package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.cloudfront.model.ImportSource;
import software.amazon.awssdk.services.cloudfront.model.ImportSourceType;

public class CloudFrontKeyValueStoreImportSource extends Diffable implements Copyable<ImportSource> {

    private ImportSourceType sourceType;
    private String sourceArn;

    /**
     * The source type of the import source. Currently only ``S3`` is supported.
     */
    @Required
    @ValidStrings("S3")
    public ImportSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ImportSourceType sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * The ARN of the S3 object containing the import data.
     */
    @Required
    public String getSourceArn() {
        return sourceArn;
    }

    public void setSourceArn(String sourceArn) {
        this.sourceArn = sourceArn;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(ImportSource model) {
        setSourceType(model.sourceType());
        setSourceArn(model.sourceARN());
    }

    ImportSource toImportSource() {
        return ImportSource.builder()
            .sourceType(getSourceType())
            .sourceARN(getSourceArn())
            .build();
    }
}
