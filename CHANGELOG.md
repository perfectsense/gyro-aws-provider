CHANGELOG
=========

## 1.9.4 (November 20th, 2025)

ENHANCEMENTS:

* [741](https://github.com/perfectsense/gyro-aws-provider/pull/741): Mark the targetOriginId as Updatable in the CloudFrontCacheBehaviour
* [742](https://github.com/perfectsense/gyro-aws-provider/pull/742): Detach Role policies before attaching new ones in order to account for the 10 policy limit

## 1.9.3 (October 27th, 2025)

ENHANCEMENTS:

* [738](https://github.com/perfectsense/gyro-aws-provider/issues/738): Add support for scope-down-statement in managed-rule-group-statement

## 1.9.2 (October 10th, 2025)

ENHANCEMENTS:

* [734](https://github.com/perfectsense/gyro-aws-provider/pull/734): Add support for CloudFront origin groups
* [735](https://github.com/perfectsense/gyro-aws-provider/pull/735): Update DbGlobalClusterResource.java to support endpoint-address
* [736](https://github.com/perfectsense/gyro-aws-provider/pull/736): Add a wait after updating nodegroups

ISSUES FIXED:

* [733](https://github.com/perfectsense/gyro-aws-provider/pull/733): Remove incorrect aurora validation

## 1.9.1 (August 21th, 2025)

ENHANCEMENTS:

* [723](https://github.com/perfectsense/gyro-aws-provider/pull/723): Make engine version updatable for rds clusters

ISSUES FIXED:

* [729](https://github.com/perfectsense/gyro-aws-provider/issues/729): masterUsername should not be required for db-clusters

## 1.9.0 (July 9th, 2025)

ENHANCEMENTS:

* [726](https://github.com/perfectsense/gyro-aws-provider/issues/726): Add support for Ssm Parameter store
* [715](https://github.com/perfectsense/gyro-aws-provider/issues/715): Add Lambda version support

## 1.8.0 (June 5th, 2025)

ENHANCEMENTS:

* [603](https://github.com/perfectsense/gyro-aws-provider/issues/603): Add support for AWS KMS Multi-Region Keys
* [709](https://github.com/perfectsense/gyro-aws-provider/issues/709): Add cross-regions Vpc Peering functionality
* [712](https://github.com/perfectsense/gyro-aws-provider/issues/712): Add OpenSearch Outbound Connection for cross-cluster replication

ISSUES FIXED:

* [719](https://github.com/perfectsense/gyro-aws-provider/pull/719): Use the ARN as the ID for eks clusters instead of the name
* [721](https://github.com/perfectsense/gyro-aws-provider/pull/721): Use arn as the `@Id` for rds resources
* [717](https://github.com/perfectsense/gyro-aws-provider/pull/717): Return an empty list instead of erroring out if a load balancer is not found

## 1.7.0 (April 2nd, 2025)

ENHANCEMENTS:

* [705](https://github.com/perfectsense/gyro-aws-provider/issues/705): Adjust rate-limit validation to a minimum of 10 on rate-based-statements
* [655](https://github.com/perfectsense/gyro-aws-provider/issues/655): WafV2 Rules no longer need to be in an incremental priority
* [692](https://github.com/perfectsense/gyro-aws-provider/issues/692): Add support for Open search Serverless
* [693](https://github.com/perfectsense/gyro-aws-provider/issues/693): Add support for Open Search

## 1.6.9 (March 19th, 2025)

ENHANCEMENTS:

* [700](https://github.com/perfectsense/gyro-aws-provider/issues/700): Add rule action overrides for wafv2 rules

ISSUES FIXED:

* [701](https://github.com/perfectsense/gyro-aws-provider/pull/701): Increase wait time for db instance deletion to match creation

## 1.6.8 (January 23rd, 2025)

ENHANCEMENTS:

* [697](https://github.com/perfectsense/gyro-aws-provider/issues/697): Add support for http2and3 for the Cloudfront resource

## 1.6.7 (January 7th, 2025)

ENHANCEMENTS:

* [691](https://github.com/perfectsense/gyro-aws-provider/issues/691): Add support for Lambda Function Permissions
* [688](https://github.com/perfectsense/gyro-aws-provider/pull/688): Makes Serverless V2 Scaling Configs updatable
* [695](https://github.com/perfectsense/gyro-aws-provider/issues/695): Add HTTP/3 Support for Amazon CloudFront

ISSUES FIXED:

* [687](https://github.com/perfectsense/gyro-aws-provider/issues/687): Fix updating Cloudwatch log export config

## 1.6.6 (November 19th, 2024)

ISSUES FIXED:

* [683](https://github.com/perfectsense/gyro-aws-provider/pull/683): Updates Lambda Layer supported Runtimes and removes the validations

## 1.6.5 (November 15th, 2024)

ISSUES FIXED:

* [681](https://github.com/perfectsense/gyro-aws-provider/issues/681): Updates Lambda Function supported Runtimes and removes the validations

## 1.6.4 (November 12th, 2024)

ENHANCEMENTS:

* [667](https://github.com/perfectsense/gyro-aws-provider/issues/667): Add writer endpoint type For database clusters
* [668](https://github.com/perfectsense/gyro-aws-provider/issues/668): Allow DB Instance to be edited
* [670](https://github.com/perfectsense/gyro-aws-provider/issues/670): The master username should be a required field
* [673](https://github.com/perfectsense/gyro-aws-provider/issues/673): Convert db-subnet-group subnets into a list
* [677](https://github.com/perfectsense/gyro-aws-provider/issues/677): Enable Point in time recovery for db clusters

ISSUES FIXED:

* [669](https://github.com/perfectsense/gyro-aws-provider/issues/669): Sorts the availability zones of db-cluster so it doesn't update every time
* [671](https://github.com/perfectsense/gyro-aws-provider/issues/671): Fixes null pointer exceptions with db-cluster
* [672](https://github.com/perfectsense/gyro-aws-provider/issues/672): Paginate the refresh call for DbClusterParameterGroupResource to pull all parameters
* [676](https://github.com/perfectsense/gyro-aws-provider/issues/676): Fix null pointer upon db-cluster deletion

## 1.6.3 (September 9th, 2024)

ENHANCEMENTS:

* [657](https://github.com/perfectsense/gyro-aws-provider/pull/657): Add support for aurora serverless v2

ISSUES FIXED:

* [656](https://github.com/perfectsense/gyro-aws-provider/issues/656): Remove collection max for web acl rules

## 1.6.2 (August 13th, 2024)

ISSUES FIXED:

* [653](https://github.com/perfectsense/gyro-aws-provider/pull/653): Fix issue with CloudWatch Dimension field.

## 1.6.1 (July 19th, 2024)

ISSUES FIXED:

* [651](https://github.com/perfectsense/gyro-aws-provider/pull/651): Fix issue with EKS Taint updates

## 1.6.0 (June 18th, 2024)

ENHANCEMENTS:

* [604](https://github.com/perfectsense/gyro-aws-provider/issues/604): Implement Cloudfront Policies.

## 1.5.10 (May 22nd, 2024)

ENHANCEMENTS:

* [644](https://github.com/perfectsense/gyro-aws-provider/issues/644): Implement node Taints on managed node groups.

## 1.5.9 (March 13th, 2024)

ISSUES FIXED:

* [642](https://github.com/perfectsense/gyro-aws-provider/issues/642): Fix DynamoDB Validation. Fails on valid PAY_PER_REQUEST Table and allows incorrect PROVISIONED tables.

## 1.5.8 (March 8th, 2024)

ENHANCEMENTS:

* [637](https://github.com/perfectsense/gyro-aws-provider/issues/637): Add support for evaluating-window for Rate Limit Criteria in WafV2.

ISSUES FIXED:

* [636](https://github.com/perfectsense/gyro-aws-provider/issues/636): Fix Credential should be scoped to a valid region Error of Iam Clients.

## 1.5.7 (March 7th, 2024)

ENHANCEMENTS:

* [632](https://github.com/perfectsense/gyro-aws-provider/issues/632): Add support in WafV2 for Regex Match Statement.
* [630](https://github.com/perfectsense/gyro-aws-provider/issues/630): Add support for WafV2 Forwarded IP Config options for IP Set Refernece.
* [628](https://github.com/perfectsense/gyro-aws-provider/issues/628): Add support for additional attributes for Subnet.
* [626](https://github.com/perfectsense/gyro-aws-provider/issues/626): Add support for WafV2 Additional Field_To_Match options for Statements.
* [625](https://github.com/perfectsense/gyro-aws-provider/issues/625): Add support for WafV2 additional options for TextTransformations.
* [622](https://github.com/perfectsense/gyro-aws-provider/issues/622): Add support for additional action options for wafv2.
* [621](https://github.com/perfectsense/gyro-aws-provider/issues/621): Add support for exposing ipv6 cidr block for vpc when auto assigned .
* [620](https://github.com/perfectsense/gyro-aws-provider/issues/620): Add support for name and tag fields for aws::egress-only-internet-gateway.
* [617](https://github.com/perfectsense/gyro-aws-provider/issues/617): Add support for rate limit request aggregation keys in WAFv2.
* [616](https://github.com/perfectsense/gyro-aws-provider/issues/616): Add support for AWS WafV2 LabelMatchStatement Statements.

## 1.5.6 (August 30th, 2023)

ENHANCEMENTS:

* [608](https://github.com/perfectsense/gyro-aws-provider/pull/608): Allow configuring Cloudfront Functions.

## 1.5.5 (August 1st, 2023)

ISSUES FIXED:

* [609](https://github.com/perfectsense/gyro-aws-provider/issues/609): Fix S3 erroring out when trying to load accelerate configs in unsupported region.

## 1.5.4 (April 12th, 2023)

ENHANCEMENTS:

* [605](https://github.com/perfectsense/gyro-aws-provider/issues/605): Allow configuring the object ownership for S3 buckets.

## 1.5.3 (January 24th, 2023)

ISSUES FIXED:

* [599](https://github.com/perfectsense/gyro-aws-provider/pull/599): Fix S3 trying to recreate encryption configuration.

## 1.5.2 (January 17th, 2023)

ENHANCEMENTS::

* [568](https://github.com/perfectsense/gyro-aws-provider/issues/568): Add support for `public access block` in S3 Bucket.
* [590](https://github.com/perfectsense/gyro-aws-provider/pull/590): Add support for specifying `keyspec` in KMS Key.
* [592](https://github.com/perfectsense/gyro-aws-provider/pull/592): Add support for `arn` in ACM Finder.

## 1.5.1 (October 25th, 2022)

ENHANCEMENTS::

* [593](https://github.com/perfectsense/gyro-aws-provider/issues/593): Add support for `gp3` type in EBS Volume.

## 1.5.0 (September 14th, 2022)

ENHANCEMENTS::

* [578](https://github.com/perfectsense/gyro-aws-provider/pull/578): Implement batch refresh for RoleResource and RecordsetResource.

## 1.4.13 (September 13th, 2022)

ENHANCEMENTS::

* [587](https://github.com/perfectsense/gyro-aws-provider/pull/587): Implement Origin Access Control for CloudFront.

ISSUES FIXED:

* [585](https://github.com/perfectsense/gyro-aws-provider/pull/585): Fix error refreshing WAFV2 if rule deleted from console.

## 1.4.12 (August 26th, 2022)

ISSUES FIXED:

* [582](https://github.com/perfectsense/gyro-aws-provider/pull/582): Fix CloudFront tag update.

* [583](https://github.com/perfectsense/gyro-aws-provider/pull/583): Fix CloudFront Monitoring subscription delete error.

## 1.4.11 (August 23rd, 2022)

ISSUES FIXED:

* [579](https://github.com/perfectsense/gyro-aws-provider/pull/579): Fix TransitGateway null on Route.

* [580](https://github.com/perfectsense/gyro-aws-provider/pull/580): Fix CloudFront refresh error when subscription monitor is not enabled.

## 1.4.10 (August 15th, 2022)

ENHANCEMENTS:

* [572](https://github.com/perfectsense/gyro-aws-provider/issues/572): Add support for `Subscription Monitoring` in Cloudfront.
* [574](https://github.com/perfectsense/gyro-aws-provider/issues/574): Add support for `Intelligent Tiering` for S3 Buckets.

## 1.4.9 (June 24th, 2022)

ISSUES FIXED:

* [569](https://github.com/perfectsense/gyro-aws-provider/issues/569): Fix NPE on Security Group Rule refresh.

## 1.4.8 (May 19th, 2022)

ENHANCEMENTS:

* [536](https://github.com/perfectsense/gyro-aws-provider/issues/536): Add support for Event Bridge.

## 1.4.7 (March 2nd, 2022)

ISSUES FIXED:

* [549](https://github.com/perfectsense/gyro-aws-provider/issues/549): Fix subresource doc for WAF-V2.

* [551](https://github.com/perfectsense/gyro-aws-provider/issues/551): Add support for `TLSv1.2_2021` minimum protocol version for CloudFront.

* [554](https://github.com/perfectsense/gyro-aws-provider/pull/554): Fix GeoRestriction error when not set on CloudFront.

* [555](https://github.com/perfectsense/gyro-aws-provider/issues/555): Fix max policy version reached error when updating IAM Policy.

* [559](https://github.com/perfectsense/gyro-aws-provider/issues/559): Fix logging configuration validation for WAF-V2.

## 1.4.6 (January 24th, 2022)

ISSUES FIXED:

* [540](https://github.com/perfectsense/gyro-aws-provider/pull/540): Fix resource creation error when state is not saved.

* [544](https://github.com/perfectsense/gyro-aws-provider/pull/544): Allow updating Dead Letter Queue on SQS.

* [545](https://github.com/perfectsense/gyro-aws-provider/pull/545): Fix error setting Kms Master Key Id on SQS.

ENHANCEMENTS:

* [546](https://github.com/perfectsense/gyro-aws-provider/pull/546): Add support for setting Kms Master Key Id on SNS Topic.

## 1.4.5 (January 11th, 2022)

ISSUES FIXED:

* [538](https://github.com/perfectsense/gyro-aws-provider/pull/538): Fix NPE for forwardedCookies response in CloudFront

ENHANCEMENTS:

* [535](https://github.com/perfectsense/gyro-aws-provider/pull/535): Add support for `BottleRocket` as a managed type in Nodegroup.

## 1.4.4 (November 17th, 2021)

ISSUES FIXED:

* [529](https://github.com/perfectsense/gyro-aws-provider/pull/529): Fix WAFV2 error refreshing with a console configured NotStatementResource.
* [533](https://github.com/perfectsense/gyro-aws-provider/issues/533): Fix NPE when creating EC2 Endpoint.

ENHANCEMENTS:

* [530](https://github.com/perfectsense/gyro-aws-provider/pull/530): Add support for `capacity type` in Nodegroup.

## 1.4.3 (September 17th, 2021)

ISSUES FIXED:

* [521](https://github.com/perfectsense/gyro-aws-provider/issues/521): Fix WAFV2 ALB association error while creating an WebAcl.
* [523](https://github.com/perfectsense/gyro-aws-provider/issues/523): Fix classic ELB security group reference updates.
* [526](https://github.com/perfectsense/gyro-aws-provider/issues/526): Allow EKS Addons and EKS Authentication to be defined as standalone resources.

## 1.4.2 (September 7th, 2021)

ISSUES FIXED:

* [519](https://github.com/perfectsense/gyro-aws-provider/issues/519): WAFV2: ip-set-reference-statement error on gyro up if multiple part of an or statement.

## 1.4.1 (August 20th, 2021)

ISSUES FIXED:

* [516](https://github.com/perfectsense/gyro-aws-provider/issues/516): Wait for Resource Record to be populated in the ACM certificate, which fails during creation.

## 1.4.0 (August 5th, 2021)

ENHANCEMENTS:

* [508](https://github.com/perfectsense/gyro-aws-provider/pull/508): Add support for field `resolve-conflicts` in EKS Addon.
* [509](https://github.com/perfectsense/gyro-aws-provider/issues/509): Add support for [timeout](https://gyro.dev/guides/language/built-in-directives.html#timeout) overrides for resources that have `wait`.

ISSUES FIXED:

* [504](https://github.com/perfectsense/gyro-aws-provider/issues/504): Fix validations for EC2 Instances when using Launch Template.
* [511](https://github.com/perfectsense/gyro-aws-provider/pull/511): Fix null display for fields in Route53 and EKS encryption config.

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
