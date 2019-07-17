package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.cloudfront.model.LambdaFunctionAssociation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CloudFrontCacheBehaviorLambdaFunction extends Diffable implements Copyable<LambdaFunctionAssociation> {

    private String eventType;
    private String arn;
    private Boolean includeBody;

    private static final Set<String> EventType =
        new HashSet<>(Arrays.asList("viewer-request", "viewer-response", "origin-request", "origin-response"));

    @Updatable
    public String getEventType() {
        if (eventType == null) {
            eventType = "";
        }

        return eventType.toLowerCase();
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Updatable
    public String getArn() {
        if (arn == null) {
            arn = "";
        }

        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Updatable
    public Boolean getIncludeBody() {
        if (includeBody == null) {
            includeBody = false;
        }

        return includeBody;
    }

    public void setIncludeBody(Boolean includeBody) {
        this.includeBody = includeBody;
    }

    @Override
    public void copyFrom(LambdaFunctionAssociation lambdaFunctionAssociation) {
        setEventType(lambdaFunctionAssociation.eventTypeAsString());
        setArn(lambdaFunctionAssociation.lambdaFunctionARN());
        setIncludeBody(lambdaFunctionAssociation.includeBody());
    }

    @Override
    public String primaryKey() {
        return getEventType();
    }

    LambdaFunctionAssociation toLambdaFunctionAssociation() {
        return LambdaFunctionAssociation.builder()
            .eventType(getEventType())
            .includeBody(getIncludeBody())
            .lambdaFunctionARN(getArn())
            .build();
    }
}
