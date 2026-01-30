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

/**
 * The import source for importing key-value pairs from S3.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     import-source
 *         source-type: "S3"
 *         source-arn: "arn:aws:s3:::my-bucket/kvs-data.json"
 *     end
 */
public class CloudFrontKeyValueStoreImportSource extends Diffable implements Copyable<ImportSource> {

    private String sourceType;
    private String sourceArn;

    /**
     * The source type of the import source. Currently only "S3" is supported.
     */
    @Required
    @ValidStrings("S3")
    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * The Amazon Resource Name (ARN) of the S3 bucket containing the import data.
     */
    @Required
    public String getSourceArn() {
        return sourceArn;
    }

    public void setSourceArn(String sourceArn) {
        this.sourceArn = sourceArn;
    }

    @Override
    public void copyFrom(ImportSource model) {
        setSourceType(model.sourceType().toString());
        setSourceArn(model.sourceARN());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public ImportSource toImportSource() {
        return ImportSource.builder()
            .sourceType(getSourceType())
            .sourceARN(getSourceArn())
            .build();
    }
}
