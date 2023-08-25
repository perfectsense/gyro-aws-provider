/*
 * Copyright 2023, Brightspot.
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

package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.cloudfront.model.EventType;
import software.amazon.awssdk.services.cloudfront.model.FunctionAssociation;

public class CloudFrontCacheBehaviorFunctionAssociation extends Diffable implements Copyable<FunctionAssociation> {

    private CloudFrontFunctionResource function;
    private EventType eventType;

    /**
     * The ARN of the cloudfront function being associated.
     */
    @Required
    @Updatable
    public CloudFrontFunctionResource getFunction() {
        return function;
    }

    public void setFunction(CloudFrontFunctionResource function) {
        this.function = function;
    }

    /**
     * The event type of the Lambda function.
     */
    @Required
    @Updatable
    @ValidStrings({"viewer-request", "viewer-response", "origin-request", "origin-response"})
    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public void copyFrom(FunctionAssociation model) {
        setEventType(model.eventType());
        setFunction(findById(CloudFrontFunctionResource.class, model.functionARN()));
    }

    @Override
    public String primaryKey() {
        return getEventType() != null ? getEventType().toString() : "";
    }

    FunctionAssociation toFunctionAssociation() {
        return FunctionAssociation.builder()
            .eventType(getEventType())
            .functionARN(getFunction().getArn())
            .build();
    }
}
