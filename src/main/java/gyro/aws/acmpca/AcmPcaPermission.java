/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.acmpca;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.acmpca.AcmPcaClient;
import software.amazon.awssdk.services.acmpca.model.Permission;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class AcmPcaPermission extends AwsResource implements Copyable<Permission> {
    private Set<String> actions;
    private String principal;

    // - Output
    private String policy;
    private Date createdAt;

    /**
     * Actions associated with the permission.
     */
    @Updatable
    public Set<String> getActions() {
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    /**
     * The AWS service or entity that holds the permission. Defaults to ``acm.amazonaws.com``.
     */
    @ValidStrings("acm.amazonaws.com")
    public String getPrincipal() {
        if (principal == null) {
            principal = "acm.amazonaws.com";
        }

        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    /**
     * The policy that is associated with the permission.
     */
    @Output
    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    /**
     * The time at which the permission was created.
     */
    @Output
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void copyFrom(Permission permission) {
        setActions(new HashSet<>(permission.actionsAsStrings()));
        setPolicy(permission.policy());
        setCreatedAt(Date.from(permission.createdAt()));
        setPrincipal(permission.principal());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AcmPcaClient client = createClient(AcmPcaClient.class);

        client.createPermission(
            r -> r.certificateAuthorityArn(getParent())
                .actionsWithStrings(getActions())
                .principal(getPrincipal())
                .sourceAccount(getAccountNumber())
        );
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AcmPcaClient client = createClient(AcmPcaClient.class);

        client.deletePermission(
            r -> r.certificateAuthorityArn(getParent())
                .principal(getPrincipal())
                .sourceAccount(getAccountNumber())
        );

        client.createPermission(
            r -> r.certificateAuthorityArn(getParent())
                .actionsWithStrings(getActions())
                .principal(getPrincipal())
                .sourceAccount(getAccountNumber())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AcmPcaClient client = createClient(AcmPcaClient.class);

        client.deletePermission(
            r -> r.certificateAuthorityArn(getParent())
                .principal(getPrincipal())
                .sourceAccount(getAccountNumber())
        );
    }

    @Override
    public String primaryKey() {
        return getPrincipal();
    }

    private String getAccountNumber() {
        StsClient client = createClient(StsClient.class);
        GetCallerIdentityResponse response = client.getCallerIdentity();
        return response.account();
    }

    private String getParent() {
        AcmPcaCertificateAuthority parent = (AcmPcaCertificateAuthority) parent();
        return parent.getArn();
    }
}
