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

package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.cloudfront.model.LambdaFunctionAssociation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CloudFrontCacheBehaviorLambdaFunction extends Diffable implements Copyable<LambdaFunctionAssociation> {

    private String eventType;
    private String arn;
    private Boolean includeBody;

    private static final Set<String> EventType =
        new HashSet<>(Arrays.asList("viewer-request", "viewer-response", "origin-request", "origin-response"));

    @Updatable
    public String getEventType() {
        if (eventType == null) {
            eventType = "";
        }

        return eventType.toLowerCase();
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Updatable
    public String getArn() {
        if (arn == null) {
            arn = "";
        }

        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Updatable
    public Boolean getIncludeBody() {
        if (includeBody == null) {
            includeBody = false;
        }

        return includeBody;
    }

    public void setIncludeBody(Boolean includeBody) {
        this.includeBody = includeBody;
    }

    @Override
    public void copyFrom(LambdaFunctionAssociation lambdaFunctionAssociation) {
        setEventType(lambdaFunctionAssociation.eventTypeAsString());
        setArn(lambdaFunctionAssociation.lambdaFunctionARN());
        setIncludeBody(lambdaFunctionAssociation.includeBody());
    }

    @Override
    public String primaryKey() {
        return getEventType();
    }

    LambdaFunctionAssociation toLambdaFunctionAssociation() {
        return LambdaFunctionAssociation.builder()
            .eventType(getEventType())
            .includeBody(getIncludeBody())
            .lambdaFunctionARN(getArn())
            .build();
    }
}
