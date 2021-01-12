CHANGELOG
=========

## 1.0.0 (January 7th, 2021)

ENHANCEMENTS: 

* [247](https://github.com/perfectsense/gyro-aws-provider/issues/247): Add support for EFS.
* [247](https://github.com/perfectsense/gyro-aws-provider/issues/250): Add support for DynamoDb Accelerator.
* [258](https://github.com/perfectsense/gyro-aws-provider/issues/258): Add support for WAF V2.
* [284](https://github.com/perfectsense/gyro-aws-provider/issues/284): Add support for CodeBuild.
* [285](https://github.com/perfectsense/gyro-aws-provider/issues/285): Add support for Kendra.
* [286](https://github.com/perfectsense/gyro-aws-provider/issues/286): Add support for Api Gateway V2.
* [287](https://github.com/perfectsense/gyro-aws-provider/issues/287): Add support for Autoscale Plan.
* [318](https://github.com/perfectsense/gyro-aws-provider/issues/318): Update KMS references to resource references.
* [329](https://github.com/perfectsense/gyro-aws-provider/issues/329): Update IAM role reference examples to be gyro managed examples.

ISSUES FIXED:

* [305](https://github.com/perfectsense/gyro-aws-provider/issues/305): Fix Network Acl refresh error.
* [310](https://github.com/perfectsense/gyro-aws-provider/issues/310): Allow passing credentials to S3 FileBackend.
* [313](https://github.com/perfectsense/gyro-aws-provider/issues/313): Fix SQS copyFrom for external references.
* [323](https://github.com/perfectsense/gyro-aws-provider/issues/323): Fix compilation on Windows.
* [335](https://github.com/perfectsense/gyro-aws-provider/issues/335): Gracefully handle `Request limit exceed` exception.
* [362](https://github.com/perfectsense/gyro-aws-provider/issues/362): Fix IAM policy refreshing older version.

## 0.99.3 (August 25th, 2020)

ENHANCEMENTS:

* [294](https://github.com/perfectsense/gyro-aws-provider/issues/294): Add `exists(String file)` and `copy(String source, String dest)` methods to FileBackend.
* [299](https://github.com/perfectsense/gyro-aws-provider/issues/299): Update SNS Subscription resource.

## 0.99.2 (August 5th, 2020)

ENHANCEMENTS:

* [270](https://github.com/perfectsense/gyro-aws-provider/issues/270): Add ability to associate a Regional WAF to an ALB.

ISSUES FIXED:

* [265](https://github.com/perfectsense/gyro-aws-provider/issues/265): Allow for Instance type changes of non-autoscale instances.
* [268](https://github.com/perfectsense/gyro-aws-provider/issues/268): Fix S3 replication config error.
* [272](https://github.com/perfectsense/gyro-aws-provider/issues/272): Fix SNS Topic refresh error.
* [273](https://github.com/perfectsense/gyro-aws-provider/issues/273): Fix CloudFront origin refresh error.
* [296](https://github.com/perfectsense/gyro-aws-provider/issues/296): Fix SNS Subscription refresh error.

## 0.99.1 (May 20th, 2020)

ENHANCEMENTS:

* [171](https://github.com/perfectsense/gyro-aws-provider/issues/171): Add support for Neptune.
* [172](https://github.com/perfectsense/gyro-aws-provider/issues/172): Add support for DynamoDb Table.
* [203](https://github.com/perfectsense/gyro-aws-provider/issues/203): Add support for EC2 Placement Group.
* [204](https://github.com/perfectsense/gyro-aws-provider/issues/204): Add support for EC2 Transit Gateway.
* [208](https://github.com/perfectsense/gyro-aws-provider/issues/208): Add support for Elastisearch Domain.
* [213](https://github.com/perfectsense/gyro-aws-provider/issues/213): Add support for EKS.
* [226](https://github.com/perfectsense/gyro-aws-provider/issues/226): Add option to add SSL Cipher policy on ELB.
* [228](https://github.com/perfectsense/gyro-aws-provider/issues/228): Add support for EC2 VPC Flow Log.
* [229](https://github.com/perfectsense/gyro-aws-provider/issues/229): Add option to add bucket level encryption to S3 Buckets.
* [237](https://github.com/perfectsense/gyro-aws-provider/issues/237): Add support for Cloudtrail.
* [241](https://github.com/perfectsense/gyro-aws-provider/issues/241): Add support to add Bucket policy for S3.

ISSUES FIXED:

* [191](https://github.com/perfectsense/gyro-aws-provider/issues/191): Allow inline policies for IAM Roles.
* [195](https://github.com/perfectsense/gyro-aws-provider/issues/195): Fix needing multiple gyro up for S3 bucket with replication configuration.
* [196](https://github.com/perfectsense/gyro-aws-provider/issues/196): Fix auto reordering of records in Route53 record sets.
* [200](https://github.com/perfectsense/gyro-aws-provider/issues/200): Allow EC2 Security groups to set rules referencing itself.
* [210](https://github.com/perfectsense/gyro-aws-provider/issues/210): Fix NPE when refreshing EC2 Security groups.
* [211](https://github.com/perfectsense/gyro-aws-provider/issues/211): Fix NPE when refreshing Route53 Hosted zone.
* [212](https://github.com/perfectsense/gyro-aws-provider/issues/212): Allow finding external EC2 Security group rules.
* [231](https://github.com/perfectsense/gyro-aws-provider/issues/231): Fix exception when enabling S3 Access logging.
* [245](https://github.com/perfectsense/gyro-aws-provider/issues/245): Add missing package-info files.

 MISC:

* [214](https://github.com/perfectsense/gyro-aws-provider/issues/214): Shade external dependencies to avoid version mismatch.
* [223](https://github.com/perfectsense/gyro-aws-provider/issues/223): Allow listing of files for S3 Filebackend.
* [232](https://github.com/perfectsense/gyro-aws-provider/issues/232): Implement state file locking using Dynamodb.
* [245](https://github.com/perfectsense/gyro-aws-provider/issues/245): Add documentation for state locking and remote storage.

## 0.99.0 (October 7th, 2019)

ISSUES FIXED:

* [158](https://github.com/perfectsense/gyro-aws-provider/issues/158): Add tags support to Classic ELB.
* [162](https://github.com/perfectsense/gyro-aws-provider/issues/162): ACM Certificate Validation Domain option throws an error if 'validation-domain' not provided.
* [159](https://github.com/perfectsense/gyro-aws-provider/issues/159): Route53 alias and geo-location should be complex types.
* [160](https://github.com/perfectsense/gyro-aws-provider/issues/160): Changing subnets in ASG breaks the update.
* [182](https://github.com/perfectsense/gyro-aws-provider/issues/182): cloudwatch/EventRuleResource, ec2/NetworkInterfaceResource and ec2/RouteTableResource don't generate example docs.
* [187](https://github.com/perfectsense/gyro-aws-provider/issues/187): Add copyright license to java and gradle files.
* [193](https://github.com/perfectsense/gyro-aws-provider/issues/193): Allow updates to CloudFront Origin
* [254](https://github.com/perfectsense/gyro-aws-provider/issues/254): Allow /16 through /32 IPv4 CIDR ranges for Classic WAF
