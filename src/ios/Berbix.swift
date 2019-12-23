import Berbix

class BerbixHandler : BerbixFlowDelegate {
    var plugin: CDVPlugin
    var config: BerbixConfiguration
    var callbackID: String

    init(plugin: CDVPlugin, config: BerbixConfiguration, callbackID: String) {
        self.plugin = plugin
        self.config = config
        self.callbackID = callbackID
    }

    func start(controller: UIViewController) {
        let berbixSDK = BerbixSDK.init()
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

    func failed(error: BerbixError) {
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

        let baseURL = options["base_url"] as? String
        let clientToken = options["client_token"] as? String
        let environment = options["environment"] as? String
        let debug = options["debug"] as? Bool

        var config = BerbixConfigurationBuilder()

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
        /*if debug != nil {
            config = config.withDebug(debug!)
        }*/

        let handler = BerbixHandler(
            plugin: self,
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
