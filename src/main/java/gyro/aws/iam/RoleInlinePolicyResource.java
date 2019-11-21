package gyro.aws.iam;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.GetRolePolicyResponse;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class RoleInlinePolicyResource extends AwsResource implements Copyable<GetRolePolicyResponse> {
    private String name;
    private String policyDocument;

    /**
     * The name of the policy. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The policy document. A policy path or policy string is allowed. (Required)
     */
    @Updatable
    public String getPolicyDocument() {
        if (this.policyDocument != null && this.policyDocument.contains(".json")) {
            try (InputStream input = openInput(this.policyDocument)) {
                this.policyDocument = formatPolicy(IoUtils.toUtf8String(input));
                return this.policyDocument;
            } catch (IOException err) {
                throw new GyroException(err.getMessage());
            }
        } else {
            return this.policyDocument;
        }
    }

    public void setPolicyDocument(String policyDocument) {
        this.policyDocument = policyDocument;
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    @Override
    public void copyFrom(GetRolePolicyResponse policy) {
        setName(policy.policyName());
        setPolicyDocument(policy.policyDocument());
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        RoleResource roleResource = (RoleResource) parent();

        client.putRolePolicy(r -> r.roleName(roleResource.getName()).policyName(getName()).policyDocument(getPolicyDocument()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        RoleResource roleResource = (RoleResource) parent();

        client.putRolePolicy(r -> r.roleName(roleResource.getName()).policyName(getName()).policyDocument(getPolicyDocument()));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        IamClient client = createClient(IamClient.class, "aws-global", "https://iam.amazonaws.com");

        RoleResource roleResource = (RoleResource) parent();

        client.deleteRolePolicy(r -> r.roleName(roleResource.getName()).policyName(getName()));
    }

    private String formatPolicy(String policy) {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ").replaceAll("\t", " ").trim().replaceAll(" ", "") : policy;
    }
}
