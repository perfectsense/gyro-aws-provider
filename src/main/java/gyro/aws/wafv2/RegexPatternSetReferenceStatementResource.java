package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.RegexPatternSetReferenceStatement;

public class RegexPatternSetReferenceStatementResource extends WafDiffable
    implements Copyable<RegexPatternSetReferenceStatement> {

    private FieldToMatchResource fieldToMatch;
    private RegexPatternSetResource regexPatternSet;
    private Set<TextTransformationResource> textTransformation;

    public FieldToMatchResource getFieldToMatch() {
        return fieldToMatch;
    }

    public void setFieldToMatch(FieldToMatchResource fieldToMatch) {
        this.fieldToMatch = fieldToMatch;
    }

    public RegexPatternSetResource getRegexPatternSet() {
        return regexPatternSet;
    }

    public void setRegexPatternSet(RegexPatternSetResource regexPatternSet) {
        this.regexPatternSet = regexPatternSet;
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
