package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.SizeConstraintStatement;

public class SizeConstraintStatementResource extends WafDiffable implements Copyable<SizeConstraintStatement> {

    private FieldToMatchResource fieldToMatch;
    private String comparisonOperator;
    private Set<TextTransformationResource> textTransformation;
    private Long size;

    public FieldToMatchResource getFieldToMatch() {
        return fieldToMatch;
    }

    public void setFieldToMatch(FieldToMatchResource fieldToMatch) {
        this.fieldToMatch = fieldToMatch;
    }

    public String getComparisonOperator() {
        return comparisonOperator;
    }

    public void setComparisonOperator(String comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }

    public Set<TextTransformationResource> getTextTransformation() {
        if (textTransformation == null) {
            textTransformation = new HashSet<>();
        }

        return textTransformation;
    }

    public void setTextTransformation(Set<TextTransformationResource> textTransformation) {
        this.textTransformation = textTransformation;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public void copyFrom(SizeConstraintStatement sizeConstraintStatement) {
        setComparisonOperator(sizeConstraintStatement.comparisonOperatorAsString());
        setSize(sizeConstraintStatement.size());
        setHashCode(sizeConstraintStatement.hashCode());

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
