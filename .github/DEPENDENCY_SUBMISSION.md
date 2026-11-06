# Gradle dependency submission

The workflow resolves two independent builds. The root SDK build uses Gradle 8.7 and JDK 17. The standalone JavaScript sample uses Gradle 6.5 and JDK 11.

Pull requests generate inventory with a read-only token. Default-branch dependency changes, weekly refreshes and manual runs also generate inventory. Only a successful run on `develop` can submit it through a separate job. Matrix context identities keep both submissions distinct. Unresolved dependencies or an inventory without direct/indirect and runtime/development Maven entries fail the workflow. The check retains one known exception: Kotlin metadata in `android-tv`, `sample-kotlin`, and `sample-kotlin-integration` cannot select a debug or release variant of the local `core` project. The `core` runtime and test configurations are checked separately. All other unresolved dependencies fail the run.

The pinned Gradle action is on the organization allowlist as of 2026-10-29. Hosted generation and default-branch ingestion remain separate acceptance steps. No build runs with the submission job's write token.

After merge, run the workflow on `develop`, inspect both artifacts, and verify their Maven packages in GitHub's dependency graph at the source revision. Confirm all seven root modules and the independent sample remain selected. A graph submission can produce alerts without an automatic fix PR. Local JARs and unsupported package identities require separate evidence.

Rollback: remove this workflow. Keep existing Dependabot alerts and updater configuration. A previous snapshot can remain in GitHub until it is superseded or removed; removal is not evidence of zero vulnerabilities.
