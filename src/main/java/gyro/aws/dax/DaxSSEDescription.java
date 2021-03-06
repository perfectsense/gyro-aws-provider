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

package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.dax.model.SSEDescription;
import software.amazon.awssdk.services.dax.model.SSEStatus;

public class DaxSSEDescription extends Diffable implements Copyable<SSEDescription> {

    private SSEStatus status;

    /**
     * The status of the SSE.
     */
    @Output
    public SSEStatus getStatus() {
        return status;
    }

    public void setStatus(SSEStatus status) {
        this.status = status;
    }

    @Override
    public void copyFrom(SSEDescription model) {
        setStatus(model.status());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
