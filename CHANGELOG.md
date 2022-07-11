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

## Changed
- Removed Bluetooth permission from the Core SDK and from now the bluetooth status would be collected and sent as a part of the payload only if bluetooth permission is included in the SDK, so that from now bluetooth permission is not necessarily needed to make use of the SDK.