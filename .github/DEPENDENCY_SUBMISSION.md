# Gradle dependency submission

The workflow resolves two independent contexts: the root SDK build (all seven included modules, Gradle 7.5) and `sample-js-integration/RudderAndroidJS` (Gradle 6.5). Both use JDK 11 and the Android SDK provided by the hosted runner. The existing R8 override, dependency bounds, wrappers and publishing tasks are unchanged.

Pull requests generate inventory with a read-only token. Default-branch dependency changes, weekly refreshes and manual runs also generate inventory. Only a successful run on `develop` can submit it through a separate job. Matrix context identities keep both submissions distinct. Failed resolution or an inventory without direct/indirect and runtime/development Maven entries fails the workflow.

The pinned Gradle action must be approved in the organization allowlist before hosted execution. No build runs with the submission job's write token.

After merge, run the workflow on `develop`, inspect both artifacts, and verify their Maven packages in GitHub's dependency graph at the source revision. Confirm all seven root modules and the independent sample remain selected. A graph submission can produce alerts without an automatic fix PR. Local JARs and unsupported package identities require separate evidence.

Rollback: remove this workflow. Keep existing Dependabot alerts and updater configuration. A previous snapshot can remain in GitHub until it is superseded or removed; removal is not evidence of zero vulnerabilities.
