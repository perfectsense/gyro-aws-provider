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
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.kendra.model.ServiceNowBuildVersionType;
import software.amazon.awssdk.services.kendra.model.ServiceNowConfiguration;

public class KendraServiceNowConfiguration extends Diffable implements Copyable<ServiceNowConfiguration> {

    private String hostUrl;
    private String secretArn;
    private ServiceNowBuildVersionType versionType;
    private KendraServiceNowKnowledgeArticleConfiguration knowledgeArticleConfiguration;
    private KendraServiceNowServiceCatalogConfiguration serviceCatalogConfiguration;

    /**
     * The ServiceNow instance that the data source connects to.
     */
    @Updatable
    @Required
    public String getHostUrl() {
        return hostUrl;
    }

    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    /**
     * The Amazon Resource Name (ARN) of the AWS Secret Manager secret that contains the user name and password required to connect to the ServiceNow instance.
     */
    @Updatable
    @Required
    public String getSecretArn() {
        return secretArn;
    }

    public void setSecretArn(String secretArn) {
        this.secretArn = secretArn;
    }

    /**
     * The identifier of the release that the ServiceNow host is running.
     */
    @Updatable
    @ValidStrings({ "LONDON", "OTHERS" })
    public ServiceNowBuildVersionType getVersionType() {
        return versionType;
    }

    public void setVersionType(ServiceNowBuildVersionType versionType) {
        this.versionType = versionType;
    }

    /**
     * The configuration for crawling knowledge articles in the ServiceNow site.
     */
    @Updatable
    public KendraServiceNowKnowledgeArticleConfiguration getKnowledgeArticleConfiguration() {
        return knowledgeArticleConfiguration;
    }

    public void setKnowledgeArticleConfiguration(KendraServiceNowKnowledgeArticleConfiguration knowledgeArticleConfiguration) {
        this.knowledgeArticleConfiguration = knowledgeArticleConfiguration;
    }

    /**
     * The configuration for crawling service catalogs in the ServiceNow site.
     */
    @Updatable
    public KendraServiceNowServiceCatalogConfiguration getServiceCatalogConfiguration() {
        return serviceCatalogConfiguration;
    }

    public void setServiceCatalogConfiguration(KendraServiceNowServiceCatalogConfiguration serviceCatalogConfiguration) {
        this.serviceCatalogConfiguration = serviceCatalogConfiguration;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(ServiceNowConfiguration model) {
        setHostUrl(model.hostUrl());
        setSecretArn(model.secretArn());
        setVersionType(model.serviceNowBuildVersion());

        if (model.knowledgeArticleConfiguration() != null) {
            KendraServiceNowKnowledgeArticleConfiguration config = newSubresource(
                KendraServiceNowKnowledgeArticleConfiguration.class);
            config.copyFrom(model.knowledgeArticleConfiguration());
            setKnowledgeArticleConfiguration(config);
        }

        if (model.serviceCatalogConfiguration() != null) {
            KendraServiceNowServiceCatalogConfiguration config = newSubresource(
                KendraServiceNowServiceCatalogConfiguration.class);
            config.copyFrom(model.serviceCatalogConfiguration());
            setServiceCatalogConfiguration(config);
        }
    }

    public ServiceNowConfiguration toServiceNowConfiguration() {
        ServiceNowConfiguration.Builder builder = ServiceNowConfiguration.builder()
            .hostUrl(getHostUrl())
            .secretArn(getSecretArn())
            .serviceNowBuildVersion(getVersionType());

        if (getKnowledgeArticleConfiguration() != null) {
            builder = builder.knowledgeArticleConfiguration(getKnowledgeArticleConfiguration()
                .toServiceNowKnowledgeArticleConfiguration());
        }

        if (getServiceCatalogConfiguration() != null) {
            builder = builder.serviceCatalogConfiguration(getServiceCatalogConfiguration()
                .toServiceNowServiceCatalogConfiguration());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!configuredFields.contains("knowledge-article-configuration") && !configuredFields.contains(
            "service-catalog-configuration")) {
            errors.add(new ValidationError(this, null,
                "At least one of 'knowledge-article-configuration' or 'service-catalog-configuration' is required."));
        }

        return errors;
    }
}
