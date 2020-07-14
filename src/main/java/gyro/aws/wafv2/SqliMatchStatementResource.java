package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.SqliMatchStatement;

public class SqliMatchStatementResource extends WafDiffable implements Copyable<SqliMatchStatement> {

    private FieldToMatchResource fieldToMatch;
    private Set<TextTransformationResource> textTransformation;

    public FieldToMatchResource getFieldToMatch() {
        return fieldToMatch;
    }

    public void setFieldToMatch(FieldToMatchResource fieldToMatch) {
        this.fieldToMatch = fieldToMatch;
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

    @Override
    public void copyFrom(SqliMatchStatement sqliMatchStatement) {
        setHashCode(sqliMatchStatement.hashCode());

        getTextTransformation().clear();
        if (sqliMatchStatement.textTransformations() != null) {
            sqliMatchStatement.textTransformations().forEach(o -> {
                TextTransformationResource textTransformation = newSubresource(TextTransformationResource.class);
                textTransformation.copyFrom(o);
                getTextTransformation().add(textTransformation);
            });
        }

        FieldToMatchResource fieldToMatch = newSubresource(FieldToMatchResource.class);
        fieldToMatch.copyFrom(sqliMatchStatement.fieldToMatch());
        setFieldToMatch(fieldToMatch);
    }

    SqliMatchStatement toSqliMatchStatement() {
        SqliMatchStatement.Builder builder = SqliMatchStatement.builder()
            .fieldToMatch(getFieldToMatch().toFieldToMatch());

        if (!getTextTransformation().isEmpty()) {
            builder = builder.textTransformations(getTextTransformation().stream()
                .map(TextTransformationResource::toTextTransformation)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
