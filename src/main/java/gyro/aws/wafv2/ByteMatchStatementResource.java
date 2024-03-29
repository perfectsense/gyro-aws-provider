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

package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.wafv2.model.ByteMatchStatement;
import software.amazon.awssdk.services.wafv2.model.PositionalConstraint;

public class ByteMatchStatementResource extends Diffable implements Copyable<ByteMatchStatement> {

    private FieldToMatchResource fieldToMatch;
    private PositionalConstraint positionalConstraint;
    private Set<TextTransformationResource> textTransformation;
    private String searchString;

    /**
     * The field setting to match the condition.
     *
     * @subresource gyro.aws.wafv2.FieldToMatchResource
     */
    @Required
    public FieldToMatchResource getFieldToMatch() {
        return fieldToMatch;
    }

    public void setFieldToMatch(FieldToMatchResource fieldToMatch) {
        this.fieldToMatch = fieldToMatch;
    }

    /**
     * The positional search type for the search string.
     */
    @Required
    @ValidStrings({"EXACTLY", "STARTS_WITH", "ENDS_WITH", "CONTAINS", "CONTAINS_WORD"})
    public PositionalConstraint getPositionalConstraint() {
        return positionalConstraint;
    }

    public void setPositionalConstraint(PositionalConstraint positionalConstraint) {
        this.positionalConstraint = positionalConstraint;
    }

    /**
     * Text transformation configuration on the data provided before doing the check.
     *
     * @subresource gyro.aws.wafv2.TextTransformationResource
     */
    @Updatable
    @CollectionMax(10)
    public Set<TextTransformationResource> getTextTransformation() {
        if (textTransformation == null) {
            textTransformation = new HashSet<>();
        }

        return textTransformation;
    }

    public void setTextTransformation(Set<TextTransformationResource> textTransformation) {
        this.textTransformation = textTransformation;
    }

    /**
     * The search string you want aws to search for in the request.
     */
    @Required
    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    @Override
    public String primaryKey() {
        return String.format(
            "field to match - '%s' with positional constraint - %s and search string - '%s'",
            getFieldToMatch() != null ? getFieldToMatch().primaryKey() : "",
            getPositionalConstraint(),
            getSearchString());
    }

    @Override
    public void copyFrom(ByteMatchStatement byteMatchStatement) {
        setPositionalConstraint(byteMatchStatement.positionalConstraint());
        setSearchString(byteMatchStatement.searchString().asUtf8String());

        getTextTransformation().clear();
        if (byteMatchStatement.textTransformations() != null) {
            byteMatchStatement.textTransformations().forEach(o -> {
                TextTransformationResource textTransformation = newSubresource(TextTransformationResource.class);
                textTransformation.copyFrom(o);
                getTextTransformation().add(textTransformation);
            });
        }

        FieldToMatchResource fieldToMatch = newSubresource(FieldToMatchResource.class);
        fieldToMatch.copyFrom(byteMatchStatement.fieldToMatch());
        setFieldToMatch(fieldToMatch);
    }

    ByteMatchStatement toByteMatchStatement() {
        ByteMatchStatement.Builder builder = ByteMatchStatement
            .builder()
            .fieldToMatch(getFieldToMatch().toFieldToMatch())
            .positionalConstraint(getPositionalConstraint())
            .searchString(SdkBytes.fromUtf8String(getSearchString()));

        if (!getTextTransformation().isEmpty()) {
            builder = builder.textTransformations(getTextTransformation().stream()
                .map(TextTransformationResource::toTextTransformation)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
