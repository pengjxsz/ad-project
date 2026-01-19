// IOSAppkitManager.swift (在 iosApp Target中)
import Combine
import ComposeApp  // 导入 KMP 框架
import Foundation
import ReownAppKit  // 您的纯 Swift 钱包库
import UIKit

//import reown_swift

// 注意：KMP 会将 Kotlin 接口 AppkitManagerBridge 导出为 Swift/Obj-C 协议
@objcMembers
public class IOSAppkitManager: NSObject, AppkitManagerBridge {
    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 关键：采纳 Kotlin 导出的协议
    var disposeBag = Set<AnyCancellable>()

    // ⚠️ 修正：添加问号使其成为可选属性，在初始化时可以为 nil
    private var stateChangeCallback: ((WalletConnectionState) -> Void)?
    private var sessionTopic: String = ""
    private func defaultSessionParms() -> SessionParams {

        // 仅保留最核心的 eth_sendTransaction 和 personal_sign
        //  let minimalMethods: Set<String> = ["eth_sendTransaction", "personal_sign"]
        // 仅保留 chainChanged
        //let minimalEvents: Set<String> = ["chainChanged"]

        let methods: Set<String> = [
            "eth_sendTransaction", "personal_sign", "eth_signTypedData",
        ]
        let events: Set<String> = ["chainChanged", "accountsChanged"]
        let blockchains: Set<Blockchain> = [Blockchain("eip155:84532")!]
        let namespaces: [String: ProposalNamespace] = [
            "eip155": ProposalNamespace(
                chains: [Blockchain("eip155:84532")!],  //Array(blockchains),
                methods: methods,
                events: events
            )
        ]

        let defaultSessionParams = SessionParams(
            requiredNamespaces: namespaces,  // [:],
            optionalNamespaces: [:],
            //            namespaces: namespaces,
            sessionProperties: nil
        )
        return defaultSessionParams

    }

    // 转换函数：Swift Account 数组 -> Kotlin List<WalletAccount>
    func convertSwiftAccountsToKotlin(accounts: [Account]) -> [WalletAccount] {
        return accounts.map { swiftAccount in
            // 创建 Kotlin WalletAccount 对象
            // 注意：WalletAccount 是 Kotlin 中定义的数据类
            return WalletAccount(
                namespace: swiftAccount.namespace,
                reference: swiftAccount.reference,
                address: swiftAccount.address,
            )
        }
    }

    // 假设您的 ReownAppKit 实例
    //private let internalAppkit = AppKit.Manager() // 示例
    private func setup(params: AppkitInitParams) {

        //        let metadata = AppMetadata(
        //            name: "Web3Modal Swift Dapp",
        //            description: "Web3Modal DApp sample",
        //            url: "www.web3modal.com",
        //            icons: ["https://avatars.githubusercontent.com/u/37784886"],
        //            redirect: try! .init(native: "w3mdapp://", universal: "https://lab.web3modal.com/web3modal_example", linkMode: true)
        //        )
        let kotlinMetaData = params.metaData
        let projectId = params.projectId
        UserDefaults.standard.set(
            false,
            forKey: "com.walletconnect.w3m.analyticsEnabled"
        )
        print("redirect is : \(CrossPlatformKt.AppSchemaIOS)://request")
        let metadata = AppMetadata(
            name: kotlinMetaData.name,
            description: kotlinMetaData.description,
            url: kotlinMetaData.url,
            icons: kotlinMetaData.icons,  // ["https://avatars.githubusercontent.com/u/179229932"],
            // Used for the Verify: to opt-out verification ignore this parameter
            //redirect: try! .init(native: "w3mdapp://", universal: kotlinMetaData.redirect, linkMode: true)
            //redirect:  try! AppMetadata.Redirect(native: "", universal: nil)
            redirect: try! AppMetadata.Redirect(
                // ⚠️ 必须提供您在 Xcode 中注册的 Native Scheme
                //native: "kotlin-sigad-wc://",
                native: "\(CrossPlatformKt.AppSchemaIOS)://request",
                universal: kotlinMetaData.appLink  //"https://111.89-1011.com/sigad"
            )

        )

        Networking.configure(
            //groupIdentifier: "group.com.walletconnect.web3modal",
            groupIdentifier: "78KU4HT8H8.io.xa.sigad",  //appid.bundleid
            projectId: projectId,
            socketFactory: DefaultSocketFactory()

        )

        AppKit.configure(
            projectId: projectId,
            metadata: metadata,
            crypto: DefaultCryptoProvider(),
            sessionParams: defaultSessionParms(),
            authRequestParams: nil,  // use .stub() for testing SIWE
            coinbaseEnabled: false
        ) { error in
            // Handle error
            print(error)
        }
        Task {
            try await AppKit.instance.cleanup()
        }
        AppKit.instance.socketConnectionStatusPublisher
            .receive(on: DispatchQueue.main)
            .sink { [weak self] status in
                print("Socket connection status: \(status)")
                //self?.socketConnectionManager.socketConnected = (status == .connected)
            }
            .store(in: &disposeBag)

        AppKit.instance.logger.setLogging(level: .debug)
        Sign.instance.setLogging(level: .debug)
        Networking.instance.setLogging(level: .debug)
        Relay.instance.setLogging(level: .debug)

        AppKit.instance.authResponsePublisher
            .sink {
                [weak self]
                (id: RPCID, result: Result<(Session?, [Cacao]), AuthError>) in
                switch result {
                case .success((_, _)):
                    //                    AlertPresenter.present(message: "User authenticated", type: .success)
                    print("User authenticated success")

                case .failure(let error):
                    //                    AlertPresenter.present(message: "User authentication error: \(error)", type: .error)
                    print("User authentication error: \(error)")

                }
            }
            .store(in: &disposeBag)

        AppKit.instance.SIWEAuthenticationPublisher
            .sink { [weak self] result in
                switch result {
                case .success((let message, let signature)):
                    //AlertPresenter.present(message: "User authenticated", type: .success)
                    print("User authenticated SIWE: success")

                case .failure(let error):
                    //                    AlertPresenter.present(message: "User authentication error: \(error)", type: .error)
                    print("User authentication SIWE error: \(error)")

                }
            }
            .store(in: &disposeBag)

        // 1. 订阅 Session Settle Publisher (监听连接成功)
        AppKit.instance.sessionSettlePublisher
            .sink { [weak self] session in
                guard let self = self else { return }

                // 获取当前活动的主链地址和 Chain ID
                let address = session.namespaces.values.first?.accounts.first?
                    .address  // 简化处理，取第一个地址
                sessionTopic = session.topic
                //let chainId = session.namespaces.values.first?.chains.first?.namespace// 简化处理，取第一个 Chain ID
                let a = session.accounts.first?.blockchainIdentifier
                let c = session.accounts.first?.reference
                let b = session.accounts.first?.namespace
                print(
                    "监听连接成功, \(session.expiryDate) blockchain \(String(describing: a)) \(b) \(c)"
                )

                let kotlinAccounts = convertSwiftAccountsToKotlin(
                    accounts: session.accounts
                )

                // 触发回调，通知 KMP 侧连接成功
                // 2. 构建 WalletConnectionState.Connected 状态
                let connectedState = WalletConnectionState.Connected(
                    topic: sessionTopic,
                    accounts: kotlinAccounts
                )

                // 3. 触发回调，通知 KMP 侧连接成功
                self.stateChangeCallback!(connectedState)

            }
            .store(in: &disposeBag)  // 存储订阅

        // 2. 订阅 Sessions Publisher (监听断开连接)
        //        AppKit.instance.sessionsPublisher
        //        // 筛选出 sessions 数组为空的情况，即所有会话都断开了
        //            .filter { $0.isEmpty }
        //            .sink { [weak self] _ in
        //                guard let self = self else { return }
        //
        //                // 触发回调，通知 KMP 侧连接断开
        //                print("监听断开连接")
        //                //self.connectionStateCallback?(nil, nil, false)
        //            }
        //            .store(in: &disposeBag) // 存储订阅

        // 假设您在同一个类/结构体中实现监听，并且拥有 disposeBag

        AppKit.instance.sessionsPublisher
            // 使用 sink 接收 [Session] 数组
            .sink { [weak self] sessions in
                guard let self = self else { return }

                print("监听 sessionsPublisher: 当前有 \(sessions.count) 个活跃会话")

                // 目标：将当前所有活跃会话列表通知给 KMP 侧

                if sessions.isEmpty {
                    // 如果列表为空，通知 KMP 侧处于 Disconnected 状态
                    let disconnectedState = WalletConnectionState.Disconnected()
                    self.stateChangeCallback?(disconnectedState)

                } else {
                    // 简化处理：通常只需要关心第一个会话作为“当前”连接
                    guard let firstSession = sessions.first else { return }

                    // 提取第一个会话的信息
                    //                    let address = firstSession.namespaces.values.first?.accounts.first?.address
                    //                    let sessionTopic = firstSession.topic
                    //
                    //                    // 1. 更新本地状态变量 (如果需要)
                    //                    // self.sessionTopic = sessionTopic // 如果您想跟踪最新的 Topic
                    //
                    //                    // 2. 构建 WalletConnectionState.Connected 状态
                    //                    let connectedState = WalletConnectionState.Connected(
                    //                        address: address!,
                    //                        topic: sessionTopic
                    //                    )
                    //
                    //                    // 3. 触发回调，通知 KMP 侧当前连接信息
                    //                    self.stateChangeCallback?(connectedState)
                }

            }
            .store(in: &disposeBag)  // 存储订阅，防止它被立即销毁

        // 3. 订阅 Session Rejection Publisher (监听连接拒绝或失败)
        AppKit.instance.sessionRejectionPublisher
            .sink { [weak self] (proposal, reason) in
                guard let self = self else { return }

                print("监听连接拒绝")

                let disconnectedState = WalletConnectionState.Disconnected()
                self.stateChangeCallback?(disconnectedState)

            }
            .store(in: &disposeBag)

        AppKit.instance.sessionResponsePublisher
            .sink { [weak self] (w3MResponse) in
                guard let self = self else { return }

                //print("Request response: \(w3MResponse.result) ")
                //w3MResponse.result.
                switch w3MResponse.result {
                case let .response(value):
                    print("Session response: \(value.stringRepresentation)")
                    let c : Int64 = 0
                    let resState = WalletConnectionState.ResError(code: c, message:"交易成功")
                    self.stateChangeCallback?(resState)
                case let .error(error):
                    print( "Session error: \(error)")
                    let resState = WalletConnectionState.ResError(code: Int64(error.code), message: "交易失败: \(error.message)")
                    self.stateChangeCallback?(resState)
                }
                

                // 触发拒绝回调
                //self.sessionRejectedCallback?(reason.message, proposal.topic)

                // 也可以通过 connectionStateCallback 报告连接失败
                // self.connectionStateCallback?(nil, nil, false)
            }
            .store(in: &disposeBag)

        let sessions = AppKit.instance.getSessions()
        print(" sessions count is \(sessions.count)")
        let firstSession = sessions.first
        //        if (firstSession == nil){
        //            print("init setup: no session")
        //        }else{
        if let firstSession = firstSession {

            let address = firstSession.namespaces.values.first?.accounts.first?
                .address  // 简化处理，取第一个地址
            let chaidId = firstSession.namespaces.values.first?.chains?.first?
                .namespace
            let abs = firstSession.namespaces.values.first?.chains?.first?
                .absoluteString
            let des = firstSession.namespaces.values.first?.chains?.first?
                .description

            sessionTopic = firstSession.topic
            //let chainId = session.namespaces.values.first?.chains.first?.namespace// 简化处理，取第一个 Chain ID
            print(
                "Resuse 连接, \(String(describing: firstSession.expiryDate)) chaindId is \(chaidId), abs: \(abs) des: \(des)"
            )
            let kotlinAccounts = convertSwiftAccountsToKotlin(
                accounts: firstSession.accounts
            )

            // 触发回调，通知 KMP 侧连接成功
            // 2. 构建 WalletConnectionState.Connected 状态
            let connectedState = WalletConnectionState.Connected(
                topic: sessionTopic,
                accounts: kotlinAccounts
            )

            // 3. 触发回调，通知 KMP 侧连接成功
            self.stateChangeCallback!(connectedState)

        } else {
            print("init setup: no session")
            let disconnectedState = WalletConnectionState.Disconnected()
            self.stateChangeCallback?(disconnectedState)

        }

    }

    @objc override public init() {
        super.init()
        // 可以在这里设置 internalAppkit 的状态监听，并将状态转发给 stateChangeCallback
        // Initialize metadata

    }

    // 实现 KMP 接口方法 (Swift 中的方法名会根据 Kotlin 方法名自动生成，通常是小驼峰)

    // setWalletStateCallback 的 Swift 实现
    @objc public func setWalletStateCallback(
        callback: @escaping (WalletConnectionState) -> Void
    ) {
        self.stateChangeCallback = callback
    }

    //    AppkitInitParams(
    //                   projectId = "3295917dc4c50eaf2208e6ebb3dcc32f",
    //                   connectionType = "auto",
    //                   metaData = AppMetaData("My Wallet DApp", "KMP Sample", "https://example.com", emptyList(), "kotlin-sigad-wc://request")
    //               )
    @objc public func initialize(params: AppkitInitParams) -> Bool {
        // 示例：调用 ReownAppKit 的初始化
        print("Initializing ReownAppKit with params: \(params)")
        // ... ReownAppKit 初始化逻辑
        setup(params: params)
        return true
    }

    @objc public func connectToTrustWallet() {
        print("Connecting to TrustWallet...")

        // 1. 在一个独立的 Task 中执行所有的异步操作和耗时操作
        Task {
            do {
                // 2. 调用 AppKit.instance.connect() 获取 WalletConnect URI
                // 传入 nil，让 AppKit 返回 URI，以便我们手动处理跳转
                print(" connectToTrustWallet asyn begin....")
                let optionalUri: WalletConnectURI? = try await AppKit.instance
                    .connect(walletUniversalLink: nil)

                // 3. 安全解包 URI
                guard let uri = optionalUri else {
                    print(
                        " connectToTrustWallet asyn ⚠️ 连接成功，但未返回 URI (AppKit 内部可能在处理或等待)"
                    )
                    return
                }

                print(
                    " connectToTrustWallet asyn : ✅ 成功获取到 WalletConnect URI: \(uri.absoluteString)"
                )

                // 4. 🚀 构造 Trust Wallet Deep Link
                /*
                 // 修正：uri.absoluteString 已经是 "wc:..." 格式
                 let wcURIString = uri.absoluteString
                 let walletScheme = "trust" // Scheme 部分：trust
                 //let walletScheme = "metamask" // Scheme 部分：trust
                
                
                 // 对 URI 进行 URL 编码
                 guard let encodedURI = wcURIString.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
                 print("❌ 错误：WalletConnect URI 编码失败")
                 return
                 }
                
                 // 构造 Deep Link URL: trust://wc?uri={encodedURI}
                 let deepLinkUrlString = "\(walletScheme)://wc?uri=\(encodedURI)"
                
                 // 转换为 URL 对象
                 guard let url = URL(string: deepLinkUrlString) else {
                 print("❌ 错误：Deep Link URL 构造失败")
                 return
                 }
                 */

                // Trust Wallet 的 Universal Link 基础 URL
                let baseURL = CrossPlatformKt.baseWalletDeepLinkURL  //"https://link.trustwallet.com/wc"

                // 1. 构建完整的 Universal Link
                // Trust Wallet 的 Universal Link 格式通常是将 WC URI 编码后作为参数传递。
                // 格式通常是：https://link.trustwallet.com/wc?uri=<encoded_wc_uri>
                guard var urlComponents = URLComponents(string: baseURL) else {
                    print("Error: Invalid base URL")
                    return
                }

                // 2. 将 WalletConnect URI 添加为查询参数
                let uriQueryItem = URLQueryItem(
                    name: "uri",
                    value: uri.absoluteString
                )
                urlComponents.queryItems = [uriQueryItem]

                guard let url = urlComponents.url else {
                    print("Error: Could not construct final Universal Link.")
                    return
                }
                // 5. 📞 在主线程执行跳转
                DispatchQueue.main.async {
                    if UIApplication.shared.canOpenURL(url) {
                        // 尝试打开 Trust Wallet
                        print(" openurl : \(url.absoluteString)")
                        UIApplication.shared.open(
                            url,
                            options: [:],
                            completionHandler: nil
                        )
                    } else {
                        // Trust Wallet 未安装
                        print("⚠️ 钱包应用未安装。请引导用户扫码或安装 Trust Wallet。")
                        //
                        // 实际产品中，您应该在这里触发二维码显示逻辑
                    }
                }

            } catch {
                // 6. 错误处理
                print("❌ WalletConnect 连接过程中发生错误: \(error)")
            }
        }
    }

    // 关键方法：处理传入的 Deep Link URL
    @objc func handleDeeplink(url: URL) throws {
        // 1. 传递给 AppKit/WalletConnect SDK
        // 假设您的 AppKit SDK 有一个静态方法或单例方法来处理 URL
        // 必须确保这个方法是线程安全的，因为它可能在主线程被调用

        do {
            try AppKit.instance.handleDeeplink(url)  // 替换为你的 AppKit SDK 实际的 URL 处理方法
            print(
                "✅ Successfully handled WalletConnect URL: \(url.absoluteString)"
            )
        } catch {
            print("❌ Error handling WalletConnect URL: \(error)")
            throw error  // 重新抛出错误以便上层捕获
        }
    }
    // 示例：状态变化时，调用回调函数更新 Kotlin StateFlow
    // stateChangeCallback?(.Connecting, nil, nil)
    //}

    @objc public func disconnect() {
        // ...
        print(" wallet disconnectWallet ios bridge implement...")
        Task {
            do {
                try await AppKit.instance.disconnect(topic: sessionTopic)
            } catch {
                print("❌ Error disconnect: \(error)")
                throw error  // 重新抛出错误以便上层捕获
            }
        }
    }

    @objc public func fetchBalances(address: String) -> [String] {
        // 示例：返回 List<String>
        return ["1.5 ETH", "1000 USDT"]
    }

    @objc public func sendTransaction(transactionParam: String) -> Bool {
        // ...
        //                AppKit.request(
        //                     //request = Modal.Params.Request(
        //                     request = com.reown.appkit.client.models.request.Request(
        //                         method = "eth_sendTransaction",
        //                         params = transactionParam
        //                     ),
        //                     onSuccess = { result: SentRequestResult ->
        //                         println("Transaction result: ${result}")
        //                     },
        //                     onError = { error: Throwable ->
        //                         println("Transaction failed: $error")
        //                         _walletState.value = WalletConnectionState.Error("Transaction failed: $error");
        //                     }
        //                 )

        let sessions = AppKit.instance.getSessions()
        let firstSession = sessions.first
        if firstSession == nil {
            print("sendTranascation: no session")
            return false
        }
        for i in 0..<sessions.count {
            //firstSession?.namespaces.first.chainName

            print(
                "i=\(i) topic is \(sessions[i].topic), expirDate is \(sessions[i].expiryDate)"
            )
        }
        //        print(" session length/* */is \(sessions.count) firsttopc \(firstSession?.topic), sessionTopic \(sessionTopic)")
        //sessionTopic = firstSession!.topic

        //        let transactionParam0: [[String: Any]] = [[
        //            "from": "0xBEf2Bd3B13D66Bcf2d3D9EA86f43b6E9F7A0f8E0",
        //            "to": "0xFdDD454E921F5FCDf0fF3399eB7A8ac4dF57B1a3",
        //            "value": "0x51b660cdd58000"
        //        ]]

        //        let transactionParam0: [[String: AnyCodable]] = [[
        //            "from": AnyCodable("0xBEf2Bd3B13D66Bcf2d3D9EA86f43b6E9F7A0f8E0"),
        //            "to": AnyCodable("0xFdDD454E921F5FCDf0fF3399eB7A8ac4dF57B1a3"),
        //            "value": AnyCodable("0x51b660cdd58000")
        //        ]]
        //
        //        let transactionParam0: [[String: AnyCodable]] = [[
        //            "from": AnyCodable("0xBEf2Bd3B13D66Bcf2d3D9EA86f43b6E9F7A0f8E0"),
        //            "to": AnyCodable("0xFdDD454E921F5FCDf0fF3399eB7A8ac4dF57B1a3"),
        //            "value": AnyCodable("0x51b660cdd58000")
        //        ]]

        Task {
            do {

                guard let jsonData = transactionParam.data(using: .utf8) else {
                    throw NSError(
                        domain: "JsonError",
                        code: 1,
                        userInfo: [NSLocalizedDescriptionKey: "无法将字符串转换为数据"]
                    )
                }

                // 2. 使用 JSONSerialization 反序列化为 Swift 原生结构
                // 预期结构是 JSON 数组，包含字典
                let transactionParamsObject = try JSONSerialization.jsonObject(
                    with: jsonData,
                    options: []
                )

                // 3. 封装到 AnyCodable
                // 注意：这里的 AnyCodable 构造函数取决于您 AppKit 的实现
                let params: AnyCodable

                // 检查并转换类型以确保 AnyCodable 能够接受
                if let array = transactionParamsObject as? [[String: Any]] {
                    // 使用带有 Any 类型参数的构造函数进行封装
                    params = AnyCodable(any: array)
                } else {
                    // 如果结构不符合预期，抛出错误
                    throw NSError(
                        domain: "JsonError",
                        code: 2,
                        userInfo: [
                            NSLocalizedDescriptionKey: "JSON 结构不匹配预期的交易参数格式"
                        ]
                    )
                }

                //251208: trust app return nothing, or the response can be decrypted by the symeric key, but can't be decode by JSON
                let request = try Request(
                    topic: sessionTopic,
                    method: "eth_sendTransaction",
                    params: params,
                    //params: jsonString,//AnyCodable(any: decoded),
                    chainId: Blockchain("eip155:84532")!
                )
                print("Encoded request: \(request)")
                try await AppKit.instance.request(
                    params: request
                )
                
                let baseURL = CrossPlatformKt.baseWalletDeepLinkURL  //"https://link.trustwallet.com/wc"

                // 1. 构建完整的 Universal Link
                // Trust Wallet 的 Universal Link 格式通常是将 WC URI 编码后作为参数传递。
                // 格式通常是：https://link.trustwallet.com/wc?uri=<encoded_wc_uri>
                guard var urlComponents = URLComponents(string: baseURL) else {
                    print("Error: Invalid base URL")
                    return
                }

                guard let url = urlComponents.url else {
                    print("Error: Could not construct final Universal Link.")
                    return
                }
                //  在主线程执行跳转
                DispatchQueue.main.async {
                    if UIApplication.shared.canOpenURL(url) {
                        // 尝试打开 Trust Wallet
                        print(" openurl : \(url.absoluteString)")
                        UIApplication.shared.open(
                            url,
                            options: [:],
                            completionHandler: nil
                        )
                    } else {
                        // Trust Wallet 未安装
                        print("⚠️ 钱包应用未安装。请引导用户扫码或安装 ")
                        //
                        // 实际产品中，您应该在这里触发二维码显示逻辑
                    }
                }
            } catch let error as NSError{
                print("❌ Error sendTransaction: \(error)")
                
                let resState = WalletConnectionState.ResError(code: Int64(error.code), message: "交易失败: \(error.description)")
                self.stateChangeCallback?(resState)

                
            }
        }
        return true
    }

    @objc public func generateReceiveQRCode(address: String) -> String {
        return "qrcode_for_\(address)"
    }
}
