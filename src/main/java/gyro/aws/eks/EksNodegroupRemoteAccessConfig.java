package gyro.aws.eks;

import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.aws.ec2.KeyPairResource;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.eks.model.RemoteAccessConfig;

public class EksNodegroupRemoteAccessConfig extends Diffable implements Copyable<RemoteAccessConfig> {

    private KeyPairResource ec2SshKey;
    private List<SecurityGroupResource> sourceSecurityGroups;

    public KeyPairResource getEc2SshKey() {
        return ec2SshKey;
    }

    public void setEc2SshKey(KeyPairResource ec2SshKey) {
        this.ec2SshKey = ec2SshKey;
    }

    public List<SecurityGroupResource> getSourceSecurityGroups() {
        return sourceSecurityGroups;
    }

    public void setSourceSecurityGroups(List<SecurityGroupResource> sourceSecurityGroups) {
        this.sourceSecurityGroups = sourceSecurityGroups;
    }

    @Override
    public void copyFrom(RemoteAccessConfig model) {
        setEc2SshKey(findById(KeyPairResource.class, model.ec2SshKey()));
        setSourceSecurityGroups(model.sourceSecurityGroups()
            .stream()
            .map(s -> findById(SecurityGroupResource.class, s))
            .collect(Collectors.toList()));
    }

    @Override
    public String primaryKey() {
        return null;
    }

    RemoteAccessConfig toRemoteAccessConfig() {
        return RemoteAccessConfig.builder()
            .ec2SshKey(getEc2SshKey().getName())
            .sourceSecurityGroups(getSourceSecurityGroups().stream()
                .map(SecurityGroupResource::getId)
                .collect(Collectors.toList()))
            .build();
    }
}
