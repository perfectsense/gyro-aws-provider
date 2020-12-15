<img src="https://github.com/perfectsense/gyro/blob/master/etc/gyro.png" height="200"/>

[![Gitter](https://img.shields.io/gitter/room/perfectsense/gyro)](https://gitter.im/perfectsense/gyro)
[![TravisCI](https://api.travis-ci.com/perfectsense/gyro-aws-provider.svg?branch=master)](https://travis-ci.org/perfectsense/gyro-aws-provider)
[![Apache License 2.0](https://img.shields.io/github/license/perfectsense/gyro-aws-provider)](https://github.com/perfectsense/gyro-aws-provider/blob/master/LICENSE)


The **AWS Provider for Gyro** enables users to easily work with Amazon Web Services. The AWS provider extends Gyro allowing you to manage your AWS infrastructure.

To learn more about Gyro see [getgyro.io](https://getgyro.io) and [gyro](https://github.com/perfectsense/gyro). 

* [Resource Documentation](https://gyro.dev/providers/aws/index.html)
* [Submit an Issue](https://github.com/perfectsense/gyro-aws-provider/issues)
* [Getting Help](#getting-help)

## Using the AWS Provider

### AWS Account ###

Before you can use AWS provider, you will need an AWS account. Please see [Sign Up for AWS](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/signup-create-iam-user.html) to create an AWS Account.

Once your account is set up and ready to be used, you need to set up AWS Credentials to a credentials file or in the environment.

Define your credentials in $HOME/.aws/credentials under a profile name of your choosing:

```
[my-project]
aws_secret_access_key = <access_key>
aws_access_key_id = <access_key_id>
```

See [Setting up AWS credentials for Gyro](https://gyro.dev/providers/aws/index.html#authentication).

### Using The Provider ###

#### Import ####

Load the AWS provider in your project by consuming it as a `plugin` directive in your init file. It uses the format `@plugin: gyro:gyro-aws-provider:<version>`.

```shell
@repository: 'https://artifactory.psdops.com/gyro-releases'
@plugin: 'gyro:gyro-aws-provider:1.0.0'
```

#### Authentication ####

Provide the AWS provider with credentials by defining the following in your `.gyro/init.gyro` file:

```
@credentials 'aws::credentials'
    profile-name: 'my-project'
    region: 'us-east-1'
@end
```

See [AWS authentication for Gyro](https://gyro.dev/providers/aws/index.html#authentication) for more details.

## Supported Services

* [ACM](https://gyro.dev/providers/aws/acm/index.html)
* [ACM PCA](https://gyro.dev/providers/aws/acm-pca/index.html)
* [Autoscaling Groups](https://gyro.dev/providers/aws/autoscaling-groups/index.html)
* [Cloudfront](https://gyro.dev/providers/aws/cloudfront/index.html)
* [Cloudtrail](https://gyro.dev/providers/aws/cloudtrail/index.html)
* [Cloudwatch](https://gyro.dev/providers/aws/cloudwatch/index.html)
* [Codebuild](https://gyro.dev/providers/aws/code-build/index.html)
* [Cognito](https://gyro.dev/providers/aws/Cognito-identity-provider/index.html)
* [Data Lifecycle Manager](https://gyro.dev/providers/aws/data-lifecycle-manager/index.html)
* [Document Db](https://gyro.dev/providers/aws/document-db/index.html)
* [Dynamo DB](https://gyro.dev/providers/aws/dynamodb/index.html)
* [EC2](https://gyro.dev/providers/aws/ec2/index.html)
* [EFS](https://gyro.dev/providers/aws/efs/index.html)
* [EKS](https://gyro.dev/providers/aws/eks/index.html)
* [ElastiCache](https://gyro.dev/providers/aws/elasticache/index.html)
* [Elasticsearch](https://gyro.dev/providers/aws/elasticsearch/index.html)
* [Identity Access Management](https://gyro.dev/providers/aws/identity-access-management/index.html)
* [Kendra](https://gyro.dev/providers/aws/kendra/index.html)
* [KMS](https://gyro.dev/providers/aws/kms/index.html)
* [Lambda](https://gyro.dev/providers/aws/lambda/index.html)
* [Load Balancer](https://gyro.dev/providers/aws/load-balancer/index.html)
* [Load Balancer - Classic](https://gyro.dev/providers/aws/load-balancer---classic/index.html)
* [Neptune](https://gyro.dev/providers/aws/neptune/index.html)
* [Relational Database Service (RDS)](https://gyro.dev/providers/aws/relational-database-service-(rds)/index.html)
* [Route53](https://gyro.dev/providers/aws/route53/index.html)
* [S3](https://gyro.dev/providers/aws/s3/index.html)
* [Simple Notification Service](https://gyro.dev/providers/aws/simple-notification-service/index.html)
* [SQS](https://gyro.dev/providers/aws/sqs/index.html)
* [WAF - Global](https://gyro.dev/providers/aws/waf---global/index.html)
* [WAF - Regional](https://gyro.dev/providers/aws/waf---regional/index.html)
* [WAF - V2](https://gyro.dev/providers/aws/waf-v2/index.html)

## Developing the AWS Provider

The provider is written in Java using Gradle as the build tool.

We recommend installing [AdoptOpenJDK](https://adoptopenjdk.net/) 11 or higher if you're going to contribute to this provider. 

Gyro uses the Gradle build tool. Once you have a JDK installed building is easy, just run ./gradlew at the root of the Gyro project. This wrapper script will automatically download and install Gradle for you, then build the provider:
```shell
$ ./gradlew
Downloading https://services.gradle.org/distributions/gradle-5.2.1-all.zip
..............................................................................................................................

Welcome to Gradle 5.2.1!

Here are the highlights of this release:
 - Define sets of dependencies that work together with Java Platform plugin
 - New C++ plugins with dependency management built-in
 - New C++ project types for gradle init
 - Service injection into plugins and project extensions

For more details see https://docs.gradle.org/5.2.1/release-notes.html

Starting a Gradle Daemon, 1 stopped Daemon could not be reused, use --status for details

.
.
.

BUILD SUCCESSFUL in 17s
38 actionable tasks: 28 executed, 10 from cache
$
```

## Getting Help

* Join the Gyro community chat on [Gitter](https://gitter.im/perfectsense/gyro).
* Take a look at the [documentation](https://gyro.dev/providers/aws/index.html) for tutorial and examples.

## License

This software is open source under the [Apache License 2.0](https://github.com/perfectsense/gyro-aws-provider/blob/master/LICENSE).
