/*
 * Copyright 2021, Brightspot, Inc.
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

package gyro.aws.ecr;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecr.model.ImageScanningConfiguration;

public class EcrImageScanningConfiguration extends Diffable implements Copyable<ImageScanningConfiguration> {

    private Boolean scanOnPush;

    /**
     * When set to ``true``, images are scanned after being pushed to a repository.
     */
    @Updatable
    @Required
    public Boolean getScanOnPush() {
        return scanOnPush;
    }

    public void setScanOnPush(Boolean scanOnPush) {
        this.scanOnPush = scanOnPush;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(ImageScanningConfiguration model) {
        setScanOnPush(model.scanOnPush());
    }

    ImageScanningConfiguration toImageScanningConfiguration() {
        return ImageScanningConfiguration.builder().scanOnPush(getScanOnPush()).build();
    }
}
