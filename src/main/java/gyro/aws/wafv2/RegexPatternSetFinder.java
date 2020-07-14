package gyro.aws.wafv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.ListRegexPatternSetsRequest;
import software.amazon.awssdk.services.wafv2.model.ListRegexPatternSetsResponse;
import software.amazon.awssdk.services.wafv2.model.RegexPatternSet;
import software.amazon.awssdk.services.wafv2.model.Scope;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;

@Type("waf-regex-pattern-set")
public class RegexPatternSetFinder extends AwsFinder<Wafv2Client, RegexPatternSet, RegexPatternSetResource> {

    private String id;
    private String name;
    private String scope;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    protected List<RegexPatternSet> findAllAws(Wafv2Client client) {
        List<RegexPatternSet> regexPatternSets = new ArrayList<>();
        ListRegexPatternSetsResponse response;
        String marker = null;

        do {
            response = client.listRegexPatternSets(ListRegexPatternSetsRequest.builder()
                .scope(Scope.CLOUDFRONT)
                .nextMarker(marker)
                .build());

            marker = response.nextMarker();

            regexPatternSets.addAll(response.regexPatternSets()
                .stream()
                .map(o -> getRegexPatternSet(client, o.id(), o.name(), Scope.CLOUDFRONT.toString()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        } while (!ObjectUtils.isBlank(marker));

        marker = null;

        do {
            response = client.listRegexPatternSets(ListRegexPatternSetsRequest.builder()
                .scope(Scope.REGIONAL)
                .nextMarker(marker)
                .build());

            marker = response.nextMarker();

            regexPatternSets.addAll(response.regexPatternSets()
                .stream()
                .map(o -> getRegexPatternSet(client, o.id(), o.name(), Scope.REGIONAL.toString()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        } while (!ObjectUtils.isBlank(marker));

        return regexPatternSets;
    }

    @Override
    protected List<RegexPatternSet> findAws(Wafv2Client client, Map<String, String> filters) {
        List<RegexPatternSet> regexPatternSets = new ArrayList<>();

        RegexPatternSet regexPatternSet = getRegexPatternSet(
            client,
            filters.get("id"),
            filters.get("name"),
            filters.get("scope"));

        if (regexPatternSet != null) {
            regexPatternSets.add(regexPatternSet);
        }

        return regexPatternSets;
    }

    private RegexPatternSet getRegexPatternSet(Wafv2Client client, String id, String name, String scope) {
        try {
            return client.getRegexPatternSet(r -> r.id(id).name(name).scope(scope)).regexPatternSet();
        } catch (WafNonexistentItemException ex) {
            return null;
        }
    }
}
