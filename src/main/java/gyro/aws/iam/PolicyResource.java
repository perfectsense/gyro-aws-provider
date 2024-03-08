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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.List;
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreatePolicyResponse;
import software.amazon.awssdk.services.iam.model.GetPolicyResponse;
import software.amazon.awssdk.services.iam.model.GetPolicyVersionResponse;
import software.amazon.awssdk.services.iam.model.LimitExceededException;
import software.amazon.awssdk.services.iam.model.NoSuchEntityException;
import software.amazon.awssdk.services.iam.model.Policy;
import software.amazon.awssdk.services.iam.model.PolicyVersion;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Creates an IAM Policy with the specified options.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::iam-policy example-role
 *         name: "rta-test-policy"
 *         description: "testing the policy functionality"
 *         policy-document: "policyFile.json"
 *         role: (aws::iam-role role)
 *     end
 */
@Type("iam-policy")
public class PolicyResource extends AwsResource implements Copyable<Policy> {

    private String arn;
    private String description;
    private String name;
    private String pastVersionId;
    private String path;
    private String policyDocument;

    /**
     * The arn of the policy.
     */
    @Output
    @Id
    public String getArn() {
        return this.arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The description of the role.
     */
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name of the policy.
     */
    @Required
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The past version id.
     */
    @Output
    public String getPastVersionId() {
        return this.pastVersionId;
    }

    public void setPastVersionId(String pastVersionId) {
        this.pastVersionId = pastVersionId;
    }

    /**
     * The path for the policy.
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * The policy document. A policy path or policy string is allowed.
     */
    @Required
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
    public void copyFrom(Policy policy) {
        IamClient client = createClient(IamClient.class);

        setName(policy.policyName());
        setDescription(policy.description());
        setArn(policy.arn());

        for (PolicyVersion versions : client.listPolicyVersions(r -> r.policyArn(getArn())).versions()) {
            setPastVersionId(versions.versionId());
        }

        GetPolicyVersionResponse versionResponse = client.getPolicyVersion(
            r -> r.versionId(policy.defaultVersionId())
                .policyArn(getArn())
        );

        String encode = URLDecoder.decode(versionResponse.policyVersion().document());
        setPolicyDocument(formatPolicy(encode));
    }

    @Override
    public boolean refresh() {
        IamClient client = createClient(IamClient.class);

        Policy policy = getPolicy(client);

        if (policy != null) {
            copyFrom(policy);

            return true;
        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        IamClient client = createClient(IamClient.class);

        CreatePolicyResponse response = client.createPolicy(
            r -> r.policyName(getName())
                        .policyDocument(getPolicyDocument())
                        .description(getDescription())
                        .path(getPath())
        );

        setArn(response.policy().arn());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        IamClient client = createClient(IamClient.class);

        List<PolicyVersion> policyVersions = client.listPolicyVersions(r -> r.policyArn(getArn())).versions();

        String policyVersionToDelete = "";
        for (PolicyVersion policyVersion : policyVersions) {
            setPastVersionId(policyVersion.versionId());

            if (!policyVersion.isDefaultVersion()) {
                policyVersionToDelete = policyVersion.versionId();
            }
        }

        if (StringUtils.isBlank(policyVersionToDelete)) {
            policyVersionToDelete = getPastVersionId();
        }

        // policy version creation done first, before version deletion
        // to ensure policy version deletion does not happen if a new version cannot be created
        boolean limitExceeded = false;
        try {
            createPolicyVersion(client);
        } catch (LimitExceededException ex) {
            limitExceeded = true;
        }

        if (limitExceeded) {
            deletePolicyVersion(client, policyVersionToDelete);
            createPolicyVersion(client);
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        IamClient client = createClient(IamClient.class);

        List<PolicyVersion> policyVersions = client.listPolicyVersions(r -> r.policyArn(getArn())).versions();

        for (PolicyVersion version : policyVersions) {
            if (!version.isDefaultVersion()) {
                deletePolicyVersion(client, version.versionId());
            }
        }

        client.deletePolicy(r -> r.policyArn(this.getArn()));
    }

    public static String formatPolicy(String policy) {
        String formattedPolicy = null;

        if (policy != null) {
            boolean quoted = false;
            StringBuilder out = new StringBuilder();

            for (Character c : policy.toCharArray()) {
                if (c == '"') {
                    quoted = !quoted;
                }

                if (c != System.lineSeparator().charAt(0) && c != '\t') {
                    if (c != ' ' || quoted) {
                        out.append(c);
                    }
                }
            }
            formattedPolicy = out.toString();
        }

        return formattedPolicy;
    }

    private Policy getPolicy(IamClient client) {
        try {
            GetPolicyResponse response = client.getPolicy(
                r -> r.policyArn(getArn())
            );

            return response.policy();
        } catch (NoSuchEntityException ex) {
            return null;
        }
    }

    private void createPolicyVersion(IamClient client) {
        client.createPolicyVersion(
            r -> r.policyArn(getArn())
                .policyDocument(getPolicyDocument())
                .setAsDefault(true)
        );
    }

    private void deletePolicyVersion(IamClient client, String versionId) {
        client.deletePolicyVersion(
            r -> r.policyArn(getArn())
                .versionId(versionId)
        );
    }
}
