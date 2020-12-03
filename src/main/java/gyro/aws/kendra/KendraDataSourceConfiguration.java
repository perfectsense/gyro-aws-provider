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
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.kendra.model.DataSourceConfiguration;

public class KendraDataSourceConfiguration extends Diffable implements Copyable<DataSourceConfiguration> {

    private KendraDatabaseConfiguration databaseConfiguration;
    private KendraOneDriveConfiguration oneDriveConfiguration;
    private KendraS3DataSourceConfiguration s3Configuration;
    private KendraSalesforceConfiguration salesforceConfiguration;
    private KendraServiceNowConfiguration serviceNowConfiguration;
    private KendraSharePointConfiguration sharePointConfiguration;

    /**
     * The information necessary to create a data source connector for a database.
     *
     * @subresource gyro.aws.kendra.KendraDatabaseConfiguration
     */
    @Updatable
    @ConflictsWith({
        "one-drive-configuration", "s3-configuration", "salesforce-configuration",
        "service-now-configuration", "share-point-configuration" })
    public KendraDatabaseConfiguration getDatabaseConfiguration() {
        return databaseConfiguration;
    }

    public void setDatabaseConfiguration(KendraDatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    /**
     * The configuration for data sources that connect to Microsoft OneDrive.
     *
     * @subresource gyro.aws.kendra.KendraOneDriveConfiguration
     */
    @Updatable
    @ConflictsWith({
        "database-configuration", "s3-configuration", "salesforce-configuration",
        "service-now-configuration", "share-point-configuration" })
    public KendraOneDriveConfiguration getOneDriveConfiguration() {
        return oneDriveConfiguration;
    }

    public void setOneDriveConfiguration(KendraOneDriveConfiguration oneDriveConfiguration) {
        this.oneDriveConfiguration = oneDriveConfiguration;
    }

    /**
     * The information to create a data source connector for a document repository in an Amazon S3 bucket.
     *
     * @subresource gyro.aws.kendra.KendraS3DataSourceConfiguration
     */
    @Updatable
    @ConflictsWith({
        "database-configuration", "one-drive-configuration", "salesforce-configuration",
        "service-now-configuration", "share-point-configuration" })
    public KendraS3DataSourceConfiguration getS3Configuration() {
        return s3Configuration;
    }

    public void setS3Configuration(KendraS3DataSourceConfiguration s3Configuration) {
        this.s3Configuration = s3Configuration;
    }

    /**
     * The configuration information for data sources that connect to a Salesforce site.
     *
     * @subresource gyro.aws.kendra.KendraSalesforceConfiguration
     */
    @Updatable
    @ConflictsWith({
        "database-configuration", "one-drive-configuration", "s3-configuration",
        "service-now-configuration", "share-point-configuration" })
    public KendraSalesforceConfiguration getSalesforceConfiguration() {
        return salesforceConfiguration;
    }

    public void setSalesforceConfiguration(KendraSalesforceConfiguration salesforceConfiguration) {
        this.salesforceConfiguration = salesforceConfiguration;
    }

    /**
     * The configuration for data sources that connect to ServiceNow instances.
     *
     * @subresource gyro.aws.kendra.KendraServiceNowConfiguration
     */
    @Updatable
    @ConflictsWith({
        "database-configuration", "one-drive-configuration", "s3-configuration",
        "salesforce-configuration", "share-point-configuration" })
    public KendraServiceNowConfiguration getServiceNowConfiguration() {
        return serviceNowConfiguration;
    }

    public void setServiceNowConfiguration(KendraServiceNowConfiguration serviceNowConfiguration) {
        this.serviceNowConfiguration = serviceNowConfiguration;
    }

    /**
     * The information necessary to create a data source connector for a Microsoft SharePoint site.
     *
     * @subresource gyro.aws.kendra.KendraSharePointConfiguration
     */
    @Updatable
    @ConflictsWith({
        "database-configuration", "one-drive-configuration", "s3-configuration",
        "salesforce-configuration", "service-now-configuration" })
    public KendraSharePointConfiguration getSharePointConfiguration() {
        return sharePointConfiguration;
    }

    public void setSharePointConfiguration(KendraSharePointConfiguration sharePointConfiguration) {
        this.sharePointConfiguration = sharePointConfiguration;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(DataSourceConfiguration model) {
        if (model.databaseConfiguration() != null) {
            KendraDatabaseConfiguration config = newSubresource(KendraDatabaseConfiguration.class);
            config.copyFrom(model.databaseConfiguration());
            setDatabaseConfiguration(config);
        }

        if (model.oneDriveConfiguration() != null) {
            KendraOneDriveConfiguration config = newSubresource(KendraOneDriveConfiguration.class);
            config.copyFrom(model.oneDriveConfiguration());
            setOneDriveConfiguration(config);
        }

        if (model.s3Configuration() != null) {
            KendraS3DataSourceConfiguration config = newSubresource(KendraS3DataSourceConfiguration.class);
            config.copyFrom(model.s3Configuration());
            setS3Configuration(config);
        }

        if (model.salesforceConfiguration() != null) {
            KendraSalesforceConfiguration config = newSubresource(KendraSalesforceConfiguration.class);
            config.copyFrom(model.salesforceConfiguration());
            setSalesforceConfiguration(config);
        }

        if (model.serviceNowConfiguration() != null) {
            KendraServiceNowConfiguration config = newSubresource(KendraServiceNowConfiguration.class);
            config.copyFrom(model.serviceNowConfiguration());
            setServiceNowConfiguration(config);
        }

        if (model.sharePointConfiguration() != null) {
            KendraSharePointConfiguration config = newSubresource(KendraSharePointConfiguration.class);
            config.copyFrom(model.sharePointConfiguration());
            setSharePointConfiguration(config);
        }
    }

    public DataSourceConfiguration toDataSourceConfiguration() {
        DataSourceConfiguration.Builder builder = DataSourceConfiguration.builder();

        if (getDatabaseConfiguration() != null) {
            builder = builder.databaseConfiguration(getDatabaseConfiguration().toDatabaseConfiguration());
        }

        if (getOneDriveConfiguration() != null) {
            builder = builder.oneDriveConfiguration(getOneDriveConfiguration().toOneDriveConfiguration());
        }

        if (getS3Configuration() != null) {
            builder = builder.s3Configuration(getS3Configuration().toS3DataSourceConfiguration());
        }

        if (getSalesforceConfiguration() != null) {
            builder = builder.salesforceConfiguration(getSalesforceConfiguration().toSalesforceConfiguration());
        }

        if (getServiceNowConfiguration() != null) {
            builder = builder.serviceNowConfiguration(getServiceNowConfiguration().toServiceNowConfiguration());
        }

        if (getSharePointConfiguration() != null) {
            builder = builder.sharePointConfiguration(getSharePointConfiguration().toSharePointConfiguration());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!configuredFields.contains("database-configuration")
            && !configuredFields.contains("one-drive-configuration") && !configuredFields.contains("s3-configuration")
            && !configuredFields.contains("salesforce-configuration") && !configuredFields.contains(
            "service-now-configuration") && !configuredFields.contains("share-point-configuration")) {
            errors.add(new ValidationError(
                this,
                null,
                "At least one of 'database-configuration', 'one-drive-configuration', 's3-configuration', "
                    + "'salesforce-configuration', 'service-now-configuration' "
                    + "or 'share-point-configuration' is required."));
        }

        return errors;
    }
}
