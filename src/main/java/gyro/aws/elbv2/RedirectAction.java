package gyro.aws.elbv2;

import gyro.core.resource.Diffable;
import gyro.core.resource.ResourceUpdatable;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.RedirectActionConfig;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     action
 *         type: "redirect"
 *
 *         redirect-action
 *             port: 443
 *             protocol: "HTTPS"
 *             status-code: "HTTP_301"
 *         end
 *     end
 */
public class RedirectAction extends Diffable {
    private String host;
    private String path;
    private String port;
    private String protocol;
    private String query;
    private String statusCode;

    public RedirectAction() {

    }

    public RedirectAction(RedirectActionConfig redirect) {
        setHost(redirect.host());
        setPath(redirect.path());
        setPort(redirect.port());
        setProtocol(redirect.protocol());
        setQuery(redirect.query());
        setStatusCode(redirect.statusCodeAsString());
    }

    @ResourceUpdatable
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @ResourceUpdatable
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @ResourceUpdatable
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @ResourceUpdatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @ResourceUpdatable
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @ResourceUpdatable
    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String primaryKey() {
        return String.format("%s/%s/%s", getPort(), getProtocol(), getStatusCode());
    }

    public String toDisplayString() {
        return "redirect action";
    }

    public RedirectActionConfig toRedirect() {
        return RedirectActionConfig.builder()
                .host(getHost())
                .path(getPath())
                .port(getPort())
                .protocol(getProtocol())
                .query(getQuery())
                .statusCode(getStatusCode())
                .build();
    }
}
