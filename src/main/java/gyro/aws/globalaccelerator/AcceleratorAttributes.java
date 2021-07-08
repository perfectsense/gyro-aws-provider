/*
 * Copyright 2021, Brightspot.
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

package gyro.aws.globalaccelerator;

import gyro.core.resource.Diffable;

public class AcceleratorAttributes extends Diffable {

    private Boolean flowLogsEnabled;
    private String flowLogsS3Bucket;
    private String flowLogsS3Prefix;

    @Override
    public String primaryKey() {
        return "";
    }

    /**
     * Whether flow logs are enabled.
     */
    @Updatable
    public Boolean getFlowLogsEnabled() {
        return flowLogsEnabled;
    }

    public void setFlowLogsEnabled(Boolean flowLogsEnabled) {
        this.flowLogsEnabled = flowLogsEnabled;
    }

    /**
     * The bucket to upload flow logs to.
     */
    public String getFlowLogsS3Bucket() {
    @Updatable
        return flowLogsS3Bucket;
    }

    public void setFlowLogsS3Bucket(String flowLogsS3Bucket) {
        this.flowLogsS3Bucket = flowLogsS3Bucket;
    }

    /**
     * The location to upload flow logs in the bucket.
     */
    @Updatable
    public String getFlowLogsS3Prefix() {
        return flowLogsS3Prefix;
    }

    public void setFlowLogsS3Prefix(String flowLogsS3Prefix) {
        this.flowLogsS3Prefix = flowLogsS3Prefix;
    }
    }
}
