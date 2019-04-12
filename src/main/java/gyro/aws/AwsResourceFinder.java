package gyro.aws;

import com.psddev.dari.util.TypeDefinition;
import gyro.core.GyroException;
import gyro.core.Credentials;
import gyro.core.resource.ResourceFinder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.ec2.model.Filter;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AwsResourceFinder<C extends SdkClient, A, R extends AwsResource> implements ResourceFinder<R> {

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

    protected abstract List<A> findAws(C client, Map<String, String> filters);

    @Override
    public final List<R> find(Credentials credentials, Map<String, String> filters) {
        TypeDefinition td = TypeDefinition.getInstance(getClass());
        Class<C> clientClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 0);
        return findAws(createClient(clientClass, credentials), filters).stream().map(this::createResource).collect(Collectors.toList());
    }

    protected abstract List<A> findAllAws(C client);

    @Override
    public final List<R> findAll(Credentials credentials) {
        TypeDefinition td = TypeDefinition.getInstance(getClass());
        Class<C> clientClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 0);
        return findAllAws(createClient(clientClass, credentials)).stream().map(this::createResource).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private R createResource(A model) {
        TypeDefinition td = TypeDefinition.getInstance(getClass());
        Class<C> clientClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 0);
        Class<A> modelClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 1);
        Class<R> resourceClass = td.getInferredGenericTypeArgumentClass(AwsResourceFinder.class, 2);

        try {
            return resourceClass.getConstructor(clientClass, modelClass).newInstance(client, model);
        } catch (NoSuchMethodException nme) {
            throw new GyroException(String.format("No constructor %s(%s, %s) is defined!", resourceClass, clientClass, modelClass));
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new GyroException(String.format("Unable to create resource of [%s]", resourceClass), e);
        }
    }
}
