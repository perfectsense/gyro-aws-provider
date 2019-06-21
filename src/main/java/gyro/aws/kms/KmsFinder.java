package gyro.aws.kms;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.KeyListEntry;
import software.amazon.awssdk.services.kms.model.KeyMetadata;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query kms key.
 *
 * .. code-block:: gyro
 *
 *    keys: $(aws::kms EXTERNAL/* | key-id = '')
 */
@Type("kms")
public class KmsFinder extends AwsFinder<KmsClient, KeyMetadata, KmsResource> {

    private String keyId;

    /**
     * The id associated with the key.
     */
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    @Override
    protected List<KeyMetadata> findAws(KmsClient client, Map<String, String> filters) {
        if (!filters.containsKey("key-id")) {
            throw new IllegalArgumentException("'key-id' is required.");
        }

        return Collections.singletonList(client.describeKey(r -> r.keyId(filters.get("key-id"))).keyMetadata());
    }

    @Override
    protected List<KeyMetadata> findAllAws(KmsClient client) {
        return client.listKeysPaginator().keys()
            .stream()
            .map(KeyListEntry::keyId)
            .map(oo -> client.describeKey(s -> s.keyId(oo)).keyMetadata())
            .collect(Collectors.toList());
    }
}
