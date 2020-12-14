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

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.aws.s3.BucketResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.S3DataSourceConfiguration;

public class KendraS3DataSourceConfiguration extends Diffable implements Copyable<S3DataSourceConfiguration> {

    private S3AccessControlListConfiguration accessControlListConfiguration;
    private BucketResource bucket;
    private KendraDocumentsMetadataConfiguration documentsMetadataConfiguration;
    private List<String> exclusionPatterns;
    private List<String> inclusionPrefixes;

    /**
     * The path to the S3 bucket that contains the user context filtering files for the data source.
     *
     * @subresource gyro.aws.kendra.S3AccessControlListConfiguration
     */
    @Updatable
    public S3AccessControlListConfiguration getAccessControlListConfiguration() {
        return accessControlListConfiguration;
    }

    public void setAccessControlListConfiguration(S3AccessControlListConfiguration accessControlListConfiguration) {
        this.accessControlListConfiguration = accessControlListConfiguration;
    }

    /**
     * The bucket that contains the documents.
     */
    @Updatable
    @Required
    public BucketResource getBucket() {
        return bucket;
    }

    public void setBucket(BucketResource bucket) {
        this.bucket = bucket;
    }

    /**
     * The value of the DocumentsMetadataConfiguration property for this object.
     *
     * @subresource gyro.aws.kendra.KendraDocumentsMetadataConfiguration
     */
    @Updatable
    public KendraDocumentsMetadataConfiguration getDocumentsMetadataConfiguration() {
        return documentsMetadataConfiguration;
    }

    public void setDocumentsMetadataConfiguration(KendraDocumentsMetadataConfiguration documentsMetadataConfiguration) {
        this.documentsMetadataConfiguration = documentsMetadataConfiguration;
    }

    /**
     * The list of glob patterns for documents that should not be indexed.
     */
    @Updatable
    public List<String> getExclusionPatterns() {
        if (exclusionPatterns == null) {
            exclusionPatterns = new ArrayList<>();
        }

        return exclusionPatterns;
    }

    public void setExclusionPatterns(List<String> exclusionPatterns) {
        this.exclusionPatterns = exclusionPatterns;
    }

    /**
     * The list of S3 prefixes for the documents that should be included in the index.
     */
    @Updatable
    public List<String> getInclusionPrefixes() {
        if (inclusionPrefixes == null) {
            inclusionPrefixes = new ArrayList<>();
        }

        return inclusionPrefixes;
    }

    public void setInclusionPrefixes(List<String> inclusionPrefixes) {
        this.inclusionPrefixes = inclusionPrefixes;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(S3DataSourceConfiguration model) {
        setBucket(findById(BucketResource.class, model.bucketName()));
        setExclusionPatterns(model.exclusionPatterns());
        setInclusionPrefixes(model.inclusionPrefixes());

        if (model.accessControlListConfiguration() != null) {
            S3AccessControlListConfiguration configuration = newSubresource(S3AccessControlListConfiguration.class);
            configuration.copyFrom(model.accessControlListConfiguration());
            setAccessControlListConfiguration(configuration);
        }

        if (model.documentsMetadataConfiguration() != null) {
            KendraDocumentsMetadataConfiguration configuration = newSubresource(KendraDocumentsMetadataConfiguration.class);
            configuration.copyFrom(model.documentsMetadataConfiguration());
            setDocumentsMetadataConfiguration(configuration);
        }
    }

    public S3DataSourceConfiguration toS3DataSourceConfiguration() {
        S3DataSourceConfiguration.Builder builder = S3DataSourceConfiguration.builder()
            .bucketName(getBucket().getName())
            .exclusionPatterns(getExclusionPatterns())
            .inclusionPrefixes(getInclusionPrefixes());

        if (getDocumentsMetadataConfiguration() != null) {
            builder = builder.documentsMetadataConfiguration(getDocumentsMetadataConfiguration().toDocumentsMetadataConfiguration());
        }

        if (getAccessControlListConfiguration() != null) {
            builder = builder.accessControlListConfiguration(getAccessControlListConfiguration().toAccessControlListConfiguration());
        }

        return builder.build();
    }
}
