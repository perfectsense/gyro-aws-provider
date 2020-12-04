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

package gyro.aws;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.FileBackend;
import gyro.core.GyroCore;
import gyro.core.Type;
import gyro.core.auth.Credentials;
import gyro.core.auth.CredentialsSettings;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Type("s3")
public class S3FileBackend extends FileBackend {

    private String bucket;
    private String prefix;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Stream<String> list() throws Exception {
        if (this.equals(GyroCore.getStateBackend(getName()))) {
            return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new S3ObjectIterator(getBucket(), prefixed(""), client()), 0),
                false)
                .map(S3Object::key)
                .filter(f -> f.endsWith(".gyro"))
                .map(this::removePrefix);
        }

        return Stream.empty();
    }

    @Override
    public InputStream openInput(String file) throws Exception {
        return client().getObject(r -> r.bucket(getBucket()).key(prefixed(file)));
    }

    @Override
    public OutputStream openOutput(String file) throws Exception {
        return new ByteArrayOutputStream() {

            public void close() {
                upload(getBucket(), prefixed(file), RequestBody.fromBytes(toByteArray()));
            }
        };
    }

    @Override
    public void delete(String file) throws Exception {
        client().deleteObject(r -> r.bucket(getBucket()).key(prefixed(file)));
    }

    @Override
    public boolean exists(String file) throws Exception {
        try {
            client().headObject(r -> r.bucket(bucket).key(prefixed(file)));
        } catch (NoSuchKeyException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void copy(String source, String destination) throws Exception {
        String bucket = getBucket();
        client().copyObject(r -> r
            .copySource(bucket + "/" + prefixed(source))
            .destinationBucket(bucket)
            .destinationKey(prefixed(destination))
            .acl(ObjectCannedACL.PRIVATE));
    }

    private void upload(String bucket, String path, RequestBody body) {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(path)
            .acl(ObjectCannedACL.PRIVATE)
            .build();

        client().putObject(request, body);
    }

    private S3Client client() {
        Credentials credentials;

        try {
            Method getCredentials = FileBackend.class.getDeclaredMethod("getCredentials", String.class);
            credentials = (Credentials) getCredentials.invoke(this, "aws");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            // Older gyro being used

            CredentialsSettings settings = getRootScope().getSettings(CredentialsSettings.class);
            String credentialName = settings.getUseCredentials();

            if (ObjectUtils.isBlank(credentialName)) {
                credentialName = "default";
            }

            credentials = settings
                .getCredentialsByName()
                .get(String.format("%s::%s", "aws", credentialName));
        }

        return AwsResource.createClient(S3Client.class, (AwsCredentials) credentials);
    }

    private String prefixed(String file) {
        return getPrefix() != null ? getPrefix() + '/' + file : file;
    }

    private String removePrefix(String file) {
        if (getPrefix() != null && file.startsWith(getPrefix() + "/")) {
            return file.substring(getPrefix().length() + 1);
        }

        return file;
    }
}
