package gyro.aws.iam;

import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.Type;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreatePolicyResponse;
import software.amazon.awssdk.services.iam.model.GetPolicyResponse;
import software.amazon.awssdk.services.iam.model.GetPolicyVersionResponse;
import software.amazon.awssdk.services.iam.model.Policy;
import software.amazon.awssdk.services.iam.model.PolicyVersion;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Set;

/**
 * Creates a Policy with the specified options.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::policy example-role
 *         name: "rta-test-policy"
 *         description: "testing the policy functionality"
 *         policy-document: "policyFile.json"
 *         role: (aws::role role)
 *     end
 */
@Type("policy")
public class PolicyResource extends AwsResource {

    private String arn;
    private String description;
    private String name;
    private String pastVersionId;
    private String path;
    private String policyDocument;
    private RoleResource role;

    @Output
    @Id
    public String getArn() {
        return this.arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The description of the role. (Optional)
     */
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name of the policy. (Required)
     */
    @Updatable
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Output
    public String getPastVersionId() {
        return this.pastVersionId;
    }

    public void setPastVersionId(String pastVersionId) {
        this.pastVersionId = pastVersionId;
    }

    /**
     * The path for the policy. (Optional)
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    /**
     * The role the policy is attached to. (Optional)
     */
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    @Override
    public boolean refresh() {
        IamClient client = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .build();

        GetPolicyResponse response = client.getPolicy(
            r -> r.policyArn(getArn())
        );

        Policy policy = response.policy();

        if (policy != null) {
            setName(policy.policyName());
            setDescription(policy.description());
            setArn(policy.arn());

            for (PolicyVersion versions : client.listPolicyVersions(r -> r.policyArn(getArn())).versions()) {
                setPastVersionId(versions.versionId());
            }

            GetPolicyVersionResponse versionResponse = client.getPolicyVersion(
                r -> r.versionId(getPastVersionId())
                            .policyArn(getArn())
            );

            String encode = URLDecoder.decode(versionResponse.policyVersion().document());
            setPolicyDocument(formatPolicy(encode));

            return true;
        }

        return false;
    }

    @Override
    public void create() {
        IamClient client = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .build();

        CreatePolicyResponse response = client.createPolicy(
            r -> r.policyName(getName())
                        .policyDocument(getPolicyDocument())
                        .description(getDescription())
                        .path(getPath())
        );

        setArn(response.policy().arn());

        if (getRole() != null) {
            client.attachRolePolicy(r -> r.roleName(getRole().getName())
                    .policyArn(getArn()));
        }
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        IamClient client = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .build();

        PolicyResource currentResource = (PolicyResource) current;

        for (PolicyVersion versions : client.listPolicyVersions(r -> r.policyArn(getArn())).versions()) {
            setPastVersionId(versions.versionId());
        }

        client.createPolicyVersion(
            r -> r.policyArn(getArn())
                    .policyDocument(getPolicyDocument())
                    .setAsDefault(true)
        );

        client.deletePolicyVersion(
            r -> r.policyArn(getArn())
                        .versionId(getPastVersionId())
        );

        if (getRole() != null) {
            client.attachRolePolicy(r -> r.roleName(getRole().getName())
                    .policyArn(getArn()));
        }

        if (currentResource.getRole() != null && getRole() == null) {
            client.detachRolePolicy(r -> r.roleName(getName())
                    .policyArn(getArn()));
        }
    }

    @Override
    public void delete() {
        IamClient client = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .build();

        client.deletePolicy(r -> r.policyArn(this.getArn()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getName() != null) {
            sb.append("policy name " + getName());

        } else {
            sb.append("policy name ");
        }

        return sb.toString();
    }

    public String formatPolicy(String policy) {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ").replaceAll("\t", " ").trim().replaceAll(" ", "") : policy;
    }
}
