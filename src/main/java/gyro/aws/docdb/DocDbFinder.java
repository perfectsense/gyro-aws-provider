package gyro.aws.docdb;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.docdb.model.Filter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DocDbFinder<C extends SdkClient, M, R extends AwsResource> extends AwsFinder<C, M, R> {

    public List<Filter> createDocDbFilters(Map<String, String> filters) {
        return filters.entrySet().stream()
            .map(e -> Filter.builder().name(e.getKey()).values(e.getValue()).build())
            .collect(Collectors.toList());
    }

}
