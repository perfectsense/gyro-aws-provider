/*
 * Copyright 2024, Brightspot.
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

package gyro.aws.wafv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.wafv2.model.Body;
import software.amazon.awssdk.services.wafv2.model.OversizeHandling;

public class FieldMatchBodyResource extends Diffable implements Copyable<Body> {

    private OversizeHandling oversizeHandling;

    /**
     * The oversize handling for the body.
     */
    @Required
    @Updatable
    @ValidStrings({"MATCH", "NO_MATCH", "CONTINUE"})
    public OversizeHandling getOversizeHandling() {
        return oversizeHandling;
    }

    public void setOversizeHandling(OversizeHandling oversizeHandling) {
        this.oversizeHandling = oversizeHandling;
    }

    @Override
    public void copyFrom(Body model) {
        setOversizeHandling(model.oversizeHandling());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    Body toBody() {
        return Body.builder()
            .oversizeHandling(getOversizeHandling())
            .build();
    }
}
