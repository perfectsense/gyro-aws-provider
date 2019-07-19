package gyro.aws.acm;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.acm.model.CertificateOptions;
import software.amazon.awssdk.services.acm.model.CertificateTransparencyLoggingPreference;

public class AcmCertificateOptions extends Diffable implements Copyable<CertificateOptions> {
    private CertificateTransparencyLoggingPreference preference;

    /**
     * Enable or Disable certificate transparency logging. Valid values ``ENABLED`` or ``DISABLED``. Defaults to ``DISABLED``.
     */
    public CertificateTransparencyLoggingPreference getPreference() {
        if (preference == null) {
            preference = CertificateTransparencyLoggingPreference.DISABLED;
        }

        return preference;
    }

    public void setPreference(CertificateTransparencyLoggingPreference preference) {
        this.preference = preference;
    }

    @Override
    public void copyFrom(CertificateOptions certificateOptions) {
        setPreference(certificateOptions.certificateTransparencyLoggingPreference());
    }

    @Override
    public String primaryKey() {
        return "certificate options";
    }

    CertificateOptions toCertificateOptions() {
        return CertificateOptions.builder()
            .certificateTransparencyLoggingPreference(getPreference())
            .build();
    }
}
