package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.ByteMatchStatement;

public class ByteMatchStatementResource extends WafDiffable implements Copyable<ByteMatchStatement> {

    private FieldToMatchResource fieldToMatch;
    private String positionalConstraint;
    private Set<TextTransformationResource> textTransformation;
    private String searchString;

    public FieldToMatchResource getFieldToMatch() {
        return fieldToMatch;
    }

    public void setFieldToMatch(FieldToMatchResource fieldToMatch) {
        this.fieldToMatch = fieldToMatch;
    }

    public String getPositionalConstraint() {
        return positionalConstraint;
    }

    public void setPositionalConstraint(String positionalConstraint) {
        this.positionalConstraint = positionalConstraint;
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

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    @Override
    public void copyFrom(ByteMatchStatement byteMatchStatement) {
        setPositionalConstraint(byteMatchStatement.positionalConstraintAsString());
        setSearchString(byteMatchStatement.searchString().toString());
        setHashCode(byteMatchStatement.hashCode());

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
            .fieldToMatch(getFieldToMatch().toFieldToMatch());

        if (!ObjectUtils.isBlank(getPositionalConstraint())) {
            builder = builder.positionalConstraint(getPositionalConstraint());
        }

        if (!getTextTransformation().isEmpty()) {
            builder = builder.textTransformations(getTextTransformation().stream()
                .map(TextTransformationResource::toTextTransformation)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
