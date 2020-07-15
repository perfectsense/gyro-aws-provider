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
import software.amazon.awssdk.services.wafv2.model.ListWebAcLsRequest;
import software.amazon.awssdk.services.wafv2.model.ListWebAcLsResponse;
import software.amazon.awssdk.services.wafv2.model.Scope;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.awssdk.services.wafv2.model.WebACL;

@Type("waf-web-acl")
public class WebAclFinder extends AwsFinder<Wafv2Client, WebACL, WebAclResource> {

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
    protected List<WebACL> findAllAws(Wafv2Client client) {
        List<WebACL> webACLs = new ArrayList<>();
        ListWebAcLsResponse response;
        String marker = null;

        do {
            try {
                response = client.listWebACLs(ListWebAcLsRequest.builder()
                    .scope(Scope.CLOUDFRONT)
                    .nextMarker(marker)
                    .build());

                marker = response.nextMarker();

                webACLs.addAll(response.webACLs()
                    .stream()
                    .map(o -> getWebACL(client, o.id(), o.name(), Scope.CLOUDFRONT.toString()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
            } catch (WafInvalidParameterException ex) {
                // Ignore
                // Occurs if no cloudfront based web acl present
            }

        } while (!ObjectUtils.isBlank(marker));

        marker = null;

        do {
            try {
                response = client.listWebACLs(ListWebAcLsRequest.builder()
                    .scope(Scope.REGIONAL)
                    .nextMarker(marker)
                    .build());

                marker = response.nextMarker();

                webACLs.addAll(response.webACLs()
                    .stream()
                    .map(o -> getWebACL(client, o.id(), o.name(), Scope.REGIONAL.toString()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
            } catch (WafInvalidParameterException ex) {
                // Ignore
                // Occurs if no regional based web acl present
            }

        } while (!ObjectUtils.isBlank(marker));

        return webACLs;
        /*GetWebAclResponse response = client.getWebACL(
            r -> r.id("8487a18b-fd40-40d4-8375-e4e7957fad41")
                .name("dj-test-waf")
                .scope(Scope.REGIONAL)
        );



        return Collections.singletonList(response.webACL());*/
    }

    @Override
    protected List<WebACL> findAws(Wafv2Client client, Map<String, String> filters) {
        List<WebACL> webACLs = new ArrayList<>();

        WebACL webACL = getWebACL(client, filters.get("id"), filters.get("name"), filters.get("scope"));

        if (webACL != null) {
            webACLs.add(webACL);
        }

        return webACLs;
    }

    private WebACL getWebACL(Wafv2Client client, String id, String name, String scope) {
        try {
            return client.getWebACL(r -> r.id(id).name(name).scope(scope)).webACL();
        } catch (WafNonexistentItemException ex) {
            return null;
        }
    }
}
