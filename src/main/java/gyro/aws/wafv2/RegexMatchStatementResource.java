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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.RegexMatchStatement;

public class RegexMatchStatementResource extends Diffable implements Copyable<RegexMatchStatement> {

    private String regexString;
    private FieldToMatchResource fieldToMatch;
    private Set<TextTransformationResource> textTransformation;

    /**
     * The regex pattern to match the condition.
     */
    @Required
    public String getRegexString() {
        return regexString;
    }

    public void setRegexString(String regexString) {
        this.regexString = regexString;
    }

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

    @Override
    public String primaryKey() {
        return String.format(
            "regex pattern - '%s' and field to match - '%s'",
            getRegexString() != null ? getRegexString() : "",
            getFieldToMatch() != null ? getFieldToMatch().primaryKey() : "");
    }

    @Override
    public void copyFrom(RegexMatchStatement model) {
        setRegexString(model.regexString());

        getTextTransformation().clear();
        if (model.textTransformations() != null) {
            model.textTransformations().forEach(o -> {
                TextTransformationResource textTransformation = newSubresource(TextTransformationResource.class);
                textTransformation.copyFrom(o);
                getTextTransformation().add(textTransformation);
            });
        }

        FieldToMatchResource fieldToMatch = newSubresource(FieldToMatchResource.class);
        fieldToMatch.copyFrom(model.fieldToMatch());
        setFieldToMatch(fieldToMatch);
    }

    public RegexMatchStatement toRegexMatchStatement() {
        RegexMatchStatement.Builder builder = RegexMatchStatement.builder()
            .regexString(getRegexString())
            .fieldToMatch(getFieldToMatch().toFieldToMatch());

        if (!getTextTransformation().isEmpty()) {
            builder = builder.textTransformations(getTextTransformation().stream()
                .map(TextTransformationResource::toTextTransformation)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
