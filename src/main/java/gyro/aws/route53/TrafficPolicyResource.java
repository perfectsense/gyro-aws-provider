/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.route53;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.CreateTrafficPolicyResponse;
import software.amazon.awssdk.services.route53.model.CreateTrafficPolicyVersionResponse;
import software.amazon.awssdk.services.route53.model.GetTrafficPolicyResponse;
import software.amazon.awssdk.services.route53.model.NoSuchTrafficPolicyException;
import software.amazon.awssdk.services.route53.model.TrafficPolicy;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Creates a Traffic Policy resource.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::route53-traffic-policy traffic-policy-example
 *         name: "traffic-policy-example"
 *         comment: "traffic-policy-example Comment"
 *         document: "policy.json"
 *     end
 *
 */
@Type("route53-traffic-policy")
public class TrafficPolicyResource extends AwsResource implements Copyable<TrafficPolicy> {
    private String name;
    private String comment;
    private String document;
    private String id;
    private Integer version;

    /**
     * The name of the Traffic Policy. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The comment you want to put with the Traffic Policy.
     */
    @Updatable
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * The policy document. Required unless document path provided.
     */
    public String getDocument() {
        if (document == null) {
            return null;
        } else if (document.endsWith(".json")) {
            try (InputStream input = openInput(document)) {
                document = IoUtils.toUtf8String(input);

            } catch (IOException ex) {
                throw new GyroException(String.format("File at path '%s' not found.", document));
            }
        }

        ObjectMapper obj = new ObjectMapper();
        try {
            JsonNode jsonNode = obj.readTree(document);
            return jsonNode.toString();
        } catch (IOException ex) {
            throw new GyroException(String.format("Could not read the json `%s`",document),ex);
        }
    }

    public void setDocument(String document) {
        this.document = document;
    }

    /**
     * The ID of the Traffic Policy.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The version of the Traffic Policy.
     */
    @Output
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public void copyFrom(TrafficPolicy trafficPolicy) {
        setId(trafficPolicy.id());
        setVersion(trafficPolicy.version());
        setName(trafficPolicy.name());
        setComment(trafficPolicy.comment());
        setDocument(trafficPolicy.document());
    }

    @Override
    public boolean refresh() {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        TrafficPolicy trafficPolicy = getTrafficPolicy(client);

        if (trafficPolicy == null) {
            return false;
        }

        setName(trafficPolicy.name());
        setComment(trafficPolicy.comment());
        setDocument(trafficPolicy.document());

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        validate(true);

        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        TrafficPolicy trafficPolicy = null;

        if (ObjectUtils.isBlank(getId())) {
            CreateTrafficPolicyResponse response = client.createTrafficPolicy(
                r -> r.name(getName())
                    .comment(getComment())
                    .document(getDocument())
            );

            trafficPolicy = response.trafficPolicy();

        } else {
            CreateTrafficPolicyVersionResponse response = client.createTrafficPolicyVersion(
                r -> r.comment(getComment())
                    .id(getId())
                    .document(getDocument())
            );

            trafficPolicy = response.trafficPolicy();
        }

        setId(trafficPolicy.id());
        setVersion(trafficPolicy.version());
        setName(trafficPolicy.name());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        validate(false);

        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        client.updateTrafficPolicyComment(
            r -> r.id(getId())
                .comment(getComment())
                .version(getVersion())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteTrafficPolicy(
            r -> r.id(getId())
                .version(getVersion())
        );
    }

    private TrafficPolicy getTrafficPolicy(Route53Client client) {
        TrafficPolicy trafficPolicy = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load traffic policy.");
        }

        if (ObjectUtils.isBlank(getVersion())) {
            throw new GyroException("version is missing, unable to load traffic policy.");
        }

        try {
            GetTrafficPolicyResponse response = client.getTrafficPolicy(
                r -> r.id(getId()).version(getVersion())
            );

            trafficPolicy = response.trafficPolicy();
        } catch (NoSuchTrafficPolicyException ignore) {
            //ignore
        }

        return trafficPolicy;
    }

    private void validate(boolean isCreate) {
        if ((ObjectUtils.isBlank(getName()) && ObjectUtils.isBlank(getId()))
            || (isCreate && !ObjectUtils.isBlank(getName()) && !ObjectUtils.isBlank(getId()))) {
            throw new GyroException("Either param 'name' or 'id' need to be provided, but not both.");
        }
    }
}
