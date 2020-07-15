/*
 * Copyright 2020, Perfect Sense, Inc.
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
import software.amazon.awssdk.services.wafv2.model.AllQueryArguments;
import software.amazon.awssdk.services.wafv2.model.Body;
import software.amazon.awssdk.services.wafv2.model.FieldToMatch;
import software.amazon.awssdk.services.wafv2.model.Method;
import software.amazon.awssdk.services.wafv2.model.QueryString;
import software.amazon.awssdk.services.wafv2.model.SingleHeader;
import software.amazon.awssdk.services.wafv2.model.SingleQueryArgument;
import software.amazon.awssdk.services.wafv2.model.UriPath;

public class FieldToMatchResource extends WafDiffable implements Copyable<FieldToMatch> {

    private FieldMatchType matchType;
    private String name;

    public FieldMatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(FieldMatchType matchType) {
        this.matchType = matchType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void copyFrom(FieldToMatch fieldToMatch) {
        FieldMatchType matchType = FieldMatchType.BODY;

        if (fieldToMatch.allQueryArguments() != null) {
            matchType = FieldMatchType.ALL_QUERY_ARGUMENTS;
        } else if (fieldToMatch.queryString() != null) {
            matchType = FieldMatchType.QUERY_STRING;
        } else if (fieldToMatch.method() != null) {
            matchType = FieldMatchType.METHOD;
        } else if (fieldToMatch.uriPath() != null) {
            matchType = FieldMatchType.URI_PATH;
        } else if (fieldToMatch.singleHeader() != null) {
            matchType = FieldMatchType.SINGLE_HEADER;
            setName(fieldToMatch.singleHeader().name());
        } else if (fieldToMatch.singleQueryArgument() != null) {
            matchType = FieldMatchType.SINGLE_QUERY_ARGUMENT;
            setName(fieldToMatch.singleQueryArgument().name());
        }

        setMatchType(matchType);
        setHashCode(fieldToMatch.hashCode());
    }

    FieldToMatch toFieldToMatch() {
        FieldToMatch.Builder builder = FieldToMatch.builder();

        if (getMatchType() == FieldMatchType.BODY) {
            builder.body(Body.builder().build());
        } else if (getMatchType() == FieldMatchType.ALL_QUERY_ARGUMENTS) {
            builder.allQueryArguments(AllQueryArguments.builder().build());
        } else if (getMatchType() == FieldMatchType.QUERY_STRING) {
            builder.queryString(QueryString.builder().build());
        } else if (getMatchType() == FieldMatchType.METHOD) {
            builder.method(Method.builder().build());
        } else if (getMatchType() == FieldMatchType.URI_PATH) {
            builder.uriPath(UriPath.builder().build());
        } else if (getMatchType() == FieldMatchType.SINGLE_HEADER) {
            builder.singleHeader(SingleHeader.builder().name(getName()).build());
        } else if (getMatchType() == FieldMatchType.SINGLE_QUERY_ARGUMENT) {
            builder.singleQueryArgument(SingleQueryArgument.builder().build());
        }

        return builder.build();
    }
}
