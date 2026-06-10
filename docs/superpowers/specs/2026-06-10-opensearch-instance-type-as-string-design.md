# INFRA-2060 — OpenSearch instance types as String (m7g/m8g support)

- **Ticket:** [INFRA-2060](https://perfectsense.atlassian.net/browse/INFRA-2060)
- **Date:** 2026-06-10
- **Branch:** `feature/INFRA-2060-opensearch-instance-type-as-string`

## Problem

The `m6g` OpenSearch instance family is **not** included in AWS Database Savings
Plans. To capture that discount we need to move OpenSearch domains to the `m7g`
or `m8g` family.

We currently run `m6g` because `OpenSearchClusterConfiguration` types its
instance-type fields as the AWS Java SDK enums
(`OpenSearchPartitionInstanceType`, `OpenSearchWarmPartitionInstanceType`), and
those enums do **not** yet contain `m7g`/`m8g`. The enum typing makes it
physically impossible to express the newer families in a gyro config.

### Confirmed approach (per AWS)

The AWS SDK for Java v2 does **not** validate the instance-type string against
the enum on the client side. Each enum-typed field exposes a string variant
(confirmed against `opensearch-2.42.32` via `javap`):

- Setter (builder): an **overloaded** `instanceType(String)` alongside
  `instanceType(OpenSearchPartitionInstanceType)` — there is no
  `instanceTypeAsString` *setter*. Passing a `String` selects the string overload.
- Getter (model): `instanceTypeAsString()` returns the raw `String` alongside
  `instanceType()` which returns the enum.

Passing the raw string serializes as-is; the **service** performs validation.
The `UNKNOWN_TO_SDK_VERSION` enum value is only a deserialization indicator —
reading an unknown type back via the enum getter returns it, which is why we must
read back via the string getter.

This is the same shape Aurora/RDS already uses in this provider:
`DbInstanceResource.dbInstanceClass` and `DbClusterResource.dbClusterInstanceClass`
are plain `String` fields (RDS never had an instance-class enum), with validation
delegated to the service and a doc link to AWS's instance-class list.

## Code change

Single file: `src/main/java/gyro/aws/opensearch/OpenSearchClusterConfiguration.java`

Convert all three instance-type fields from enum → `String`:

| Field | From | To |
|---|---|---|
| `instanceType` | `OpenSearchPartitionInstanceType` | `String` |
| `dedicatedMasterType` | `OpenSearchPartitionInstanceType` | `String` |
| `warmType` | `OpenSearchWarmPartitionInstanceType` | `String` |

Edits:

1. Field declarations + getters/setters → `String`.
2. `toOpenSearchClusterConfig()`: the builder calls stay `.instanceType(...)`,
   `.dedicatedMasterType(...)`, `.warmType(...)` — because the getters now return
   `String`, the compiler resolves the overloaded `instanceType(String)` setters
   (same pattern EC2's `InstanceResource` uses). No `...AsString` setter exists.
3. `copyFrom()` reads back via the string getters
   (`model.instanceTypeAsString()`, `dedicatedMasterTypeAsString()`,
   `warmTypeAsString()`). **Critical:** the enum getters would return
   `UNKNOWN_TO_SDK_VERSION` for `m7g`/`m8g` and cause a permanent spurious diff.
4. Remove `@ValidStrings` on `warm-type` — it locks the field to three ultrawarm
   values, which would defeat the future-proofing goal. Validation is delegated
   to the service for all three fields now.
5. Drop the two now-unused enum imports.
6. Tidy the Javadoc: give a string example (e.g. `m7g.large.search`) and link
   AWS's supported-instance-types doc, mirroring `db-instance-class`.

### Backward compatibility

No breaking change for config authors — gyro configs already write
`instance-type: 'm6g.large.search'` (a string). For already-deployed `m6g`
domains, `instanceTypeAsString()` returns the same wire value that the enum's
`toString()` produced, so refreshing an unchanged domain yields no spurious diff.

### Validation / testing

This repo has no unit-test harness (no `src/test`, no JUnit); verification is by
compile + the runnable `examples/` configs:

- `./gradlew compileJava` — proves the `...AsString` SDK methods resolve on
  AWS SDK BOM `2.42.32`.
- Update `examples/opensearch/domain.gyro` to set
  `instance-type: 'm7g.large.search'` as a live-runnable artifact.

## Migration plan — converting existing OpenSearch domains

The code change only *enables* the newer families; the conversion is an
operational rollout, one domain at a time.

1. **Release the provider change** and bump the gyro-aws-provider dependency in
   the consuming tenant repos.
2. **Per cluster, edit the gyro config:** change `instance-type` (and
   `dedicated-master-type` wherever dedicated masters run on `m6g`) from
   `m6g.*` to the equivalent `m7g.*` (or `m8g.*`) size. Keep node counts and
   storage the same so the only change is the family.
3. **Apply with `gyro up`.** Changing an OpenSearch domain's instance type
   triggers a **blue/green deployment**: AWS stands up the new fleet, migrates
   shards, and cuts over with no downtime, but it is a full data migration and
   can take a while on large domains. Do **one domain at a time**.
4. **Roll out in waves:** non-prod first, then prod; one region/cluster at a
   time; prefer low-traffic windows. After each apply, confirm the domain
   returns to `Active` with green cluster health and that the gyro diff is clean
   before moving on.
5. **Confirm savings:** after a domain is on the new family, verify in Cost
   Explorer that the instances are now absorbed by the Database Savings Plan
   coverage.

### Rollback

If a domain misbehaves on the new family, revert the `instance-type` in the gyro
config and `gyro up` again — this triggers another blue/green back to `m6g.*`.
No state surgery is required because the field is just a string.

## Out of scope

- Changing default instance types or node counts.
- Automating the per-tenant rollout (handled manually / tracked separately).
- Touching `opensearchserverless` resources (no instance-type concept).
