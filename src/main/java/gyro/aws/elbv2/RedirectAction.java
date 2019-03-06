package gyro.aws.elbv2;

import gyro.core.diff.Diffable;
import gyro.core.diff.ResourceDiffProperty;

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

    @ResourceDiffProperty(updatable = true)
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @ResourceDiffProperty(updatable = true)
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @ResourceDiffProperty(updatable = true)
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @ResourceDiffProperty(updatable = true)
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @ResourceDiffProperty(updatable = true)
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @ResourceDiffProperty(updatable = true)
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
