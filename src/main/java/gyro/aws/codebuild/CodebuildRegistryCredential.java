package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.RegistryCredential;

public class CodebuildRegistryCredential extends Diffable implements Copyable<RegistryCredential> {

    private String credential;
    private String credentialProvider;

    @Required
    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    @Required
    @ValidStrings("SECRETS_MANAGER")
    public String getCredentialProvider() {
        return credentialProvider;
    }

    public void setCredentialProvider(String credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    @Override
    public void copyFrom(RegistryCredential model) {
        setCredential(model.credential());
        setCredentialProvider(model.credentialProviderAsString());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public RegistryCredential toRegistryCredential() {
        return RegistryCredential.builder()
            .credential(getCredential())
            .credentialProvider(getCredentialProvider())
            .build();
    }
}
