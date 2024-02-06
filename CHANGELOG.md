# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

### [1.21.3](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.21.2...v1.21.3) (2024-02-06)


### Bug Fixes

* handling the serialization of special floating point values while serializing any object ([#382](https://github.com/rudderlabs/rudder-sdk-android/issues/382)) ([55521b6](https://github.com/rudderlabs/rudder-sdk-android/commit/55521b675de289c3c1ba5f80d40d338a61c9aac5))
* race condition fix using semaphore ([#388](https://github.com/rudderlabs/rudder-sdk-android/issues/388)) ([a792ce2](https://github.com/rudderlabs/rudder-sdk-android/commit/a792ce26514b31d82317eb59f16a97979ddfc13c))

### [1.21.2](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.21.1...v1.21.2) (2024-01-25)


### Bug Fixes

* added synchronization to RudderContext ([#372](https://github.com/rudderlabs/rudder-sdk-android/issues/372)) ([ad9baa0](https://github.com/rudderlabs/rudder-sdk-android/commit/ad9baa07ab5685a82c9b6822e01824db227642a3))

### [1.21.1](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.21.0...v1.21.1) (2023-12-13)


### Bug Fixes

* batches failed to be sent to data plane ([#367](https://github.com/rudderlabs/rudder-sdk-android/issues/367)) ([2a04e28](https://github.com/rudderlabs/rudder-sdk-android/commit/2a04e2878c20c6e99345533542a4117abc323c75))

## [1.21.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.20.2...v1.21.0) (2023-11-14)


### Features

* added support for JSONObject and JSONArray as part of RudderMessage object ([#350](https://github.com/rudderlabs/rudder-sdk-android/issues/350)) ([c9bb58f](https://github.com/rudderlabs/rudder-sdk-android/commit/c9bb58f7e2e2b76f9aaed817e36d5f46c3b86bab))


### Bug Fixes

* update lastActiveTimestamp value when reset call is made ([#360](https://github.com/rudderlabs/rudder-sdk-android/issues/360)) ([7596d71](https://github.com/rudderlabs/rudder-sdk-android/commit/7596d718fa63dda0e1d5cb683749acfc6691b295))

### [1.20.2](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.20.1...v1.20.2) (2023-11-01)


### Bug Fixes

* Null pointer exception in alias call ([#349](https://github.com/rudderlabs/rudder-sdk-android/issues/349)) ([e025fd2](https://github.com/rudderlabs/rudder-sdk-android/commit/e025fd295ed3f0839285e297e76d8f6b52cbe5ab))

### [1.20.1](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.20.0...v1.20.1) (2023-10-16)


### Bug Fixes

* typo in sample-kotlin build.gradle ([4097ea4](https://github.com/rudderlabs/rudder-sdk-android/commit/4097ea40f9c39966bed2ecad501a78defd93e2c1))
* work manager not getting initialised if ([#330](https://github.com/rudderlabs/rudder-sdk-android/issues/330)) ([d10fc24](https://github.com/rudderlabs/rudder-sdk-android/commit/d10fc244e105fe885628105dc8c22344a2d2e549))

## [1.20.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.19.1...v1.20.0) (2023-10-03)


### Features

* adding metrics for workmanager dbEncryption and dmt ([#329](https://github.com/rudderlabs/rudder-sdk-android/issues/329)) ([d38ad43](https://github.com/rudderlabs/rudder-sdk-android/commit/d38ad433b3dded21cbe57ce4e05699232e502954))


### Bug Fixes

* added filter for rudderstack crashes ([#325](https://github.com/rudderlabs/rudder-sdk-android/issues/325)) ([b804a32](https://github.com/rudderlabs/rudder-sdk-android/commit/b804a32b5928f8a5bab081de624e245a17cc4305))
* expose proguard rules as part of the library to ensure safer builds ([#321](https://github.com/rudderlabs/rudder-sdk-android/issues/321)) ([46a5413](https://github.com/rudderlabs/rudder-sdk-android/commit/46a54137990b171b6430de6ca8e0fc90aa26cde0))

### [1.19.1](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.19.0...v1.19.1) (2023-09-21)


### Bug Fixes

* null pointer exception in metrics ([#315](https://github.com/rudderlabs/rudder-sdk-android/issues/315)) ([2818904](https://github.com/rudderlabs/rudder-sdk-android/commit/28189049ed5e1ae446880dd7bb70d8992c1d9681))

## [1.19.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.18.1...v1.19.0) (2023-09-20)


### Features

* add getAnonymousId() API support ([#308](https://github.com/rudderlabs/rudder-sdk-android/issues/308)) ([3e1a67a](https://github.com/rudderlabs/rudder-sdk-android/commit/3e1a67ae77dec5d7a2f87b2ad789d945d4349046))
* add getsessionid api support ([#301](https://github.com/rudderlabs/rudder-sdk-android/issues/301)) ([d63f1d2](https://github.com/rudderlabs/rudder-sdk-android/commit/d63f1d2e33fdcfcd41313569d2edfec246250acc))
* sdk 94 error stats integrations ([#309](https://github.com/rudderlabs/rudder-sdk-android/issues/309)) ([96cc9f1](https://github.com/rudderlabs/rudder-sdk-android/commit/96cc9f1b5d27926f5eff75cbe3fcdfacaab5f6c8))


### Bug Fixes

* fixed batch payload being sent as empty to data plane ([#304](https://github.com/rudderlabs/rudder-sdk-android/issues/304)) ([c5602e7](https://github.com/rudderlabs/rudder-sdk-android/commit/c5602e7a4cee0e485d331beaa979a12eb4614fd2))
* github actions ([c239454](https://github.com/rudderlabs/rudder-sdk-android/commit/c239454b6f2c8566dff5ebe42a6377f1a37224f9))
* sessionId not getting cleared issue ([#300](https://github.com/rudderlabs/rudder-sdk-android/issues/300)) ([12e09ca](https://github.com/rudderlabs/rudder-sdk-android/commit/12e09caa236eccf98c3f112608c950ad0ddca195))

### [1.18.1](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.18.0...v1.18.1) (2023-09-04)


### Bug Fixes

* ci actions in draft_new_release ([195c162](https://github.com/rudderlabs/rudder-sdk-android/commit/195c162171a53fc7e95d01d5ded28e85b1b15697))
* remove depth from draft new release ([d851b62](https://github.com/rudderlabs/rudder-sdk-android/commit/d851b627ec5a2089a3b64b5680ad3012c36b98fe))
* sdk 197 metrics initialization fix ([#294](https://github.com/rudderlabs/rudder-sdk-android/issues/294)) ([82fd548](https://github.com/rudderlabs/rudder-sdk-android/commit/82fd5481e7781be0c589d9780afa058bb089b8fd))
* sdk 400 ensure empty key is not accepted as encryption key for ([#293](https://github.com/rudderlabs/rudder-sdk-android/issues/293)) ([a7176b1](https://github.com/rudderlabs/rudder-sdk-android/commit/a7176b1bb6ca2e9d626c440fb6dad20e626eeef8))

## [1.18.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.16.0...v1.18.0) (2023-08-28)


### Features

*  sdk-173 encrypt values in sqlite3 database ([#283](https://github.com/rudderlabs/rudder-sdk-android/issues/283)) ([4f26ef9](https://github.com/rudderlabs/rudder-sdk-android/commit/4f26ef98fda8d3e85ca4712aac0e188ba57fe6b0))
* add metrics to Android SDK V1 ([#241](https://github.com/rudderlabs/rudder-sdk-android/issues/241)) ([fbf93f7](https://github.com/rudderlabs/rudder-sdk-android/commit/fbf93f718b36ca81dde01fd55c7eb90accbb8b23))
* making device id collection configurable and de-linking anonymousId and deviceId ([#281](https://github.com/rudderlabs/rudder-sdk-android/issues/281)) ([7e81e74](https://github.com/rudderlabs/rudder-sdk-android/commit/7e81e748d94f0910866ed0ae999c20785bf39991))


### Bug Fixes

* sdk 392 sqlcipher recreate database if key is wrong ([#287](https://github.com/rudderlabs/rudder-sdk-android/issues/287)) ([2e057ef](https://github.com/rudderlabs/rudder-sdk-android/commit/2e057ef65cf36b074d1aca9c366a959bef5e8835))

## [1.17.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.15.0...v1.17.0) (2023-08-02)


### Features

* add metrics to Android SDK V1 ([#241](https://github.com/rudderlabs/rudder-sdk-android/issues/241)) ([fbf93f7](https://github.com/rudderlabs/rudder-sdk-android/commit/fbf93f718b36ca81dde01fd55c7eb90accbb8b23))
* enhance support for dmt source config changes and retrying with exponential backoff logic ([#236](https://github.com/rudderlabs/rudder-sdk-android/issues/236)) ([c27d39c](https://github.com/rudderlabs/rudder-sdk-android/commit/c27d39c71180c9d2849dcda0fa75721efd96fedb))

## [1.16.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.15.0...v1.16.0) (2023-08-02)


### Features

* enhance support for dmt source config changes and retrying with exponential backoff logic ([#236](https://github.com/rudderlabs/rudder-sdk-android/issues/236)) ([c27d39c](https://github.com/rudderlabs/rudder-sdk-android/commit/c27d39c71180c9d2849dcda0fa75721efd96fedb))

## [1.15.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.14.0...v1.15.0) (2023-07-31)


### Bug Fixes

* event not getting removed from db in certain cases ([#239](https://github.com/rudderlabs/rudder-sdk-android/issues/239)) ([ce8d057](https://github.com/rudderlabs/rudder-sdk-android/commit/ce8d057e60b61af0571242b7bfe0c7367b11377f))
* moved db operations to background thread/executor and fixed leaking objects ([#237](https://github.com/rudderlabs/rudder-sdk-android/issues/237)) ([9b66f65](https://github.com/rudderlabs/rudder-sdk-android/commit/9b66f651b120bd4e9580eb8fb2bb5f058133c871)), closes [#238](https://github.com/rudderlabs/rudder-sdk-android/issues/238)
* replay message queue dumping logic ([#246](https://github.com/rudderlabs/rudder-sdk-android/issues/246)) ([f5253cd](https://github.com/rudderlabs/rudder-sdk-android/commit/f5253cdddee407acad10b450c1cd43929210c73e))

## [1.14.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.13.0...v1.14.0) (2023-06-12)


### Features

* added gzip feature ([#224](https://github.com/rudderlabs/rudder-sdk-android/issues/224)) ([a0212f0](https://github.com/rudderlabs/rudder-sdk-android/commit/a0212f0bf6879232da86d4ecc0fd0225b3651557))
* added new way for tracking life cycle events in android using life cycle observer ([#225](https://github.com/rudderlabs/rudder-sdk-android/issues/225)) ([ae5a938](https://github.com/rudderlabs/rudder-sdk-android/commit/ae5a938abd19448ee173bed17b817fa3c6d22ee3)), closes [#233](https://github.com/rudderlabs/rudder-sdk-android/issues/233)


### Bug Fixes

* sourceConfig issue by changing encoding to Base64.NO_WRAP ([#221](https://github.com/rudderlabs/rudder-sdk-android/issues/221)) ([4d9cba1](https://github.com/rudderlabs/rudder-sdk-android/commit/4d9cba17c0a8a4a119ca8c2ab70269c6c9a3c4d9))

## [1.13.1](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.13.0...v1.13.1) (2023-05-17)

### Bug Fixes

* sourceConfig issue by changing encoding to Base64.NO_WRAP ([#221](https://github.com/rudderlabs/rudder-sdk-android/issues/221)) ([4d9cba1](https://github.com/rudderlabs/rudder-sdk-android/commit/4d9cba17c0a8a4a119ca8c2ab70269c6c9a3c4d9))

## [1.13.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.11.0...v1.13.0) (2023-04-19)


### Features

* adding consented values for cloud mode. ([ba4a698](https://github.com/rudderlabs/rudder-sdk-android/commit/ba4a698eeb9089d67fabf97dd74231cca8696b76))
* device mode transformation ([#123](https://github.com/rudderlabs/rudder-sdk-android/issues/123)) ([6a28177](https://github.com/rudderlabs/rudder-sdk-android/commit/6a28177d360f3f7d9726a4825b899b033b93ccb4)), closes [#138](https://github.com/rudderlabs/rudder-sdk-android/issues/138)

## [1.13.0-beta.1](2023-03-23)

* Added Support for Device Mode Transformations

## [1.12.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.10.0...v1.12.0) (2023-03-02)


### Features

* adding consented values for cloud mode. ([ba4a698](https://github.com/rudderlabs/rudder-sdk-android/commit/ba4a698eeb9089d67fabf97dd74231cca8696b76))
* log error message for empty writeKey & dataPlaneUrl ([465e14a](https://github.com/rudderlabs/rudder-sdk-android/commit/465e14ad2dbbf90130691dd64a36da454b506975))

## [1.11.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.9.0...v1.11.0) (2023-02-21)


### Features

* consent ([#197](https://github.com/rudderlabs/rudder-sdk-android/issues/197)) ([2f33197](https://github.com/rudderlabs/rudder-sdk-android/commit/2f33197e67d4b062392c44975708865e1141ea27))
* log error message for empty writeKey & dataPlaneUrl ([465e14a](https://github.com/rudderlabs/rudder-sdk-android/commit/465e14ad2dbbf90130691dd64a36da454b506975))

## [1.10.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.8.1...v1.10.0) (2023-02-09)


### Features

* consent ([#197](https://github.com/rudderlabs/rudder-sdk-android/issues/197)) ([2f33197](https://github.com/rudderlabs/rudder-sdk-android/commit/2f33197e67d4b062392c44975708865e1141ea27))
* data residency support ([#149](https://github.com/rudderlabs/rudder-sdk-android/issues/149)) ([acb06bb](https://github.com/rudderlabs/rudder-sdk-android/commit/acb06bb2b5a27f544805c3df65ff12d240598aa3)), closes [#150](https://github.com/rudderlabs/rudder-sdk-android/issues/150) [#154](https://github.com/rudderlabs/rudder-sdk-android/issues/154) [#153](https://github.com/rudderlabs/rudder-sdk-android/issues/153) [#184](https://github.com/rudderlabs/rudder-sdk-android/issues/184) [#185](https://github.com/rudderlabs/rudder-sdk-android/issues/185)

## [1.9.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.8.0...v1.9.0) (2023-02-02)


### Features

* data residency support ([#149](https://github.com/rudderlabs/rudder-sdk-android/issues/149)) ([acb06bb](https://github.com/rudderlabs/rudder-sdk-android/commit/acb06bb2b5a27f544805c3df65ff12d240598aa3)), closes [#150](https://github.com/rudderlabs/rudder-sdk-android/issues/150) [#154](https://github.com/rudderlabs/rudder-sdk-android/issues/154) [#153](https://github.com/rudderlabs/rudder-sdk-android/issues/153) [#184](https://github.com/rudderlabs/rudder-sdk-android/issues/184) [#185](https://github.com/rudderlabs/rudder-sdk-android/issues/185)


### Bug Fixes

* fixed the null pointer exception thrown while iterating through the message queue  ([#191](https://github.com/rudderlabs/rudder-sdk-android/issues/191)) ([1bca2ba](https://github.com/rudderlabs/rudder-sdk-android/commit/1bca2baea224883f7d6234907fb41102bf9e0e20))
* fixed the null pointer exception thrown while iterating through the message queue  ([#191](https://github.com/rudderlabs/rudder-sdk-android/issues/191)) ([0871e4a](https://github.com/rudderlabs/rudder-sdk-android/commit/0871e4a9830e2f9181d0ff3e03fbd4b833f896fb))
* removed tag check from release ([569806c](https://github.com/rudderlabs/rudder-sdk-android/commit/569806ce36f72ad9f0cc4119ea5491679353b444))

### [1.8.1](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.8.0...v1.8.1) (2023-01-04)


### Bug Fixes

* fixed the null pointer exception thrown while iterating through the message queue  ([#191](https://github.com/rudderlabs/rudder-sdk-android/issues/191)) ([1bca2ba](https://github.com/rudderlabs/rudder-sdk-android/commit/1bca2baea224883f7d6234907fb41102bf9e0e20))
* fixed the null pointer exception thrown while iterating through the message queue  ([#191](https://github.com/rudderlabs/rudder-sdk-android/issues/191)) ([0871e4a](https://github.com/rudderlabs/rudder-sdk-android/commit/0871e4a9830e2f9181d0ff3e03fbd4b833f896fb))
* removed tag check from release ([569806c](https://github.com/rudderlabs/rudder-sdk-android/commit/569806ce36f72ad9f0cc4119ea5491679353b444))

## [1.8.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.6.1...v1.8.0) (2022-12-08)


### Features

* added gradle files ([fabfaf5](https://github.com/rudderlabs/rudder-sdk-android/commit/fabfaf549dc5a77a91ecfe6354148b02f81c81e2))
* refactored version name to a single source ([e1cf1e8](https://github.com/rudderlabs/rudder-sdk-android/commit/e1cf1e803a0bae7c1671633ad0869c0f43b3587c))
* removed timestamp from messageId ([#175](https://github.com/rudderlabs/rudder-sdk-android/issues/175)) ([1e61538](https://github.com/rudderlabs/rudder-sdk-android/commit/1e615382977cf4b0d59a977ef0ea4157bb3588b3))


### Bug Fixes

* added bump file ([316c375](https://github.com/rudderlabs/rudder-sdk-android/commit/316c375a4fa32cba6342bab4f894d892f51d76ff))
* added dependency for properties reader ([3ece85b](https://github.com/rudderlabs/rudder-sdk-android/commit/3ece85b915da555714566b238e7806533377c65b))
* added gradle scripts ([fcfb0b0](https://github.com/rudderlabs/rudder-sdk-android/commit/fcfb0b050b7a8aac4c5d8d3769998192f17bc3b4))
* added package-lock.json ([b5fbcca](https://github.com/rudderlabs/rudder-sdk-android/commit/b5fbcca99889293ef40aa5df2a805ae6cb962658))
* added pull request and slack notify ([84842b5](https://github.com/rudderlabs/rudder-sdk-android/commit/84842b5201602abb899e4e8502ba5e148ee9122d))
* added version in package.json ([082be73](https://github.com/rudderlabs/rudder-sdk-android/commit/082be732e6e62f1fc793ecc63af702730a33fd89))
* changed library version to numeric in gradle.properties files ([#169](https://github.com/rudderlabs/rudder-sdk-android/issues/169)) ([43979c1](https://github.com/rudderlabs/rudder-sdk-android/commit/43979c167350410d82dd93b0101bb7a1d071fdee))
* commented code coverage ([fe69391](https://github.com/rudderlabs/rudder-sdk-android/commit/fe69391c1ebbfcd62a01d1b19fcf196ddcdfd96a))
* downgraded manually incremented version ([#170](https://github.com/rudderlabs/rudder-sdk-android/issues/170)) ([3bb5d2a](https://github.com/rudderlabs/rudder-sdk-android/commit/3bb5d2a31cb097e9ba23b1529faa51a5be311af6))
* fix draft new release ([3c776b9](https://github.com/rudderlabs/rudder-sdk-android/commit/3c776b9bbc667f0380dafb3c78042fb5423c1946))
* fix slack channel secret ([3ba89bb](https://github.com/rudderlabs/rudder-sdk-android/commit/3ba89bb7172569b98456851cb5609388554bed1b))
* fix slack secret ([45ec458](https://github.com/rudderlabs/rudder-sdk-android/commit/45ec458b715fa72d95793699853c59f452723162))
* fix workflows ([269e562](https://github.com/rudderlabs/rudder-sdk-android/commit/269e56214a8352fc151771af539a2af4625b0fab))
* fix workflows branch name ([5fba79f](https://github.com/rudderlabs/rudder-sdk-android/commit/5fba79f68adeb33c71fba80d18fadbcb5fce6440))
* fixed bash scripts to include mpx ([9f455f0](https://github.com/rudderlabs/rudder-sdk-android/commit/9f455f0c4311bad3ef07479cb301566350c44d54))
* fixed draft new release ([753c72a](https://github.com/rudderlabs/rudder-sdk-android/commit/753c72a1242dc756a6104e8a2eb1822aa2335ba4))
* fixed draft new release ([fc5699f](https://github.com/rudderlabs/rudder-sdk-android/commit/fc5699f5831bf7b73be372a3ff464a0d373b5014))
* fixed issues in release scripts ([#166](https://github.com/rudderlabs/rudder-sdk-android/issues/166)) ([0fa3810](https://github.com/rudderlabs/rudder-sdk-android/commit/0fa3810abe47ce16d2c8980d0cc23e70c5cf44c7))
* handled db downgrade issue by removing the extra status column ([#158](https://github.com/rudderlabs/rudder-sdk-android/issues/158)) ([b4e5d36](https://github.com/rudderlabs/rudder-sdk-android/commit/b4e5d36b0a5418101cc264a7dd4bf99c35b20522))
* indentation ([0b47f8e](https://github.com/rudderlabs/rudder-sdk-android/commit/0b47f8ec414987dbac844a6bc6b95b1dbe9289e8))
* release yaml ([2e1d678](https://github.com/rudderlabs/rudder-sdk-android/commit/2e1d67823d13d5659224b2304a607b167988f5e3))
* remove node ([#161](https://github.com/rudderlabs/rudder-sdk-android/issues/161)) ([8da800f](https://github.com/rudderlabs/rudder-sdk-android/commit/8da800f6d0ccb7599adb07162b6868e80e2d3681))
* slack-notify ([214d0c0](https://github.com/rudderlabs/rudder-sdk-android/commit/214d0c0b7d8734bfc5cc2c19fa2bf173e7a02ace))
* token ([225ec7c](https://github.com/rudderlabs/rudder-sdk-android/commit/225ec7c6ecee1155c48c775763b0dcd82cbed54b))
* workflow draft new release ([#176](https://github.com/rudderlabs/rudder-sdk-android/issues/176)) ([48f2a98](https://github.com/rudderlabs/rudder-sdk-android/commit/48f2a980887a2c1a7dfa4ba34fb650bb6a33eb23))
* workflow draft new release ([#177](https://github.com/rudderlabs/rudder-sdk-android/issues/177)) ([7a020b6](https://github.com/rudderlabs/rudder-sdk-android/commit/7a020b6e2f7e944fd276c56a90519b6eb1d659e9))

### [1.7.1](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.7.0...v1.7.1) (2022-11-18)

## [1.7.0](https://github.com/rudderlabs/rudder-sdk-android/compare/v1.6.1...v1.7.0) (2022-11-18)


### Features

* added gradle files ([fabfaf5](https://github.com/rudderlabs/rudder-sdk-android/commit/fabfaf549dc5a77a91ecfe6354148b02f81c81e2))
* refactored version name to a single source ([e1cf1e8](https://github.com/rudderlabs/rudder-sdk-android/commit/e1cf1e803a0bae7c1671633ad0869c0f43b3587c))


### Bug Fixes

* added gradle scripts ([fcfb0b0](https://github.com/rudderlabs/rudder-sdk-android/commit/fcfb0b050b7a8aac4c5d8d3769998192f17bc3b4))
* added pull request and slack notify ([84842b5](https://github.com/rudderlabs/rudder-sdk-android/commit/84842b5201602abb899e4e8502ba5e148ee9122d))
* changed library version to numeric in gradle.properties files ([#169](https://github.com/rudderlabs/rudder-sdk-android/issues/169)) ([43979c1](https://github.com/rudderlabs/rudder-sdk-android/commit/43979c167350410d82dd93b0101bb7a1d071fdee))
* commented code coverage ([fe69391](https://github.com/rudderlabs/rudder-sdk-android/commit/fe69391c1ebbfcd62a01d1b19fcf196ddcdfd96a))
* downgraded manually incremented version ([#170](https://github.com/rudderlabs/rudder-sdk-android/issues/170)) ([3bb5d2a](https://github.com/rudderlabs/rudder-sdk-android/commit/3bb5d2a31cb097e9ba23b1529faa51a5be311af6))
* fix draft new release ([3c776b9](https://github.com/rudderlabs/rudder-sdk-android/commit/3c776b9bbc667f0380dafb3c78042fb5423c1946))
* fix slack channel secret ([3ba89bb](https://github.com/rudderlabs/rudder-sdk-android/commit/3ba89bb7172569b98456851cb5609388554bed1b))
* fix slack secret ([45ec458](https://github.com/rudderlabs/rudder-sdk-android/commit/45ec458b715fa72d95793699853c59f452723162))
* fix workflows ([269e562](https://github.com/rudderlabs/rudder-sdk-android/commit/269e56214a8352fc151771af539a2af4625b0fab))
* fix workflows branch name ([5fba79f](https://github.com/rudderlabs/rudder-sdk-android/commit/5fba79f68adeb33c71fba80d18fadbcb5fce6440))
* fixed bash scripts to include mpx ([9f455f0](https://github.com/rudderlabs/rudder-sdk-android/commit/9f455f0c4311bad3ef07479cb301566350c44d54))
* fixed issues in release scripts ([#166](https://github.com/rudderlabs/rudder-sdk-android/issues/166)) ([0fa3810](https://github.com/rudderlabs/rudder-sdk-android/commit/0fa3810abe47ce16d2c8980d0cc23e70c5cf44c7))
* handled db downgrade issue by removing the extra status column ([#158](https://github.com/rudderlabs/rudder-sdk-android/issues/158)) ([b4e5d36](https://github.com/rudderlabs/rudder-sdk-android/commit/b4e5d36b0a5418101cc264a7dd4bf99c35b20522))
* indentation ([0b47f8e](https://github.com/rudderlabs/rudder-sdk-android/commit/0b47f8ec414987dbac844a6bc6b95b1dbe9289e8))
* release yaml ([2e1d678](https://github.com/rudderlabs/rudder-sdk-android/commit/2e1d67823d13d5659224b2304a607b167988f5e3))
* remove node ([#161](https://github.com/rudderlabs/rudder-sdk-android/issues/161)) ([8da800f](https://github.com/rudderlabs/rudder-sdk-android/commit/8da800f6d0ccb7599adb07162b6868e80e2d3681))
* slack-notify ([214d0c0](https://github.com/rudderlabs/rudder-sdk-android/commit/214d0c0b7d8734bfc5cc2c19fa2bf173e7a02ace))
* token ([225ec7c](https://github.com/rudderlabs/rudder-sdk-android/commit/225ec7c6ecee1155c48c775763b0dcd82cbed54b))

# Changelog
All notable changes to this project will be documented in this file.

## Version - 1.0 - 2020-02-10
### Added
- Automatic App Life cycle events tracking is added. `Application Installed`, `Application Updated`, `Application Opened`, `Application Backgrounded`. It is tracked by default and can be turned off using `RudderConfig`.
- Automatic Screen view events tracking is added. All Activities are tracked at `onStart` of the `Activity`. It is turned off by default. It can be turned on using `RudderConfig`.
- Added support for ECommerce events from the SDK. Different builders for important events are added.
- A new header `anonymousId` is added to the request to `data-plane` along with `writeKey` to handle sticky-session at the server.
### Changed
- Package name is changed from `com.rudderlabs.android.sdk.core` to `com.rudderstack.android.sdk.core`.
- New field `userId` is supported to make it more compliant under `context->traits` for `identify` and all successive calls. Old filed for developer identification i.e. `id` is still supported.

## Version - 1.6.0 - 2022-07-11
### Changed
- Removed Bluetooth permission from the Core SDK and from now the bluetooth status would be collected and sent as a part of the payload only if bluetooth permission is included in the SDK, so that from now bluetooth permission is not necessarily needed to make use of the SDK.

## Version - 1.6.1 - 2022-08-02
### Changed
- Removed the app_name from the string resources of the Core SDK.

## Version - 1.7.0 - 2022-08-04
### Added
- Session Tracking.

## Version -1.7.1 - 2022-11-17

### Fixed
- Db downgradation issues when the SDK is downgraded from DMT versions (1.8.0-beta.1) to previous versions
