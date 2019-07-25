package gyro.aws.acmpca;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
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
import software.amazon.awssdk.services.acmpca.AcmPcaClient;
import software.amazon.awssdk.services.acmpca.model.CertificateAuthority;
import software.amazon.awssdk.services.acmpca.model.CertificateAuthorityStatus;
import software.amazon.awssdk.services.acmpca.model.CertificateAuthorityType;
import software.amazon.awssdk.services.acmpca.model.CreateCertificateAuthorityRequest;
import software.amazon.awssdk.services.acmpca.model.CreateCertificateAuthorityResponse;
import software.amazon.awssdk.services.acmpca.model.DescribeCertificateAuthorityResponse;
import software.amazon.awssdk.services.acmpca.model.InvalidStateException;
import software.amazon.awssdk.services.acmpca.model.ListPermissionsResponse;
import software.amazon.awssdk.services.acmpca.model.ResourceNotFoundException;
import software.amazon.awssdk.services.acmpca.model.Tag;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a ACM PCA Certificate Authority.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::certificate-authority certificate-authority-example
 *         type: "ROOT"
 *         configuration
 *             key-algorithm: "RSA_2048"
 *             signing-algorithm: "SHA256WITHRSA"
 *             subject
 *                 country: "US"
 *                 organization: "Gyro LLC"
 *                 organizational-unit: "Dev"
 *                 state: "Virginia"
 *                 common-name: "VA R2"
 *                 locality: "Reston"
 *             end
 *         end
 *
 *         permission
 *             actions: [
 *                 "IssueCertificate",
 *                 "GetCertificate",
 *                 "ListPermissions"
 *             ]
 *             principal: "acm.amazonaws.com"
 *         end
 *     end
 */
@Type("certificate-authority")
public class AcmPcaCertificateAuthority extends AwsResource implements Copyable<CertificateAuthority> {
    private AcmPcaCertificateAuthorityConfiguration configuration;
    private CertificateAuthorityType type;
    private AcmPcaRevocationConfiguration revocationConfiguration;
    private Map<String, String> tags;
    private AcmPcaPermission permission;
    private Boolean enabled;

    // --Output
    private String arn;
    private String serial;
    private String failureReason;
    private Date createdAt;
    private Date lastStateChangeAt;
    private Date notAfter;
    private Date notBefore;
    private CertificateAuthorityStatus status;

    /**
     * The configuration setting for the Certificate Authority. (Required)
     */
    public AcmPcaCertificateAuthorityConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(AcmPcaCertificateAuthorityConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * The type of Certificate Authority. Valid values are ``ROOT`` or ``SUBORDINATE``. (Required)
     */
    public CertificateAuthorityType getType() {
        return type;
    }

    public void setType(CertificateAuthorityType type) {
        this.type = type;
    }

    /**
     * The revocation configuration for the Certificate Authority.
     */
    public AcmPcaRevocationConfiguration getRevocationConfiguration() {
        if (revocationConfiguration == null) {
            revocationConfiguration = newSubresource(AcmPcaRevocationConfiguration.class);
        }

        return revocationConfiguration;
    }

    public void setRevocationConfiguration(AcmPcaRevocationConfiguration revocationConfiguration) {
        this.revocationConfiguration = revocationConfiguration;
    }

    /**
     * Tags for the Certificate Authority.
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
     * The permission setting for the Certificate Authority.
     */
    @Updatable
    public AcmPcaPermission getPermission() {
        return permission;
    }

    public void setPermission(AcmPcaPermission permission) {
        this.permission = permission;
    }

    /**
     * Enable or Disable the Certificate Authority. Defaults to enabled.
     */
    @Updatable
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = true;
        }

        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The ARN for the Certificate Authority.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The Serial for the Certificate Authority.
     */
    @Output
    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    /**
     * The failure reason for the Certificate Authority.
     */
    @Output
    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    /**
     * Creation time for the Certificate Authority.
     */
    @Output
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Last update time for the Certificate Authority.
     */
    @Output
    public Date getLastStateChangeAt() {
        return lastStateChangeAt;
    }

    public void setLastStateChangeAt(Date lastStateChangeAt) {
        this.lastStateChangeAt = lastStateChangeAt;
    }

    /**
     * Date and time after which the Certificate Authority is not valid.
     */
    @Output
    public Date getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
    }

    /**
     * Date and time before which the Certificate Authority is not valid.
     */
    @Output
    public Date getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    /**
     * The status of the Certificate Authority.
     */
    @Output
    public CertificateAuthorityStatus getStatus() {
        return status;
    }

    public void setStatus(CertificateAuthorityStatus status) {
        this.status = status;
    }

    @Override
    public void copyFrom(CertificateAuthority certificateAuthority) {
        setType(certificateAuthority.type());
        AcmPcaCertificateAuthorityConfiguration certificateAuthorityConfiguration = newSubresource(AcmPcaCertificateAuthorityConfiguration.class);
        certificateAuthorityConfiguration.copyFrom(certificateAuthority.certificateAuthorityConfiguration());
        setConfiguration(certificateAuthorityConfiguration);

        AcmPcaRevocationConfiguration revocationConfiguration = newSubresource(AcmPcaRevocationConfiguration.class);
        revocationConfiguration.copyFrom(certificateAuthority.revocationConfiguration());
        setRevocationConfiguration(revocationConfiguration);

        setArn(certificateAuthority.arn());
        setCreatedAt(certificateAuthority.createdAt() != null ? Date.from(certificateAuthority.createdAt()) : null);
        setFailureReason(certificateAuthority.failureReasonAsString());
        setLastStateChangeAt(certificateAuthority.lastStateChangeAt() != null ? Date.from(certificateAuthority.lastStateChangeAt()) : null);
        setNotAfter(certificateAuthority.notAfter() != null ? Date.from(certificateAuthority.notAfter()) : null);
        setNotBefore(certificateAuthority.notBefore() != null ? Date.from(certificateAuthority.notBefore()) : null);
        setStatus(certificateAuthority.status());
        setSerial(certificateAuthority.serial());
        setEnabled(!getStatus().equals(CertificateAuthorityStatus.DISABLED));

        AcmPcaClient client = createClient(AcmPcaClient.class);

        ListPermissionsResponse response = client.listPermissions(r -> r.certificateAuthorityArn(getArn()));
        if (!response.permissions().isEmpty()) {
            AcmPcaPermission permission = newSubresource(AcmPcaPermission.class);
            permission.copyFrom(response.permissions().get(0));
            setPermission(permission);
        } else {
            setPermission(null);
        }
    }

    @Override
    public boolean refresh() {
        AcmPcaClient client = createClient(AcmPcaClient.class);

        try {
            DescribeCertificateAuthorityResponse response = client.describeCertificateAuthority(r -> r.certificateAuthorityArn(getArn()));

            copyFrom(response.certificateAuthority());

            return true;
        } catch (ResourceNotFoundException ex) {
            return false;
        }
    }

    @Override
    public void create(GyroUI ui, State state) {
        AcmPcaClient client = createClient(AcmPcaClient.class);

        CreateCertificateAuthorityRequest.Builder builder = CreateCertificateAuthorityRequest.builder()
            .certificateAuthorityConfiguration(getConfiguration().toCertificateAuthorityConfiguration())
            .certificateAuthorityType(getType())
            .revocationConfiguration(getRevocationConfiguration().toRevocationConfiguration());

        if (!getTags().isEmpty()) {
            builder.tags(toTags());
        }

        CreateCertificateAuthorityResponse response = client.createCertificateAuthority(builder.build());
        setArn(response.certificateAuthorityArn());
        state.save();

        if (!getEnabled()) {
            client.updateCertificateAuthority(
                r -> r.certificateAuthorityArn(getArn())
                    .revocationConfiguration(getRevocationConfiguration()
                        .toRevocationConfiguration())
                    .status(CertificateAuthorityStatus.DISABLED)
            );
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AcmPcaClient client = createClient(AcmPcaClient.class);

        if (changedFieldNames.contains("revocation-configuration") || changedFieldNames.contains("enabled")) {

            client.updateCertificateAuthority(
                r -> r.certificateAuthorityArn(getArn())
                    .revocationConfiguration(getRevocationConfiguration()
                        .toRevocationConfiguration())
                    .status(getEnabled() ? CertificateAuthorityStatus.ACTIVE : CertificateAuthorityStatus.DISABLED)
            );
        }

        if (changedFieldNames.contains("tags")) {
            Map<String, String> oldTags = ((AcmPcaCertificateAuthority) current).getTags();
            MapDifference<String,String> diff = Maps.difference(getTags(), oldTags);

            List<Tag> deleteTags = new ArrayList<>();
            List<Tag> addTags = new ArrayList<>();

            if (!diff.entriesOnlyOnRight().isEmpty()) {
                deleteTags.addAll(diff.entriesOnlyOnRight().keySet().stream()
                    .map(o -> Tag.builder().key(o).value(diff.entriesOnlyOnRight().get(o)).build())
                    .collect(Collectors.toList()));
            }

            if (!diff.entriesDiffering().isEmpty()) {
                deleteTags.addAll(diff.entriesDiffering().keySet().stream()
                    .map(o -> Tag.builder().key(o).value(diff.entriesDiffering().get(o).rightValue()).build())
                    .collect(Collectors.toList()));

                addTags.addAll(diff.entriesDiffering().keySet().stream()
                    .map(o -> Tag.builder().key(o).value(diff.entriesDiffering().get(o).leftValue()).build())
                    .collect(Collectors.toList()));
            }

            if (!diff.entriesOnlyOnLeft().isEmpty()) {
                addTags.addAll(diff.entriesOnlyOnLeft().keySet().stream()
                    .map(o -> Tag.builder().key(o).value(diff.entriesOnlyOnLeft().get(o)).build())
                    .collect(Collectors.toList()));
            }

            if (!deleteTags.isEmpty()) {
                client.untagCertificateAuthority(r -> r.certificateAuthorityArn(getArn()).tags(deleteTags));
            }

            if (!addTags.isEmpty()) {
                client.tagCertificateAuthority(r -> r.certificateAuthorityArn(getArn()).tags(addTags));
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AcmPcaClient client = createClient(AcmPcaClient.class);

        try {
            client.deleteCertificateAuthority(r -> r.certificateAuthorityArn(getArn()));
        } catch (InvalidStateException ex) {
            throw new GyroException(String.format("Certificate Authority - %s cannot be deleted until disabled", getArn()));
        }
    }

    private List<Tag> toTags() {
        return getTags().keySet().stream().map(o -> Tag.builder().key(o).value(getTags().get(o)).build()).collect(Collectors.toList());
    }
}
