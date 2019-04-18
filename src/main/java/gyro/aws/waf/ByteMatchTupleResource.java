package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ByteMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.ByteMatchTuple;
import software.amazon.awssdk.services.waf.model.ChangeAction;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@ResourceName(parent = "byte-match-set", value = "byte-match-tuple")
public class ByteMatchTupleResource extends AwsResource {
    private String type;
    private String data;
    private String positionalConstraint;
    private String targetString;
    private String textTransformation;

    public ByteMatchTupleResource() {

    }

    public ByteMatchTupleResource(ByteMatchTuple byteMatchTuple) {
        setType(byteMatchTuple.fieldToMatch().typeAsString());
        setData(byteMatchTuple.fieldToMatch().data());
        setPositionalConstraint(byteMatchTuple.positionalConstraintAsString());
        setTargetString(byteMatchTuple.targetString().asString(StandardCharsets.UTF_8));
        setTextTransformation(byteMatchTuple.textTransformationAsString());
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
     * The comparison to be done on the filter. Valid values ```EQ```, ```NE```, ```LE```, ```LT```, ```GE```, ```GT```. (Required)
     */
    public String getPositionalConstraint() {
        return positionalConstraint != null ? positionalConstraint.toUpperCase() : null;
    }

    public void setPositionalConstraint(String positionalConstraint) {
        this.positionalConstraint = positionalConstraint;
    }

    /**
     * the target string to filter on for the byte match filter. (Required)
     */
    public String getTargetString() {
        return targetString;
    }

    public void setTargetString(String targetString) {
        this.targetString = targetString;
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

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        saveByteMatchTuple(client, getByteMatchTuple(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        saveByteMatchTuple(client, getByteMatchTuple(), true);
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

        if (!ObjectUtils.isBlank(getPositionalConstraint())) {
            sb.append(" - ").append(getPositionalConstraint());
        }

        if (!ObjectUtils.isBlank(getTargetString())) {
            sb.append(" - ").append(getTargetString());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s %s %s %s", getData(), getType(), getTextTransformation(), getPositionalConstraint(), getTargetString());
    }

    @Override
    public String resourceIdentifier() {
        return null;
    }

    private ByteMatchTuple getByteMatchTuple() {
        return ByteMatchTuple.builder()
            .fieldToMatch(f -> f.data(getData()).type(getType()))
            .textTransformation(getTextTransformation())
            .positionalConstraint(getPositionalConstraint())
            .targetString(SdkBytes.fromUtf8String(getTargetString()))
            .build();
    }

    private void saveByteMatchTuple(WafClient client, ByteMatchTuple byteMatchTuple, boolean isDelete) {
        ByteMatchSetResource parent = (ByteMatchSetResource) parent();

        ByteMatchSetUpdate byteMatchSetUpdate = ByteMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .byteMatchTuple(byteMatchTuple)
            .build();

        client.updateByteMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .byteMatchSetId(parent.getByteMatchSetId())
                .updates(byteMatchSetUpdate)
        );
    }
}
