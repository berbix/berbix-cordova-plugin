import Berbix

class BerbixHandler : BerbixSDKDelegate {
    var plugin: CDVPlugin
    var clientID: String
    var config: BerbixConfiguration
    var callbackID: String

    init(plugin: CDVPlugin, clientID: String, config: BerbixConfiguration, callbackID: String) {
        self.plugin = plugin
        self.clientID = clientID
        self.config = config
        self.callbackID = callbackID
    }

    func start(controller: UIViewController) {
        let berbixSDK = BerbixSDK.init(clientID: clientID)
        berbixSDK.startFlow(controller, delegate: self, config: config)
    }

    func completed() {
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK
        )

        plugin.commandDelegate!.send(
            pluginResult,
            callbackId: callbackID
        )
    }

    func failed(error: Error) {
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR,
            messageAs: error.localizedDescription
        )

        plugin.commandDelegate!.send(
            pluginResult,
            callbackId: callbackID
        )
    }
}

@objc(Berbix) class Berbix : CDVPlugin {
  @objc(verify:)
  func verify(command: CDVInvokedUrlCommand) {
    let options = command.arguments[0] as! [String: Any]

    let clientID = options["client_id"] as? String
    let templateKey = options["template_key"] as? String
    let baseURL = options["base_url"] as? String
    let clientToken = options["client_token"] as? String
    let environment = options["environment"] as? String

    if clientID == nil {
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR,
            messageAs: "cannot start berbix flow without client ID"
        )

        commandDelegate!.send(
            pluginResult,
            callbackId: command.callbackId
        )
    }

    var config = BerbixConfigurationBuilder()

    if templateKey != nil {
        config = config.withTemplateKey(templateKey!)
    }
    if baseURL != nil {
        config = config.withBaseURL(baseURL!)
    }
    if clientToken != nil {
        config = config.withClientToken(clientToken!)
    }
    if environment != nil {
        if let env = getEnvironment(environment!) {
            config = config.withEnvironment(env)
        }
    }

    let handler = BerbixHandler(
        plugin: self,
        clientID: clientID!,
        config: config.build(),
        callbackID: command.callbackId)

    handler.start(controller: viewController)
  }

    func getEnvironment(_ env: String) -> BerbixEnvironment? {
        switch env {
        case "production":
            return .production
        case "staging":
            return .staging
        case "sandbox":
            return .sandbox
        default:
            return nil
        }
    }
}
