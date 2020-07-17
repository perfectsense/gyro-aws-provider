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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.RegexPatternSetReferenceStatement;

public class RegexPatternSetReferenceStatementResource extends WafDiffable
    implements Copyable<RegexPatternSetReferenceStatement> {

    private FieldToMatchResource fieldToMatch;
    private RegexPatternSetResource regexPatternSet;
    private Set<TextTransformationResource> textTransformation;

    @Required
    @Updatable
    public FieldToMatchResource getFieldToMatch() {
        return fieldToMatch;
    }

    public void setFieldToMatch(FieldToMatchResource fieldToMatch) {
        this.fieldToMatch = fieldToMatch;
    }

    @Required
    @Updatable
    public RegexPatternSetResource getRegexPatternSet() {
        return regexPatternSet;
    }

    public void setRegexPatternSet(RegexPatternSetResource regexPatternSet) {
        this.regexPatternSet = regexPatternSet;
    }

    @Updatable
    @CollectionMax(3)
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
    public void copyFrom(RegexPatternSetReferenceStatement regexPatternSetReferenceStatement) {
        setHashCode(regexPatternSetReferenceStatement.hashCode());
        setRegexPatternSet(findById(RegexPatternSetResource.class, regexPatternSetReferenceStatement.arn()));

        getTextTransformation().clear();
        if (regexPatternSetReferenceStatement.textTransformations() != null) {
            regexPatternSetReferenceStatement.textTransformations().forEach(o -> {
                TextTransformationResource textTransformation = newSubresource(TextTransformationResource.class);
                textTransformation.copyFrom(o);
                getTextTransformation().add(textTransformation);
            });
        }

        FieldToMatchResource fieldToMatch = newSubresource(FieldToMatchResource.class);
        fieldToMatch.copyFrom(regexPatternSetReferenceStatement.fieldToMatch());
        setFieldToMatch(fieldToMatch);
    }

    RegexPatternSetReferenceStatement toRegexPatternSetReferenceStatement() {
        RegexPatternSetReferenceStatement.Builder builder = RegexPatternSetReferenceStatement.builder()
            .arn(getRegexPatternSet().getArn())
            .fieldToMatch(getFieldToMatch().toFieldToMatch());

        if (!getTextTransformation().isEmpty()) {
            builder = builder.textTransformations(getTextTransformation().stream()
                .map(TextTransformationResource::toTextTransformation)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
