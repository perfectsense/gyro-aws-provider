/*
 * Copyright 2020, Brightspot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.apigatewayv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.Api;
import software.amazon.awssdk.services.apigatewayv2.model.GetStagesResponse;
import software.amazon.awssdk.services.apigatewayv2.model.Stage;

/**
 * Create a stage.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::api-gateway-stage example-stage
 *         api: $(aws::api example-api)
 *         name: example-stage
 *         description: "example-desc"
 *         auto-deploy: false
 *
 *         default-route-settings
 *             detailed-metrics-enabled: true
 *             throttling-burst-limit: 2
 *             throttling-rate-limit: 2
 *         end
 *
 *         route-settings
 *             key: 'ANY /api/example/route'
 *             detailed-metrics-enabled: true
 *             throttling-burst-limit: 2
 *             throttling-rate-limit: 2
 *         end
 *
 *         stage-variables: {
 *             "exampleKey": "exampleValue"
 *         }
 *
 *         tags: {
 *             "example-key": "example-value"
 *         }
 *     end
 */
@Type("api-gateway-stage")
public class StageResource extends AwsResource implements Copyable<Stage> {

    private String name;
    private ApiAccessLogSettings accessLogSettings;
    private ApiResource api;
    private Boolean autoDeploy;
    private String clientCertificateId;
    private ApiRouteSettings defaultRouteSettings;
    private DeploymentResource deployment;
    private String description;
    private List<ApiRouteSettings> routeSettings;
    private Map<String, String> stageVariables;
    private Map<String, String> tags;

    // Output
    private String arn;

    /**
     * The name of the stage.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Settings for logging access in this stage.
     */
    @Updatable
    public ApiAccessLogSettings getAccessLogSettings() {
        return accessLogSettings;
    }

    public void setAccessLogSettings(ApiAccessLogSettings accessLogSettings) {
        this.accessLogSettings = accessLogSettings;
    }

    /**
     * The API associated with the resource.
     */
    @Required
    public ApiResource getApi() {
        return api;
    }

    public void setApi(ApiResource api) {
        this.api = api;
    }

    /**
     * If set to ``true``, updates to an API automatically trigger a new deployment.
     */
    @Updatable
    public Boolean getAutoDeploy() {
        return autoDeploy;
    }

    public void setAutoDeploy(Boolean autoDeploy) {
        this.autoDeploy = autoDeploy;
    }

    /**
     * The identifier of a client certificate for a Stage.
     */
    @Updatable
    public String getClientCertificateId() {
        return clientCertificateId;
    }

    public void setClientCertificateId(String clientCertificateId) {
        this.clientCertificateId = clientCertificateId;
    }

    /**
     * The default route settings for the stage.
     */
    @Updatable
    public ApiRouteSettings getDefaultRouteSettings() {
        return defaultRouteSettings;
    }

    public void setDefaultRouteSettings(ApiRouteSettings defaultRouteSettings) {
        this.defaultRouteSettings = defaultRouteSettings;
    }

    /**
     * The deployment of the API stage.
     */
    @Updatable
    public DeploymentResource getDeployment() {
        return deployment;
    }

    public void setDeployment(DeploymentResource deployment) {
        this.deployment = deployment;
    }

    /**
     * The description for the API stage.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Route settings for the stage, by routeKey.
     */
    @Updatable
    public List<ApiRouteSettings> getRouteSettings() {
        if (routeSettings == null) {
            routeSettings = new ArrayList<>();
        }

        return routeSettings;
    }

    public void setRouteSettings(List<ApiRouteSettings> routeSettings) {
        this.routeSettings = routeSettings;
    }

    /**
     * A map that defines the stage variables for a Stage.
     */
    @Updatable
    public Map<String, String> getStageVariables() {
        if (stageVariables == null) {
            stageVariables = new HashMap<>();
        }

        return stageVariables;
    }

    public void setStageVariables(Map<String, String> stageVariables) {
        this.stageVariables = stageVariables;
    }

    /**
     * The collection of tags.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The ARN of the stage.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(Stage model) {
        setName(model.stageName());
        setAutoDeploy(model.autoDeploy());
        setClientCertificateId(model.clientCertificateId());
        setDeployment(findById(DeploymentResource.class, model.deploymentId()));
        setDescription(model.description());
        setArn(getArnFormat());

        if (model.accessLogSettings() != null) {
            ApiAccessLogSettings logSettings = newSubresource(ApiAccessLogSettings.class);
            logSettings.copyFrom(model.accessLogSettings());
            setAccessLogSettings(logSettings);
        }

        if (model.defaultRouteSettings() != null) {
            ApiRouteSettings settings = newSubresource(ApiRouteSettings.class);
            settings.copyFrom(model.defaultRouteSettings());
            setDefaultRouteSettings(settings);
        }

        if (model.hasRouteSettings()) {
            setRouteSettings(model.routeSettings().entrySet().stream()
                .map(r -> {
                    ApiRouteSettings settings = newSubresource(ApiRouteSettings.class);
                    settings.setKey(r.getKey());
                    settings.copyFrom(r.getValue());

                    return settings;
                })
                .collect(Collectors.toList()));
        }

        if (model.hasStageVariables()) {
            setStageVariables(model.stageVariables());
        }

        if (model.hasTags()) {
            setTags(model.tags());
        }

        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);
        List<String> apis = client.getApis().items().stream().map(Api::apiId).collect(Collectors.toList());

        apis.stream().filter(a -> {
            GetStagesResponse response = client.getStages(r -> r.apiId(getApi().getId()));

            return response.hasItems() &&
                response.items()
                    .stream()
                    .filter(i -> i.stageName().equals(getName()))
                    .findFirst()
                    .orElse(null) != null;
        }).findFirst().ifPresent(apiId -> setApi(findById(ApiResource.class, apiId)));
    }

    @Override
    public boolean refresh() {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        Stage stage = getStage(client);

        if (stage == null) {
            return false;
        }

        copyFrom(stage);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.createStage(r -> r.apiId(getApi().getId())
            .accessLogSettings(getAccessLogSettings() != null ? getAccessLogSettings().toAccessLogSettings() : null)
            .autoDeploy(getAutoDeploy())
            .clientCertificateId(getClientCertificateId())
            .defaultRouteSettings(
                getDefaultRouteSettings() != null ? getDefaultRouteSettings().toRouteSettings() : null)
            .deploymentId(getDeployment() != null ? getDeployment().getId() : null)
            .description(getDescription())
            .routeSettings(getRouteSettings().stream()
                .collect(Collectors.toMap(ApiRouteSettings::getKey, ApiRouteSettings::toRouteSettings)))
            .stageName(getName())
            .stageVariables(getStageVariables())
            .tags(getTags()));

        setArn(getArnFormat());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.updateStage(r -> r.apiId(getApi().getId())
            .accessLogSettings(getAccessLogSettings() != null ? getAccessLogSettings().toAccessLogSettings() : null)
            .autoDeploy(getAutoDeploy())
            .clientCertificateId(getClientCertificateId())
            .defaultRouteSettings(
                getDefaultRouteSettings() != null ? getDefaultRouteSettings().toRouteSettings() : null)
            .deploymentId(getDeployment() != null ? getDeployment().getId() : null)
            .description(getDescription())
            .routeSettings(getRouteSettings().stream()
                .collect(Collectors.toMap(ApiRouteSettings::getKey, ApiRouteSettings::toRouteSettings)))
            .stageName(getName())
            .stageVariables(getStageVariables()));

        if (changedFieldNames.contains("tags")) {
            StageResource currentResource = (StageResource) current;

            if (!currentResource.getTags().isEmpty()) {
                client.untagResource(r -> r.resourceArn(getArn())
                    .tagKeys(currentResource.getTags().keySet())
                    .build());
            }

            client.tagResource(r -> r.resourceArn(getArn()).tags(getTags()));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.deleteStage(r -> r.apiId(getApi().getId()).stageName(getName()));
    }

    private Stage getStage(ApiGatewayV2Client client) {
        Stage stage = null;

        GetStagesResponse response = client.getStages(r -> r.apiId(getApi().getId()));

        if (response.hasItems()) {
            stage = response.items()
                .stream()
                .filter(i -> i.stageName().equals(getName()))
                .findFirst()
                .orElse(null);
        }

        return stage;
    }

    private String getArnFormat() {
        return String.format(
            "arn:aws:apigateway:%s::/apis/%s/stages/%s",
            credentials(AwsCredentials.class).getRegion(),
            getApi().getId(), getName());
    }
}
