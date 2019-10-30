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

package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;

import java.util.List;
import java.util.Map;

/**
 * Query key pair.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    key-pair: $(external-query aws::key-pair { key-name: ''})
 */
@Type("key-pair")
public class KeyPairFinder extends AwsFinder<Ec2Client, KeyPairInfo, KeyPairResource> {
    private String fingerprint;
    private String keyName;

    /**
     * The fingerprint of the key pair.
     */
    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    /**
     * The key name of the key pair.
     */
    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    @Override
    protected List<KeyPairInfo> findAllAws(Ec2Client client) {
        return client.describeKeyPairs().keyPairs();
    }

    @Override
    protected List<KeyPairInfo> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeKeyPairs(r -> r.filters(createFilters(filters))).keyPairs();
    }
}
