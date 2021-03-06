CHANGELOG
=========

## 1.3.0 (July 14th, 2021)

ENHANCEMENTS:

* [489](https://github.com/perfectsense/gyro-aws-provider/issues/489): Add support for Global Accelerators.

ISSUES FIXED:

* [497](https://github.com/perfectsense/gyro-aws-provider/issues/497): Remove required annotation from key-pair field for EC2 Instance.
* [498](https://github.com/perfectsense/gyro-aws-provider/issues/498): Remove enum validation of type for EC2 Instance.

## 1.2.2 (June 28th, 2021)

ENHANCEMENTS:

* [464](https://github.com/perfectsense/gyro-aws-provider/issues/464): Expose endpoint and certificate-authority-data for EKS cluster.
* [487](https://github.com/perfectsense/gyro-aws-provider/pull/487): Allow "gp3" volume type for EC2 BlockDeviceMapping.
* [488](https://github.com/perfectsense/gyro-aws-provider/pull/488): Allow "CUSTOM" ami type for EKS Nodegroup.
* [490](https://github.com/perfectsense/gyro-aws-provider/issues/490): Allow updating image-tag-mutability and image-scanning-configuration for ECR repository.

## 1.2.1 (June 21st, 2021)

ISSUES FIXED:

* [476](https://github.com/perfectsense/gyro-aws-provider/issues/476): Fix EKS voc-config update error.
* [478](https://github.com/perfectsense/gyro-aws-provider/issues/478): Fix EKS addon null display.
* [479](https://github.com/perfectsense/gyro-aws-provider/issues/479): Fix EKS authentication null display.
* [480](https://github.com/perfectsense/gyro-aws-provider/issues/480): Fix missing documentation for Launch Template tag-specification.

## 1.2.0 (June 8th, 2021)

ENHANCEMENTS:

* [472](https://github.com/perfectsense/gyro-aws-provider/issues/472): Add support for cleaning AWS client cache.
* [466](https://github.com/perfectsense/gyro-aws-provider/issues/466): Convert EKS Addon as a subresource.
* [462](https://github.com/perfectsense/gyro-aws-provider/issues/462): Add support for EKS Authentication.
* [440](https://github.com/perfectsense/gyro-aws-provider/issues/440): Add support for additional fields in Launch Template.

ISSUES FIXED:

* [468](https://github.com/perfectsense/gyro-aws-provider/issues/468): Fix ECR lifecycle update.

## 1.1.0 (April 27th, 2021)

ENHANCEMENTS:

* [361](https://github.com/perfectsense/gyro-aws-provider/issues/361): Add support for creating EKS Nodegroup Launch Template.
* [407](https://github.com/perfectsense/gyro-aws-provider/issues/407): Add support for creating RDS cluster/instance from snapshots.
* [418](https://github.com/perfectsense/gyro-aws-provider/issues/418): Add support for Backup.
* [448](https://github.com/perfectsense/gyro-aws-provider/issues/448): Allow ALB forwarding action to target multiple groups by weight.
* [450](https://github.com/perfectsense/gyro-aws-provider/issues/450): Allow EKS to associate a KMS key after creation.

ISSUES FIXED:

* [404](https://github.com/perfectsense/gyro-aws-provider/issues/404): Fix Route53 Record Set refresh error.
* [420](https://github.com/perfectsense/gyro-aws-provider/issues/420): Update EKS Nodegroup default desired-capacity.
* [421](https://github.com/perfectsense/gyro-aws-provider/issues/421): Fix delayed wait time for VPC Endpoint.
* [425](https://github.com/perfectsense/gyro-aws-provider/issues/425): Fix EKS Nodegroup save error.
* [427](https://github.com/perfectsense/gyro-aws-provider/issues/427): Fix WAFV2 refresh error when associated with a Cloudfront.
* [428](https://github.com/perfectsense/gyro-aws-provider/issues/428): Fix WAFV2 scoping error.
* [431](https://github.com/perfectsense/gyro-aws-provider/issues/431): Update EKS examples.
* [432](https://github.com/perfectsense/gyro-aws-provider/issues/432): Format assume role policy examples.
* [437](https://github.com/perfectsense/gyro-aws-provider/issues/437): Fix Cloudfront list field confusing change output.
* [438](https://github.com/perfectsense/gyro-aws-provider/issues/438): Have ELBV2 expose hosted-zone-id.

## 1.0.2 (March 5th, 2021)

ISSUES FIXED:

* [410](https://github.com/perfectsense/gyro-aws-provider/issues/410): Fix NPE when searching for AMI using external query.
* [412](https://github.com/perfectsense/gyro-aws-provider/issues/412): Fix updating instance type when starting an instance.

## 1.0.1 (March 3rd, 2021)

ENHANCEMENTS:

* [164](https://github.com/perfectsense/gyro-aws-provider/issues/164): Add support for IPV6 addressing.
* [174](https://github.com/perfectsense/gyro-aws-provider/issues/174): Add support for IAM OIDC provider.
* [289](https://github.com/perfectsense/gyro-aws-provider/issues/289): Add support for ECR.
* [358](https://github.com/perfectsense/gyro-aws-provider/issues/358): Add support for EKS Add-ons.
* [366](https://github.com/perfectsense/gyro-aws-provider/issues/366): Add support for ALB condition resource.
* [381](https://github.com/perfectsense/gyro-aws-provider/issues/381): Add support for OriginShield in Cloudfront.
* [389](https://github.com/perfectsense/gyro-aws-provider/issues/389): Add support for Vpn connection in Transit Gateway.

ISSUES FIXED:

* [355](https://github.com/perfectsense/gyro-aws-provider/issues/355): Fix NAT Gateway creation.
* [356](https://github.com/perfectsense/gyro-aws-provider/issues/356): Fix NAT Gateway refresh.
* [359](https://github.com/perfectsense/gyro-aws-provider/issues/359): Fix EFS nodegroup refresh.
* [362](https://github.com/perfectsense/gyro-aws-provider/issues/362): Fix refresh of IAM policy.
* [365](https://github.com/perfectsense/gyro-aws-provider/issues/365): Fix NPE when refreshing multiple resources.
* [371](https://github.com/perfectsense/gyro-aws-provider/issues/371): Add support for updating EKS nodegroup labels.
* [370](https://github.com/perfectsense/gyro-aws-provider/issues/370): Reduce wait time for eks nodegroup.
* [375](https://github.com/perfectsense/gyro-aws-provider/issues/375): Fix RDS cluster param group refresh.
* [382](https://github.com/perfectsense/gyro-aws-provider/issues/382): Fix autoscaling group desired-capacity auto update.
* [398](https://github.com/perfectsense/gyro-aws-provider/issues/398): Fix Security Group delete when using @uses-credentials.

## 1.0.0 (January 13th, 2021)

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
* [365](https://github.com/perfectsense/gyro-aws-provider/issues/365): Fix NPE when refreshing multiple resources.

RELEASE NOTES:

Version `1.0.0` of Gyro AWS provider is not compatible with Gyro prior to version `1.0.0`.
If you are using an older Gyro version and encounter errors of the following nature, please update to version `1.0.0` or higher.

```
aws::acm-certificate prod-frontend-load-balancer → options certificate options → preference: Must be one of ENABLED, DISABLED
In prod/frontend.gyro on line 59 from column 9 to 27
58:     options
59:         preference: ENABLED
60:     end
```


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
