package gyro.aws.apigatewayv2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.Api;
import software.amazon.awssdk.services.apigatewayv2.model.GetApisRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetApisResponse;

public abstract class ApiGatewayFinder<C extends SdkClient, M, R extends AwsResource> extends AwsFinder<C, M, R> {

    public List<String> getApis(ApiGatewayV2Client client) {
        List<Api> apis = new ArrayList<>();
        String marker = null;
        GetApisResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.getApis();
            } else {
                response = client.getApis(GetApisRequest.builder().nextToken(marker).build());
            }

            marker = response.nextToken();
            apis.addAll(response.items());
        } while (!ObjectUtils.isBlank(marker));

        return apis.stream().map(Api::apiId).collect(Collectors.toList());
    }
}
