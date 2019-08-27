import Berbix

@objc(Berbix) class Berbix : CDVPlugin, BerbixSDKDelegate {
  @objc(echo:)
  func echo(command: CDVInvokedUrlCommand) {
    var pluginResult = CDVPluginResult(
      status: CDVCommandStatus_ERROR
    )

    let msg = command.arguments[0] as? String ?? ""

    if msg.characters.count > 0 {
        let berbixSDK = BerbixSDK.init(clientID: "JwZ_PatHf5pe1VqVnSd1_DbocrzFqPNS")
        let config = BerbixConfigurationBuilder()
        .withBaseURL("https://eric.dev.berbix.com:8443")
            .withRoleKey("zZa8mzSepqJu-8cfupZ11vdukLK9v1J8")
            .build()
        berbixSDK.startFlow(self.viewController, delegate: self, config: config)
        
      pluginResult = CDVPluginResult(
        status: CDVCommandStatus_OK,
        messageAs: msg
      )
    }

    self.commandDelegate!.send(
      pluginResult,
      callbackId: command.callbackId
    )
  }

      func completed() {
        print("completed successfully")
    }
    
    func failed(error: Error) {
        switch error {
        case BerbixError.userExitError:
            print("user exited early")
        case BerbixError.permissionError:
            print("there was a permission error")
        default:
            print("there was a default error", error)
        }
        print("failed...", error.localizedDescription)
    }
}