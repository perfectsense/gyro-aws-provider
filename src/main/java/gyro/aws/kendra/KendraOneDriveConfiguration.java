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
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.OneDriveConfiguration;

public class KendraOneDriveConfiguration extends Diffable implements Copyable<OneDriveConfiguration> {

    private List<String> exclusionPatterns;
    private List<String> inclusionPatterns;
    private String secret;
    private String tenantDomain;
    private List<KendraDataSourceToIndexFieldMapping> fieldMapping;
    private KendraOneDriveUsers users;

    /**
     * List of regular expression patterns to exclude.
     */
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
     * List of regular expression patterns to include.
     */
    public List<String> getInclusionPatterns() {
        if (inclusionPatterns == null) {
            inclusionPatterns = new ArrayList<>();
        }

        return inclusionPatterns;
    }

    public void setInclusionPatterns(List<String> inclusionPatterns) {
        this.inclusionPatterns = inclusionPatterns;
    }

    /**
     * The Amazon Resource Name (ARN) of an AWS Secrets Manager secret that contains the user name and password to connect to OneDrive.
     */
    @Updatable
    @Required
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * Tha Azure Active Directory domain of the organization.
     */
    @Updatable
    @Required
    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    /**
     * The list of objects that map Microsoft OneDrive fields to custom fields in the Amazon Kendra index.
     */
    public List<KendraDataSourceToIndexFieldMapping> getFieldMapping() {
        if (fieldMapping == null) {
            fieldMapping = new ArrayList<>();
        }

        return fieldMapping;
    }

    public void setFieldMapping(List<KendraDataSourceToIndexFieldMapping> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    /**
     * The list of user accounts whose documents should be indexed.
     */
    @Required
    public KendraOneDriveUsers getUsers() {
        return users;
    }

    public void setUsers(KendraOneDriveUsers users) {
        this.users = users;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(OneDriveConfiguration model) {
        setExclusionPatterns(model.exclusionPatterns());
        setInclusionPatterns(model.inclusionPatterns());
        setSecret(model.secretArn());
        setTenantDomain(model.tenantDomain());

        if (model.hasFieldMappings()) {
            setFieldMapping(model.fieldMappings().stream().map(f -> {
                KendraDataSourceToIndexFieldMapping mapping = newSubresource(KendraDataSourceToIndexFieldMapping.class);
                mapping.copyFrom(f);

                return mapping;
            }).collect(Collectors.toList()));
        }

        KendraOneDriveUsers oneDriveUsers = newSubresource(KendraOneDriveUsers.class);
        oneDriveUsers.copyFrom(model.oneDriveUsers());
        setUsers(oneDriveUsers);
    }

    public OneDriveConfiguration toOneDriveConfiguration() {
        return OneDriveConfiguration.builder()
            .exclusionPatterns(getExclusionPatterns())
            .inclusionPatterns(getInclusionPatterns())
            .fieldMappings(getFieldMapping().stream()
                .map(KendraDataSourceToIndexFieldMapping::toDataSourceToIndexFieldMapping)
                .collect(Collectors.toList()))
            .oneDriveUsers(getUsers().toOneDriveUsers())
            .secretArn(getSecret())
            .tenantDomain(getTenantDomain())
            .build();
    }
}
