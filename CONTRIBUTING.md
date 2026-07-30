# Contributing to RudderStack

Thanks for taking the time and for your help in improving this project!

## Table of contents

- [**RudderStack Contributor Agreement**](#rudderstack-contributor-agreement)
- [**Contribute to this project**](#contribute-to-this-project)
- [**Committing**](#committing)
- [**Installing and setting up RudderStack**](#installing-and-setting-up-rudderstack)
- [**Getting help**](#getting-help)

## RudderStack Contributor Agreement

To contribute to this project, we need you to sign the [**Contributor License Agreement (“CLA”)**][CLA] for the first commit you make. By agreeing to the [**CLA**][CLA]
we can add you to list of approved contributors and review the changes proposed by you.

## Contribute to this project

If you encounter a bug or have any suggestion for improving this project, you can [**submit an issue**][issue] describing your proposed change. Alternatively, you can propose a change by making a pull request and tagging our team members.

For more information on the different ways in which you can contribute to RudderStack, you can chat with us on our [**Slack**][Slack] channel.

## Committing

The active v1 SDK uses `develop` as its integration branch and `master` as its
release branch. Normal pull requests target `develop` and use Conventional
Commit titles because release-please derives versions and changelog entries
from the commits promoted to `master`.

To release v1, run **Promote Release Candidate** from `develop` or a `hotfix/*`
branch. Merge the resulting promotion pull request into `master` with a merge
commit; do not squash or rebase it. Release-please must retain the individual
Conventional Commits to determine the correct version and changelog.
Release-please then creates or updates a separate semantic-version/changelog
pull request. Merging that pull request creates the `v`-prefixed tag and GitHub
release, which triggers Maven Central publication and the `master` to `develop`
back-merge.

The v2 workflows are separate and are not driven by this v1 release-please
configuration.

## Installing and setting up RudderStack

To contribute to this project, you may need to install RudderStack on your machine. You can do so by following our [**docs**](https://rudderstack.com/docs/get-started/installing-and-setting-up-rudderstack) and set up RudderStack in no time.

## Getting help

For any questions, concerns, or queries, you can start by asking a question on our [**Slack**][Slack] channel.
<br><br>

### We look forward to your feedback on improving this project!


<!----variables---->

[issue]: https://github.com/rudderlabs/rudder-sdk-android/issues/new
[CLA]: https://forms.gle/845JRGVZaC6kPZy68
[Slack]: https://rudderstack.com/join-rudderstack-slack-community/
