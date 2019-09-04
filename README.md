# Cordova Berbix Plugin

## Installation

### Install prerequisites

cordova platform add ios
cordova platform add android
cordova plugin add cordova-plugin-add-swift-support

### Configuration

```xml
<preference name="deployment-target" value="11.0.0" />
```

```xml
<edit-config target="NSCameraUsageDescription" file="*-Info.plist" mode="merge">
    <string>To take photos of your identity documents for verification purposes.</string>
</edit-config>
```

### Plugin installation

Ensure that your Cordova project can build before installing the Berbix plugin.

    cordova build

Finally, install the Cordova plugin.

    cordova plugin add cordova-berbix-plugin

## Usage

```javascript
berbix.verify(
  function() {
    // Verification has completed successfully
  },
  function(err) {
    // Verification did not complete successfully
  },
  {
    client_id: "your client id here",
    template_key: "your template key here",
    client_token: "your client token here"
  }
);
```
