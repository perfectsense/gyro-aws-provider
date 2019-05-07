package gyro.aws.waf;

import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.UpdateXssMatchSetRequest;
import software.amazon.awssdk.services.waf.model.XssMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.XssMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.Set;

@ResourceName(parent = "xss-match-set", value = "xss-match-tuple")
public class XssMatchTupleResource extends AbstractWafResource {
    private String data;
    private String type;
    private String textTransformation;

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
     * Part of the request to filter on. Valid values ```URI```, ```QUERY_STRING```, ```HEADER```, ```METHOD```, ```BODY```, ```SINGLE_QUERY_ARG```, ```ALL_QUERY_ARGS```. (Required)
     */
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
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

    public XssMatchTupleResource() {

    }

    public XssMatchTupleResource(XssMatchTuple xssMatchTuple) {
        setData(xssMatchTuple.fieldToMatch().data());
        setType(xssMatchTuple.fieldToMatch().typeAsString());
        setTextTransformation(xssMatchTuple.textTransformationAsString());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        if (getRegionalWaf()) {
            saveXssMatchTuple(getRegionalClient(), getXssMatchTuple(), false);
        } else {
            saveXssMatchTuple(getGlobalClient(), getXssMatchTuple(), false);
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        if (getRegionalWaf()) {
            saveXssMatchTuple(getRegionalClient(), getXssMatchTuple(), true);
        } else {
            saveXssMatchTuple(getGlobalClient(), getXssMatchTuple(), true);
        }
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("xss match tuple");

        if (!ObjectUtils.isBlank(getData())) {
            sb.append(" - ").append(getData());
        }

        if (!ObjectUtils.isBlank(getType())) {
            sb.append(" - ").append(getType());
        }

        if (!ObjectUtils.isBlank(getTextTransformation())) {
            sb.append(" - ").append(getTextTransformation());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s %s", getData(), getType(), getTextTransformation());
    }

    @Override
    public String resourceIdentifier() {
        return null;
    }

    private XssMatchTuple getXssMatchTuple() {
        return XssMatchTuple.builder()
            .fieldToMatch(f -> f.data(getData()).type(getType()))
            .textTransformation(getTextTransformation())
            .build();
    }

    private void saveXssMatchTuple(WafClient client, XssMatchTuple xssMatchTuple, boolean isDelete) {
        client.updateXssMatchSet(getUpdateXssMatchSetRequest(xssMatchTuple, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }

    private void saveXssMatchTuple(WafRegionalClient client, XssMatchTuple xssMatchTuple, boolean isDelete) {
        client.updateXssMatchSet(getUpdateXssMatchSetRequest(xssMatchTuple, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }

    private UpdateXssMatchSetRequest.Builder getUpdateXssMatchSetRequest(XssMatchTuple xssMatchTuple, boolean isDelete) {
        XssMatchSetResource parent = (XssMatchSetResource) parent();

        XssMatchSetUpdate xssMatchSetUpdate = XssMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .xssMatchTuple(xssMatchTuple)
            .build();

        return UpdateXssMatchSetRequest.builder()
            .xssMatchSetId(parent.getXssMatchSetId())
            .updates(xssMatchSetUpdate);
    }
}
