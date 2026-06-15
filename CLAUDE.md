# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **AWS Provider for Gyro**, an infrastructure-as-code tool that allows users to manage AWS resources through declarative configuration files. The provider is written in Java and extends the Gyro core framework to support 40+ AWS services.

- **Website**: https://getgyro.io
- **Documentation**: https://gyro.dev/providers/aws/index.html
- **Core Framework**: https://github.com/perfectsense/gyro

## Build Commands

```bash
# Build the project (default task - runs build and publishToMavenLocal)
./gradlew

# Build and create shadow JAR
./gradlew build

# Publish to local Maven repository
./gradlew publishToMavenLocal

# Generate reference documentation
./gradlew referenceDocs

# Clean build artifacts
./gradlew clean
```

**Build Requirements:**
- JDK 11 or higher (AdoptOpenJDK recommended)
- Gradle wrapper included (will auto-download Gradle 5.2.1)
- Java 8 source/target compatibility

## Architecture Overview

### Core Design Pattern

The codebase follows a consistent pattern for implementing AWS resources:

1. **Resource Class** - Extends `AwsResource`, implements `Copyable<AwsSdkModel>`
2. **Finder Class** - Extends `AwsFinder<Client, Model, Resource>` for queries
3. **Subresources** (optional) - Extend `Diffable`, implement `Copyable<AwsSdkModel>`

### Base Classes

#### AwsResource (`gyro.aws.AwsResource`)
The foundation for all AWS resources. Key responsibilities:
- AWS SDK client creation and caching via `createClient(Class<T> clientClass)`
- Automatic retry logic with `executeService()` (10 retries, 1-second intervals)
- HTTP proxy configuration from environment variables
- Region/endpoint override support
- Global service handling (IAM → us-east-1, GlobalAccelerator → us-west-2)

#### AwsFinder (`gyro.aws.AwsFinder`)
Generic base for finding AWS resources. Type parameters: `<Client, Model, Resource>`
- `findAllAws(client)` - Query all resources
- `findAws(client, filters)` - Query with filters
- `newResource(model)` - Convert SDK model to Gyro resource
- Helper methods: `createFilters()`, `createRdsFilters()`, `createNeptuneFilters()`, etc.

#### Copyable Interface (`gyro.aws.Copyable`)
Bidirectional conversion interface:
```java
public interface Copyable<M> {
    void copyFrom(M model);  // AWS SDK model → Gyro resource
}
```

### Specialized Base Classes

#### Ec2TaggableResource (`gyro.aws.ec2.Ec2TaggableResource`)
Template method pattern for EC2 resources with tagging support:
- Automatic tag synchronization (create, update, delete)
- Name tag special handling via `getName()` / `setName()`
- Lazy tag loading with `refreshTags()`
- Differential updates using Google Guava's `MapDifference`
- Subclasses implement `doCreate()` and `doUpdate()` instead of `create()` and `update()`

#### WafTaggableResource (`gyro.aws.wafv2.WafTaggableResource`)
Similar pattern for WAFv2 resources:
- Uses WAFv2-specific tag APIs
- Requires `scope` field (CLOUDFRONT or REGIONAL)
- Uses ARN instead of resource ID

### Package Structure

```
src/main/java/gyro/aws/
├── AwsResource.java          # Base resource class
├── AwsFinder.java            # Base finder class
├── Copyable.java             # Conversion interface
├── AwsCredentials.java       # Credentials management
├── DynamoDbLockBackend.java  # Distributed locking
├── S3FileBackend.java        # Remote state storage
└── [service-name]/           # Service-specific packages
    ├── *Resource.java        # Resource implementations
    ├── *Finder.java          # Resource finders
    └── *.java                # Subresources (Diffable)
```

**Services include**: acm, apigatewayv2, autoscaling, backup, cloudfront, cloudtrail, cloudwatch, codebuild, cognitoidp, dax, dlm, docdb, dynamodb, ec2, ecr, ecs, efs, eks, elasticache, elasticsearch, eventbridge, globalaccelerator, iam, kendra, kms, lambda, loadbalancer, neptune, opensearch, rds, route53, s3, sns, sqs, waf, wafv2, and more.

## Implementing a New AWS Resource

### 1. Create Resource Class

```java
@Type("service-resource-name")  // e.g., "lambda-function"
public class ResourceName extends AwsResource implements Copyable<AwsSdkModel> {

    private String name;
    private String arn;

    @Id
    public String getArn() {
        return arn;
    }

    @Required
    public String getName() {
        return name;
    }

    @Override
    public void copyFrom(AwsSdkModel model) {
        setArn(model.arn());
        setName(model.name());
    }

    @Override
    public boolean refresh() {
        // Fetch current state from AWS
        // Return false if resource no longer exists
    }

    @Override
    public void create(GyroUI ui, State state) {
        // Create resource in AWS using createClient(ClientClass.class)
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        // Update only changed fields
    }

    @Override
    public void delete(GyroUI ui, State state) {
        // Delete resource from AWS
    }
}
```

### 2. Create Finder Class

```java
@Type("service-resource-name")  // Must match resource @Type
public class ResourceFinder extends AwsFinder<ClientClass, SdkModel, ResourceName> {

    @Override
    protected List<SdkModel> findAllAws(ClientClass client) {
        return client.listResourcesPaginator()
            .resources()
            .stream()
            .collect(Collectors.toList());
    }

    @Override
    protected List<SdkModel> findAws(ClientClass client, Map<String, String> filters) {
        // Implement filtered queries
    }
}
```

### 3. Create Subresources (Optional)

For complex nested structures:

```java
public class SubresourceName extends Diffable implements Copyable<SdkModel> {

    private String field;

    @Override
    public String primaryKey() {
        return field;  // Unique identifier for diff tracking
    }

    @Override
    public void copyFrom(SdkModel model) {
        setField(model.field());
    }

    public SdkModel toSdkModel() {
        return SdkModel.builder()
            .field(getField())
            .build();
    }
}
```

### Key Annotations

- `@Type("resource-name")` - Resource type identifier
- `@Id` - Unique identifier field
- `@Required` - Required field for creation
- `@Updatable` - Field can be updated after creation
- `@Output` - Read-only computed field
- `@ValidStrings`, `@Range`, `@CollectionMax` - Validation

### Common Patterns

**Creating AWS SDK Clients:**
```java
ServiceClient client = createClient(ServiceClient.class);
```

**Retry with Backoff:**
```java
executeService(() -> {
    // AWS API call that may need retry
});
```

**Eventual Consistency:**
```java
Wait.atMost(Duration.ofMinutes(2))
    .checkEvery(5, TimeUnit.SECONDS)
    .until(() -> {
        // Check condition
    });
```

**Differential Updates (for tags/maps):**
```java
MapDifference<String, String> diff = Maps.difference(oldMap, newMap);
if (!diff.entriesOnlyOnRight().isEmpty()) {
    // Add new entries
}
if (!diff.entriesOnlyOnLeft().isEmpty()) {
    // Remove old entries
}
```

**Policy JSON from Files:**
```java
String policy = getPolicyDocument() != null
    ? getPolicyDocument().stream().map(s -> s + "\n").collect(Collectors.joining())
    : null;
```

## Working with Taggable Resources

For EC2 resources that support tagging, extend `Ec2TaggableResource`:

```java
public class MyResource extends Ec2TaggableResource<MyModel> {

    @Override
    protected String getResourceId() {
        return getId();  // Return EC2 resource ID
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        // Create logic (tags handled automatically)
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        // Update logic (tags handled automatically)
    }
}
```

## Testing Locally

To test changes:

1. Build the provider: `./gradlew build`
2. Publish to local Maven: `./gradlew publishToMavenLocal`
3. In a test Gyro project, reference the local version in `.gyro/init.gyro`:
```
@repository: 'mavenLocal'
@plugin: 'gyro:gyro-aws-provider:1.10.0-SNAPSHOT'
```

## Important Implementation Notes

- **Client Caching**: Clients are cached per (ClientClass, Credentials, Region, Endpoint) tuple
- **Global Services**: IAM always uses us-east-1; GlobalAccelerator uses us-west-2
- **Retry Policy**: All clients configured with 20 retries on throttling
- **Shadow JAR**: Common dependencies are relocated to avoid conflicts (Jackson, Guava, JSON)
- **AWS SDK Version**: Uses AWS SDK v2 with BOM version 2.31.69

## Dependencies and State Management

- **gyro-core**: Version 1.3.0 (core framework)
- **Google Guava**: Version 23.0 (utilities, differential updates)
- **Jackson**: Version 2.13.0 (JSON processing)
- **AWS SDK v2**: BOM 2.31.69 (all AWS service clients)
- **DynamoDB**: Distributed locking for state
- **S3**: Remote state storage backend

## Resource Lifecycle

1. **Read Phase** (`refresh()`): Fetch current state from AWS, update fields
2. **Plan Phase**: Gyro core compares desired vs. actual state
3. **Apply Phase**:
   - New resources → `create()`
   - Changed resources → `update()` with `changedFieldNames`
   - Removed resources → `delete()`

## Common Development Tasks

**Adding a new AWS service:**
1. Create package: `src/main/java/gyro/aws/[service-name]/`
2. Add AWS SDK dependency to `build.gradle`
3. Implement Resource and Finder classes
4. Add examples to `examples/[service-name]/`
5. Build and test locally

**Updating to new AWS SDK version:**
1. Update BOM version in `build.gradle`: `software.amazon.awssdk:bom:X.X.X`
2. Review breaking changes in AWS SDK changelog
3. Update affected resource implementations
4. Test thoroughly

**Debugging resource issues:**
- Check AWS SDK client configuration in `AwsResource.createClient()`
- Verify `copyFrom()` maps all necessary fields
- Ensure `refresh()` handles non-existent resources (return false)
- Check for eventual consistency issues (use `Wait.atMost()`)
