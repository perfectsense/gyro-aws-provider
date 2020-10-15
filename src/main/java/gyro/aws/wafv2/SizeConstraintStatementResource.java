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
import software.amazon.awssdk.services.wafv2.model.ComparisonOperator;
import software.amazon.awssdk.services.wafv2.model.SizeConstraintStatement;

public class SizeConstraintStatementResource extends Diffable implements Copyable<SizeConstraintStatement> {

    private FieldToMatchResource fieldToMatch;
    private ComparisonOperator comparisonOperator;
    private Set<TextTransformationResource> textTransformation;
    private Long size;

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
     * The comparison operator for the size specified.
     */
    @Required
    @ValidStrings({"EQ", "NE", "LE", "LT", "GE", "GT"})
    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }

    public void setComparisonOperator(ComparisonOperator comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }

    /**
     * Text transformation configuration on the data provided before doing the check.
     *
     * @subresource gyro.aws.wafv2.TextTransformationResource
     */
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

    /**
     * The size in byte for the constraint to work on.
     */
    @Required
    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public String primaryKey() {
        return String.format(
            "field to match - '%s' with size constraint - %s and comparison operator - '%s'",
            getFieldToMatch() != null ? getFieldToMatch().primaryKey() : "",
            getSize(),
            getComparisonOperator());
    }

    @Override
    public void copyFrom(SizeConstraintStatement sizeConstraintStatement) {
        setComparisonOperator(sizeConstraintStatement.comparisonOperator());
        setSize(sizeConstraintStatement.size());

        getTextTransformation().clear();
        if (sizeConstraintStatement.textTransformations() != null) {
            sizeConstraintStatement.textTransformations().forEach(o -> {
                TextTransformationResource textTransformation = newSubresource(TextTransformationResource.class);
                textTransformation.copyFrom(o);
                getTextTransformation().add(textTransformation);
            });
        }

        FieldToMatchResource fieldToMatch = newSubresource(FieldToMatchResource.class);
        fieldToMatch.copyFrom(sizeConstraintStatement.fieldToMatch());
        setFieldToMatch(fieldToMatch);
    }

    SizeConstraintStatement toSizeConstraintStatement() {
        SizeConstraintStatement.Builder builder = SizeConstraintStatement.builder()
            .comparisonOperator(getComparisonOperator())
            .size(getSize())
            .fieldToMatch(getFieldToMatch().toFieldToMatch());

        if (!getTextTransformation().isEmpty()) {
            builder = builder.textTransformations(getTextTransformation().stream()
                .map(TextTransformationResource::toTextTransformation)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
