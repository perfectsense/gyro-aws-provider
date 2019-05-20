package gyro.aws;

import com.psddev.dari.util.TypeDefinition;
import gyro.core.Credentials;
import gyro.core.resource.ResourceFinder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.ec2.model.Filter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AwsResourceFinder<C extends SdkClient, M, R extends AwsResource> implements ResourceFinder<R, M> {

    private SdkClient client;

    protected <T extends SdkClient> T createClient(Class<T> clientClass, Credentials credentials) {
        if (client == null) {
            client = AwsResource.createClient(clientClass, (AwsCredentials) credentials);
        }

        return (T) client;
    }

    protected List<Filter> createFilters(Map<String, String> query) {
        return query.entrySet().stream()
            .map(e -> Filter.builder().name(e.getKey()).values(e.getValue()).build())
            .collect(Collectors.toList());
    }

    protected abstract List<M> findAws(C client, Map<String, String> filters);

    @Override
    public final List<M> find(Credentials credentials, Map<String, String> filters) {
        TypeDefinition td = TypeDefinition.getInstance(getClass());
        Class<C> clientClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 0);
        return findAws(createClient(clientClass, credentials), filters);
    }

    protected abstract List<M> findAllAws(C client);

    @Override
    public final List<M> findAll(Credentials credentials) {
        TypeDefinition td = TypeDefinition.getInstance(getClass());
        Class<C> clientClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 0);
        return findAllAws(createClient(clientClass, credentials));
    }

    protected abstract void initAwsResource(C client, R resource, M model);

    @Override
    public void initResource(Credentials credentials, R resource, M model) {
        TypeDefinition td = TypeDefinition.getInstance(getClass());
        Class<C> clientClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 0);
        initAwsResource(createClient(clientClass, credentials), resource, model);
    }
}
