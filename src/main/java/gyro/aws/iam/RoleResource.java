package gyro.aws.iam;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.Type;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AttachedPolicy;
import software.amazon.awssdk.services.iam.model.CreateRoleResponse;
import software.amazon.awssdk.services.iam.model.ListAttachedRolePoliciesResponse;
import software.amazon.awssdk.services.iam.model.Role;
import software.amazon.awssdk.services.iam.model.Tag;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a role resource.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::role example-role
 *         name: "rta-test-role"
 *         description: "testing the role functionality"
 *         assume-role-policy: "role_example.json"
 *         policies: [(aws::policy policy)]
 *     end
 */
@Type("role")
public class RoleResource extends AwsResource implements Copyable<Role> {

    private String arn;
    private String assumeRolePolicy;
    private String description;
    private List<PolicyResource> policies;
    private Integer maxSessionDuration;
    private String name;
    private String path;
    private String permissionsBoundaryArn;
    private Map<String, String> tags;

    @Output
    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The assumed role policy. (Required)
     */
    @Updatable
    public String getAssumeRolePolicy() {
        if (this.assumeRolePolicy != null && this.assumeRolePolicy.contains(".json")) {
            try (InputStream input = openInput(this.assumeRolePolicy)) {
                this.assumeRolePolicy = formatPolicy(IoUtils.toUtf8String(input));
                return this.assumeRolePolicy;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return this.assumeRolePolicy;
        }
    }

    public void setAssumeRolePolicy(String assumeRolePolicy) {
        this.assumeRolePolicy = assumeRolePolicy;
    }

    /**
     * The description of the role. (Optional)
     */
    @Updatable
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The policies associated with the role. (Optional)
     */
    @Updatable
    public List<PolicyResource> getPolicies() {
        if (policies == null) {
            policies = new ArrayList<>();
        }

        return policies;
    }

    public void setPolicies(List<PolicyResource> policies) {
        this.policies = policies;
    }

    /**
     * The maximum duration of the role, in seconds. Valid values are between 3600 and 43200. (Optional)
     */
    @Updatable
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
     * The name of the role. (Required)
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The path to the role. Defaults to ``/``. (Optional)
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
     * The arn of the permission boundary. (Optional)
     */
    public String getPermissionsBoundaryArn() {
        return permissionsBoundaryArn;
    }

    public void setPermissionsBoundaryArn(String permissionsBoundaryArn) {
        this.permissionsBoundaryArn = permissionsBoundaryArn;
    }

    /**
     * The tags associated with the role. (Optional)
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

    @Override
    public void copyFrom(Role role) {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        setArn(role.arn());
        setName(role.roleName());
        setDescription(role.description());
        String encode = URLDecoder.decode(role.assumeRolePolicyDocument());
        setAssumeRolePolicy(formatPolicy(encode));
        setMaxSessionDuration(role.maxSessionDuration());
        setPath(role.path());
        setPermissionsBoundaryArn(role.permissionsBoundary() != null ? role.permissionsBoundary().permissionsBoundaryArn() : null);
        getTags().entrySet().forEach(r -> getTags().put(r.getKey(), r.getValue()));

        getPolicies().clear();
        ListAttachedRolePoliciesResponse policyResponse = client.listAttachedRolePolicies(r -> r.roleName(getName()));
        for (AttachedPolicy attachedPolicy: policyResponse.attachedPolicies()) {
            getPolicies().add(findById(PolicyResource.class, attachedPolicy.policyArn()));
        }
    }

    @Override
    public boolean refresh() {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        Role role = client.getRole(r -> r.roleName(getName())).role();

        if (role != null) {
            this.copyFrom(role);

            return true;
        }

        return false;
    }

    @Override
    public void create() {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

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
            delete();
            throw new GyroException(err.getMessage());
        }
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        client.updateAssumeRolePolicy(r -> r.policyDocument(formatPolicy(getAssumeRolePolicy()))
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

        client.tagRole(r -> r.roleName(getName()).tags(toTags(getTags())));
    }

    @Override
    public void delete() {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        ListAttachedRolePoliciesResponse response = client.listAttachedRolePolicies(r -> r.roleName(getName()));
        for (AttachedPolicy policies : response.attachedPolicies()) {
            client.detachRolePolicy(r -> r.policyArn(policies.policyArn()).roleName(getName()));
        }

        client.deleteRole(r -> r.roleName(getName()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getName() != null) {
            sb.append("role " + getName());

        } else {
            sb.append("role ");
        }

        return sb.toString();
    }

    public String formatPolicy(String policy) {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ").replaceAll("\t", " ").trim().replaceAll(" ", "") : policy;
    }

    private List<Tag> toTags(Map<String, String> tag) {
        List<Tag> tags = new ArrayList<>();

        tag.entrySet().forEach(r -> tags.add(Tag.builder().key(r.getKey()).value(r.getValue()).build()));

        return tags;
    }
}
