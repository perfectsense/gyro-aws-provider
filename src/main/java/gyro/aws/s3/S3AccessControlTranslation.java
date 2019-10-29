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

package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.s3.model.AccessControlTranslation;
import software.amazon.awssdk.services.s3.model.OwnerOverride;

public class S3AccessControlTranslation extends Diffable implements Copyable<AccessControlTranslation> {


    private OwnerOverride ownerOverride;

    /**
     * Sets the ownership of the replica. Valid value is ``DESTINATION``
     */
    public OwnerOverride getOwnerOverride() {
        return ownerOverride;
    }

    public void setOwnerOverride(OwnerOverride ownerOverride) {
        this.ownerOverride = ownerOverride;
    }

    @Override
    public String primaryKey() {
        return "access control translation";
    }

    @Override
    public void copyFrom(AccessControlTranslation accessControlTranslation) {
        setOwnerOverride(accessControlTranslation.owner());
    }

    AccessControlTranslation toAccessControlTranslation() {
        return AccessControlTranslation.builder()
                .owner(getOwnerOverride())
                .build();
    }

}
