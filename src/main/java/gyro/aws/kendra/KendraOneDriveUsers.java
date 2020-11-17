/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.kendra;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.kendra.model.OneDriveUsers;

public class KendraOneDriveUsers extends Diffable implements Copyable<OneDriveUsers> {

    private List<String> userList;
    private KendraS3Path path;

    /**
     * The list of users whose documents should be indexed.
     */
    @ConflictsWith("path")
    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }

    /**
     * The S3 bucket location of a file containing a list of users whose documents should be indexed.
     *
     * @subresource gyro.aws.kendra.KendraS3Path
     */
    @ConflictsWith("user-list")
    public KendraS3Path getPath() {
        return path;
    }

    public void setPath(KendraS3Path path) {
        this.path = path;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(OneDriveUsers model) {
        if (model.hasOneDriveUserList()) {
            setUserList(model.oneDriveUserList());

        } else {
            KendraS3Path s3Path = newSubresource(KendraS3Path.class);
            s3Path.copyFrom(model.oneDriveUserS3Path());
            setPath(s3Path);
        }
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!configuredFields.contains("user-list") && !configuredFields.contains("path")) {
            errors.add(new ValidationError(this, null, "Either 'user-list' or 'path' is required."));
        }

        return errors;
    }

    public OneDriveUsers toOneDriveUsers() {
        OneDriveUsers.Builder builder = OneDriveUsers.builder();

        if (getUserList() != null) {
            builder = builder.oneDriveUserList(getUserList());

        } else {
            builder = builder.oneDriveUserS3Path(getPath().toS3Path());
        }

        return builder.build();
    }
}
