package gyro.aws.acmpca;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
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
     * Actions associated with the permission. Valid values are ``IssueCertificate`` and ``GetCertificate`` and ``ListPermissions``. (Required)
     */
    @Updatable
    public Set<String> getActions() {
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    /**
     * The AWS service or entity that holds the permission. Currently only supported value ``acm.amazonaws.com``. Defaults to ``acm.amazonaws.com``.
     */
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
