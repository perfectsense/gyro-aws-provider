package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.RegexMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;

import java.util.Set;

public class RegexMatchTupleResource extends AwsResource {
    private String type;
    private String data;
    private String textTransformation;
    private String regexPatternSetId;

    public RegexMatchTupleResource() {

    }

    public RegexMatchTupleResource(RegexMatchTuple regexMatchTuple) {
        setType(regexMatchTuple.fieldToMatch().typeAsString());
        setData(regexMatchTuple.fieldToMatch().data());
        setRegexPatternSetId(regexMatchTuple.regexPatternSetId());
        setTextTransformation(regexMatchTuple.textTransformationAsString());
    }

    /**
     * Part of the request to filter on. Valid values ```URI```, ```QUERY_STRING```, ```HEADER```, ```METHOD```, ```BODY```, ```SINGLE_QUERY_ARG```, ```ALL_QUERY_ARGS```. (Required)
     */
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * If type selected as ```HEADER``` or ```SINGLE_QUERY_ARG```, the value needs to be provided.
     */
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * Text transformation on the data provided before doing the check. Valid values ``NONE``, ``COMPRESS_WHITE_SPACE``, ``HTML_ENTITY_DECODE``, ``LOWERCASE``, ``CMD_LINE``, ``URL_DECODE``. (Required)
     */
    public String getTextTransformation() {
        return textTransformation != null ? textTransformation.toUpperCase() : null;
    }

    public void setTextTransformation(String textTransformation) {
        this.textTransformation = textTransformation;
    }

    /**
     * The id of a regex pattern set, having the regex patterns to filter the request.
     */
    public String getRegexPatternSetId() {
        return regexPatternSetId;
    }

    public void setRegexPatternSetId(String regexPatternSetId) {
        this.regexPatternSetId = regexPatternSetId;
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        saveRegexMatchTuple(client, getRegexMatchTuple(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        saveRegexMatchTuple(client, getRegexMatchTuple(), true);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("byte match tuple");

        if (!ObjectUtils.isBlank(getData())) {
            sb.append(" - ").append(getData());
        }

        if (!ObjectUtils.isBlank(getType())) {
            sb.append(" - ").append(getType());
        }

        if (!ObjectUtils.isBlank(getTextTransformation())) {
            sb.append(" - ").append(getTextTransformation());
        }

        if (!ObjectUtils.isBlank(getRegexPatternSetId())) {
            sb.append(" - ").append(getRegexPatternSetId());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s %s %s", getData(), getType(), getTextTransformation(), getRegexPatternSetId());
    }

    private RegexMatchTuple getRegexMatchTuple() {
        return RegexMatchTuple.builder()
            .fieldToMatch(f -> f.data(getData()).type(getType()))
            .regexPatternSetId(getRegexPatternSetId())
            .textTransformation(getTextTransformation())
            .build();
    }

    private void saveRegexMatchTuple(WafClient client, RegexMatchTuple regexMatchTuple, boolean isDelete) {
        RegexMatchSetResource parent = (RegexMatchSetResource) parent();

        RegexMatchSetUpdate regexMatchSetUpdate = RegexMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .regexMatchTuple(regexMatchTuple)
            .build();

        client.updateRegexMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .regexMatchSetId(parent.getRegexMatchSetId())
                .updates(regexMatchSetUpdate)
        );
    }
}
