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

import java.util.Set;

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
import software.amazon.awssdk.services.apigatewayv2.model.CreateDeploymentResponse;
import software.amazon.awssdk.services.apigatewayv2.model.Deployment;
import software.amazon.awssdk.services.apigatewayv2.model.DeploymentStatus;
import software.amazon.awssdk.services.apigatewayv2.model.GetDeploymentsResponse;

/**
 * Create a deployment.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::deployment example-deployment
 *         api: $(aws::api example-api)
 *         description: "example-desc-changed"
 *         stage: $(aws::stage example-stage)
 *     end
 */
@Type("deployment")
public class DeploymentResource extends AwsResource implements Copyable<Deployment> {

    private ApiResource api;
    private String description;
    private StageResource stage;

    // Output
    private String id;
    private DeploymentStatus status;

    /**
     * The API identifier.
     */
    @Required
    public ApiResource getApi() {
        return api;
    }

    public void setApi(ApiResource api) {
        this.api = api;
    }

    /**
     * The description for the deployment resource.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The Stage resource for the Deployment resource to create.
     */
    @Required
    public StageResource getStage() {
        return stage;
    }

    public void setStage(StageResource stage) {
        this.stage = stage;
    }

    /**
     * The Id of the deployment.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Output
    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    @Override
    public void copyFrom(Deployment model) {
        setDescription(model.description());
        setId(model.deploymentId());
        setStatus(model.deploymentStatus());
    }

    @Override
    public boolean refresh() {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        Deployment deployment = getDeployment(client);

        if (deployment == null) {
            return false;
        }

        copyFrom(deployment);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        CreateDeploymentResponse deployment = client.createDeployment(r -> r.apiId(getApi().getId())
            .description(getDescription())
            .stageName(getStage().getName()));

        setId(deployment.deploymentId());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.updateDeployment(r -> r.apiId(getApi().getId()).deploymentId(getId()).description(getDescription()));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.deleteDeployment(r -> r.apiId(getApi().getId()).deploymentId(getId()));
    }

    private Deployment getDeployment(ApiGatewayV2Client client) {
        Deployment deployment = null;

        GetDeploymentsResponse deployments = client.getDeployments(r -> r.apiId(getApi().getId()));

        if (deployments.hasItems()) {
            deployment = deployments.items()
                .stream()
                .filter(i -> i.deploymentId().equals(getId()))
                .findFirst()
                .orElse(null);
        }

        return deployment;
    }
}
