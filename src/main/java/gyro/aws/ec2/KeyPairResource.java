package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.ImportKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

/**
 * Creates a key pair using the public key provided.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::key-pair key-pair-example
 *         name: "key-pair-example"
 *         public-key-path: "example-public-key.pub"
 *     end
 *
 * .. code-block:: gyro
 *
 *     aws::key-pair key-pair-example
 *         name: "key-pair-example"
 *         public-key: ".."
 *     end
 */
@Type("key-pair")
public class KeyPairResource extends AwsResource implements Copyable<KeyPairInfo> {

    private String name;
    private String publicKeyPath;
    private String publicKey;
    private String keyFingerPrint;

    /**
     * The key name that you want to assign for your key pair. See `Amazon EC2 Key Pairs <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html/>`_. (Required)
     */
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The file path that contains the public key needed to generate the key pair. See `Importing Your Own Public Key to Amazon EC2 <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html#how-to-generate-your-own-key-and-import-it-to-aws/>`_. (Required)
     */
    public String getPublicKeyPath() {
        return publicKeyPath;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }

    /**
     * The public key needed to generate the key pair. See `Importing Your Own Public Key to Amazon EC2 <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html#how-to-generate-your-own-key-and-import-it-to-aws/>`_. (Required)
     */
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * The fingerprint for the key pair.
     */
    @Output
    public String getKeyFingerPrint() {
        return keyFingerPrint;
    }

    public void setKeyFingerPrint(String keyFingerPrint) {
        this.keyFingerPrint = keyFingerPrint;
    }

    @Override
    public void copyFrom(KeyPairInfo keyPairInfo) {
        setName(keyPairInfo.keyName());
        setKeyFingerPrint(keyPairInfo.keyFingerprint());
    }

    @Override
    public boolean refresh() {
        Ec2Client client = createClient(Ec2Client.class);

        KeyPairInfo keyPairInfo = getKeyPairInfo(client);

        if (keyPairInfo == null) {
            return false;
        }

        copyFrom(keyPairInfo);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        if (ObjectUtils.isBlank(getPublicKey())) {
            setPublicKey(getPublicKeyFromPath());
        }

        Ec2Client client = createClient(Ec2Client.class);

        ImportKeyPairResponse response = client.importKeyPair(
            r -> r.keyName(getName())
                .publicKeyMaterial(SdkBytes.fromByteArray(getPublicKey().getBytes()))
        );

        setKeyFingerPrint(response.keyFingerprint());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteKeyPair(r -> r.keyName(getName()));
    }

    private String getPublicKeyFromPath() {
        try (InputStream input = openInput(getPublicKeyPath())) {
            return IoUtils.toUtf8String(input);

        } catch (IOException ioex) {
            throw new GyroException("Unable to read public key from file.");
        }
    }

    private KeyPairInfo getKeyPairInfo(Ec2Client client) {
        KeyPairInfo keyPairInfo = null;

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load key pair.");
        }

        try {
            DescribeKeyPairsResponse response = client.describeKeyPairs(r -> r.keyNames(Collections.singleton(getName())));

            if (!response.keyPairs().isEmpty()) {
                keyPairInfo = response.keyPairs().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return keyPairInfo;
    }
}
