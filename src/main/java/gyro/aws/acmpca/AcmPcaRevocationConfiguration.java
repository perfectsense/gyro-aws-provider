package gyro.aws.acmpca;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.acmpca.model.RevocationConfiguration;

public class AcmPcaRevocationConfiguration extends Diffable implements Copyable<RevocationConfiguration> {
    private AcmPcaCrlConfiguration crlConfiguration;

    /**
     * The Crl configuration.
     */
    @Updatable
    public AcmPcaCrlConfiguration getCrlConfiguration() {
        if (crlConfiguration == null) {
            crlConfiguration = newSubresource(AcmPcaCrlConfiguration.class);
        }

        return crlConfiguration;
    }

    public void setCrlConfiguration(AcmPcaCrlConfiguration crlConfiguration) {
        this.crlConfiguration = crlConfiguration;
    }

    @Override
    public void copyFrom(RevocationConfiguration revocationConfiguration) {
        AcmPcaCrlConfiguration crlConfiguration = newSubresource(AcmPcaCrlConfiguration.class);
        crlConfiguration.copyFrom(revocationConfiguration.crlConfiguration());
        setCrlConfiguration(crlConfiguration);
    }

    @Override
    public String primaryKey() {
        return "revocation configuration";
    }

    RevocationConfiguration toRevocationConfiguration() {
        return RevocationConfiguration.builder()
            .crlConfiguration(getCrlConfiguration().toCrlConfiguration())
            .build();
    }
}
