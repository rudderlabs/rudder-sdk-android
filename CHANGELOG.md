# Changelog
All notable changes to this project will be documented in this file.

## Version -1.5.3 - 2023-12-2

### Fixed
- Db down-gradation issues when the SDK is downgraded from DB version > 1 to 1.

## Version - 1.0 - 2020-02-10
### Added
- Automatic App Life cycle events tracking is added. `Application Installed`, `Application Updated`, `Application Opened`, `Application Backgrounded`. It is tracked by default and can be turned off using `RudderConfig`.
- Automatic Screen view events tracking is added. All Activities are tracked at `onStart` of the `Activity`. It is turned off by default. It can be turned on using `RudderConfig`.
- Added support for ECommerce events from the SDK. Different builders for important events are added.
- A new header `anonymousId` is added to the request to `data-plane` along with `writeKey` to handle sticky-session at the server.
### Changed
- Package name is changed from `com.rudderlabs.android.sdk.core` to `com.rudderstack.android.sdk.core`.
- New field `userId` is supported to make it more compliant under `context->traits` for `identify` and all successive calls. Old filed for developer identification i.e. `id` is still supported. 