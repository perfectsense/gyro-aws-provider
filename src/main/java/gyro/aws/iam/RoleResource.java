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

package gyro.aws.iam;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.route53.RecordSetResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AttachedPolicy;
import software.amazon.awssdk.services.iam.model.CreateRoleResponse;
import software.amazon.awssdk.services.iam.model.GetRolePolicyResponse;
import software.amazon.awssdk.services.iam.model.GetRoleResponse;
import software.amazon.awssdk.services.iam.model.ListAttachedRolePoliciesResponse;
import software.amazon.awssdk.services.iam.model.ListRolePoliciesResponse;
import software.amazon.awssdk.services.iam.model.NoSuchEntityException;
import software.amazon.awssdk.services.iam.model.Role;
import software.amazon.awssdk.services.iam.model.Tag;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates an IAM role resource.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::iam-role example-role
 *         name: "rta-test-role"
 *         description: "testing the role functionality"
 *         assume-role-policy: "role_example.json"
 *         policies: [(aws::iam-policy policy)]
 *     end
 */
@Type("iam-role")
public class RoleResource extends AwsResource implements Copyable<Role> {

    private String arn;
    private String assumeRolePolicy;
    private String description;
    private Set<PolicyResource> policies;
    private Integer maxSessionDuration;
    private String name;
    private String path;
    private String permissionsBoundaryArn;
    private Map<String, String> tags;
    private Set<RoleInlinePolicyResource> inlinePolicy;

    /**
     * The arn of the role.
     */
    @Output
    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The assumed role policy.
     */
    @Required
    @Updatable
    public String getAssumeRolePolicy() {
        if (this.assumeRolePolicy != null && this.assumeRolePolicy.contains(".json")) {
            try (InputStream input = openInput(this.assumeRolePolicy)) {
                this.assumeRolePolicy = PolicyResource.formatPolicy(IoUtils.toUtf8String(input));
                return this.assumeRolePolicy;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return PolicyResource.formatPolicy(this.assumeRolePolicy);
        }
    }

    public void setAssumeRolePolicy(String assumeRolePolicy) {
        this.assumeRolePolicy = assumeRolePolicy;
    }

    /**
     * The description of the role.
     */
    @Updatable
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The policies associated with the role.
     */
    @Updatable
    public Set<PolicyResource> getPolicies() {
        if (policies == null) {
            policies = new HashSet<>();
        }

        return policies;
    }

    public void setPolicies(Set<PolicyResource> policies) {
        this.policies = policies;
    }

    /**
     * The maximum duration of the role, in seconds.
     */
    @Updatable
    @Range(min = 3600, max = 43200)
    public Integer getMaxSessionDuration() {
        if (maxSessionDuration == null) {
            maxSessionDuration = 3600;
        }

        return maxSessionDuration;
    }

    public void setMaxSessionDuration(Integer maxSessionDuration) {
        this.maxSessionDuration = maxSessionDuration;
    }

    /**
     * The name of the role.
     */
    @Required
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The path to the role. Defaults to ``/``.
     */
    public String getPath() {
        if (path == null) {
            path = "/";
        }

        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * The arn of the permission boundary.
     */
    public String getPermissionsBoundaryArn() {
        return permissionsBoundaryArn;
    }

    public void setPermissionsBoundaryArn(String permissionsBoundaryArn) {
        this.permissionsBoundaryArn = permissionsBoundaryArn;
    }

    /**
     * The tags associated with the role.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * A list of inline rile policies.RolePolicyResource
     *
     * @subresource gyro.aws.iam.RoleInlinePolicyResource
     */
    @Updatable
    public Set<RoleInlinePolicyResource> getInlinePolicy() {
        if (inlinePolicy == null) {
            inlinePolicy = new HashSet<>();
        }

        return inlinePolicy;
    }

    public void setInlinePolicy(Set<RoleInlinePolicyResource> inlinePolicy) {
        this.inlinePolicy = inlinePolicy;
    }

    @Override
    public void copyFrom(Role role) {
        IamClient client = createClient(IamClient.class);

        setArn(role.arn());
        setName(role.roleName());
        setDescription(role.description());
        String encode = URLDecoder.decode(role.assumeRolePolicyDocument());
        setAssumeRolePolicy(PolicyResource.formatPolicy(encode));
        setMaxSessionDuration(role.maxSessionDuration());
        setPath(role.path());
        setPermissionsBoundaryArn(role.permissionsBoundary() != null ? role.permissionsBoundary().permissionsBoundaryArn() : null);
        getTags().entrySet().forEach(r -> getTags().put(r.getKey(), r.getValue()));

        getPolicies().clear();
        ListAttachedRolePoliciesResponse policyResponse = client.listAttachedRolePolicies(r -> r.roleName(getName()));
        for (AttachedPolicy attachedPolicy: policyResponse.attachedPolicies()) {
            getPolicies().add(findById(PolicyResource.class, attachedPolicy.policyArn()));
        }

        getInlinePolicy().clear();
        ListRolePoliciesResponse inlinePolicyResponse = client.listRolePolicies(r -> r.roleName(getName()));
        for (String inlinePolicy : inlinePolicyResponse.policyNames()) {
            GetRolePolicyResponse policy = client.getRolePolicy(r -> r.roleName(getName()).policyName(inlinePolicy));
            RoleInlinePolicyResource policyResource = newSubresource(RoleInlinePolicyResource.class);
            policyResource.copyFrom(policy);
            getInlinePolicy().add(policyResource);
        }
    }

    @Override
    public boolean refresh() {
        IamClient client = createClient(IamClient.class);

        Role role = getRole(client);

        if (role != null) {
            this.copyFrom(role);

            return true;
        }

        return false;
    }

    @Override
    public Map<? extends Resource, Boolean> batchRefresh(List<? extends Resource> resources) {
        IamClient client = createClient(IamClient.class);
        Map<RoleResource, Boolean> refreshStatus = new HashMap<>();

        List<Role> roles = getRoles(client);

        for (Resource resource : resources) {
            RoleResource roleResource = (RoleResource) resource;

            Role role = getRole(roles, roleResource.getName());
            if (role != null) {
                roleResource.copyFrom(role);
                refreshStatus.put(roleResource, true);
            } else {
                refreshStatus.put(roleResource, false);
            }

        }

        return refreshStatus;
    }

    @Override
    public void create(GyroUI ui, State state) {
        IamClient client = createClient(IamClient.class);

        CreateRoleResponse response = client.createRole(r -> r.assumeRolePolicyDocument(getAssumeRolePolicy())
                .description(getDescription())
                .maxSessionDuration(getMaxSessionDuration())
                .path(getPath())
                .roleName(getName())
                .permissionsBoundary(getPermissionsBoundaryArn())
                .tags(toTags(getTags())));

        setArn(response.role().arn());

        try {
            for (PolicyResource policy : getPolicies()) {
                client.attachRolePolicy(r -> r.roleName(getName()).policyArn(policy.getArn()));
            }
        } catch (Exception err) {
            delete(ui, state);
            throw new GyroException(err.getMessage());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        IamClient client = createClient(IamClient.class);

        client.updateAssumeRolePolicy(r -> r.policyDocument(PolicyResource.formatPolicy(getAssumeRolePolicy()))
                                            .roleName(getName()));

        client.updateRole(r -> r.description(getDescription())
                                .maxSessionDuration(getMaxSessionDuration())
                                .roleName(getName()));

        RoleResource currentResource = (RoleResource) current;

        List<PolicyResource> additions = new ArrayList<>(getPolicies());
        additions.removeAll(currentResource.getPolicies());

        List<PolicyResource> subtractions = new ArrayList<>(currentResource.getPolicies());
        subtractions.removeAll(getPolicies());

        for (PolicyResource addPolicyArn : additions) {
            client.attachRolePolicy(r -> r.policyArn(addPolicyArn.getArn())
                    .roleName(getName()));
        }

        for (PolicyResource deletePolicyArn : subtractions) {
            client.detachRolePolicy(r -> r.policyArn(deletePolicyArn.getArn())
                    .roleName(getName()));
        }

        Map<String, String> oldTags = ((RoleResource) current).getTags();

        List<String> removeTags = oldTags.keySet().stream()
                .filter(o -> !getTags().containsKey(o))
                .collect(Collectors.toList());

        client.untagRole(r -> r.roleName(getName()).tagKeys(removeTags));

        if (!getTags().isEmpty()) {
            client.tagRole(r -> r.roleName(getName()).tags(toTags(getTags())));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        IamClient client = createClient(IamClient.class);

        ListAttachedRolePoliciesResponse response = client.listAttachedRolePolicies(r -> r.roleName(getName()));
        for (AttachedPolicy policies : response.attachedPolicies()) {
            client.detachRolePolicy(r -> r.policyArn(policies.policyArn()).roleName(getName()));
        }

        client.deleteRole(r -> r.roleName(getName()));
    }

    private Role getRole(IamClient client) {
        try {
            GetRoleResponse response = client.getRole(r -> r.roleName(getName()));

            return response.role();
        } catch (NoSuchEntityException ex) {
            return null;
        }
    }

    private static Role getRole(List<Role> roles, String name) {
        return roles.stream()
            .filter(r -> r.roleName().equals(name))
            .findFirst()
            .orElse(null);
    }

    private static List<Role> getRoles(IamClient client) {
        try {
            return client.listRolesPaginator().roles().stream().collect(Collectors.toList());
        } catch (NoSuchEntityException ex) {
            return new ArrayList<>();
        }
    }

    private List<Tag> toTags(Map<String, String> tag) {
        List<Tag> tags = new ArrayList<>();

        tag.entrySet().forEach(r -> tags.add(Tag.builder().key(r.getKey()).value(r.getValue()).build()));

        return tags;
    }
}
