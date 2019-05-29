package gyro.aws;

import com.psddev.dari.util.TypeDefinition;
import gyro.core.finder.Finder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.ec2.model.Filter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AwsFinder<C extends SdkClient, M, R extends AwsResource> extends Finder<R> {

    protected abstract List<M> findAllAws(C client);

    @Override
    public final List<R> findAll() {
        return findAllAws(newClient()).stream()
            .map(this::newResource)
            .collect(Collectors.toList());
    }

    private C newClient() {
        @SuppressWarnings("unchecked")
        Class<C> clientClass = (Class<C>) TypeDefinition.getInstance(getClass())
            .getInferredGenericTypeArgumentClass(AwsFinder.class, 0);

        return AwsResource.createClient(clientClass, credentials(AwsCredentials.class));
    }

    @SuppressWarnings("unchecked")
    private R newResource(M model) {
        R resource = newResource();

        if (resource instanceof Copyable) {
            ((Copyable<M>) resource).copyFrom(model);
        }

        return resource;
    }


    protected abstract List<M> findAws(C client, Map<String, String> filters);

    @Override
    public final List<R> find(Map<String, String> filters) {
        return findAws(newClient(), filters).stream()
            .map(this::newResource)
            .collect(Collectors.toList());
    }

    public List<Filter> createFilters(Map<String, String> query) {
        return query.entrySet().stream()
            .map(e -> Filter.builder().name(e.getKey()).values(e.getValue()).build())
            .collect(Collectors.toList());
    }

    public List<software.amazon.awssdk.services.rds.model.Filter> createRdsFilters(Map<String, String> query) {
        return query.entrySet().stream()
            .map(e -> software.amazon.awssdk.services.rds.model.Filter.builder().name(e.getKey()).values(e.getValue()).build())
            .collect(Collectors.toList());
    }
}
