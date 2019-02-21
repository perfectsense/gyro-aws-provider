package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetRegexMatchSetResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ResourceName("regex-match-set")
public class RegexMatchSetResource extends AwsResource {
    private String name;
    private String regexMatchSetId;
    private List<RegexMatchTupleResource> regexMatchTuple;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegexMatchSetId() {
        return regexMatchSetId;
    }

    public void setRegexMatchSetId(String regexMatchSetId) {
        this.regexMatchSetId = regexMatchSetId;
    }

    public List<RegexMatchTupleResource> getRegexMatchTuple() {
        if (regexMatchTuple == null) {
            regexMatchTuple = new ArrayList<>();
        }

        return regexMatchTuple;
    }

    public void setRegexMatchTuple(List<RegexMatchTupleResource> regexMatchTuple) {
        this.regexMatchTuple = regexMatchTuple;
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getRegexMatchSetId())) {
            return false;
        }

        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        GetRegexMatchSetResponse response = client.getRegexMatchSet(
            r -> r.regexMatchSetId(getRegexMatchSetId())
        );

        RegexMatchSet regexMatchSet = response.regexMatchSet();

        setName(regexMatchSet.name());

        getRegexMatchTuple().clear();

        for (RegexMatchTuple regexMatchTuple : regexMatchSet.regexMatchTuples()) {
            RegexMatchTupleResource regexMatchTupleResource = new RegexMatchTupleResource(regexMatchTuple);
            regexMatchTupleResource.parent(this);
            getRegexMatchTuple().add(regexMatchTupleResource);
        }

        return true;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        CreateRegexMatchSetResponse response = client.createRegexMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        RegexMatchSet regexMatchSet = response.regexMatchSet();

        setRegexMatchSetId(regexMatchSet.regexMatchSetId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteRegexMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .regexMatchSetId(getRegexMatchSetId())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("regex match set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getRegexMatchSetId())) {
            sb.append(" - ").append(getRegexMatchSetId());
        }

        return sb.toString();
    }
}
