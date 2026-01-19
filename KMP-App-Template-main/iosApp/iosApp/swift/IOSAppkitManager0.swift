// iosMain/Swift/IOSAppkitManager.swift
// 必须确保这个文件被编译到 KMP 的 iOS 框架中

import Foundation
//import UIKit
import ReownAppKit
//import WalletConnectSign
//import WalletConnectUtils

// 定义状态变更的闭包类型，便于 Kotlin 调用
//@objc(AppKitStateChangeCallback)
public typealias AppKitStateChangeCallback = (
    _ newState: NSString,
    _ address: NSString?,
    _ topic: NSString?,
    _ error: NSString?
) -> Void


@objcMembers
public class IOSAppkitManager: NSObject {
//    @objc public init(onStateChange: @escaping (NSString, NSString?, NSString?, NSString?) -> Void) {
//        super.init()
//    }
    
    // 显式重写并暴露无参数的 Objective-C 构造函数
        // 必须有 @objc 标记，因为 Swift init() 默认是隐藏的
        @objc override public init() {
            // 在这里对您的属性进行安全初始化。
            // 例如，如果您的回调属性是可选的，或者有默认值：
            // self.onStateChange = nil // 假设 onStateChange 可以为 nil
            
            super.init()
        }
}


/*
// 必须继承 NSObject 并使用 @objcMembers 才能在 Kotlin/Native 中看到
@objcMembers
public class IOSAppkitManager: NSObject {

    private let onStateChange: AppKitStateChangeCallback

    // 必须持有 ModalClientDelegate 的实例，否则它会被释放
    private var modalDelegate: IOSAppkitDelegate?

    public init(onStateChange: @escaping AppKitStateChangeCallback) {
        self.onStateChange = onStateChange
        super.init()
        // 实例化 Delegate
        self.modalDelegate = IOSAppkitDelegate(callback: self.onStateChange)
        // 注册 Delegate
        ReownAppKit.setDelegate(delegate: self.modalDelegate!)
    }

    @objc public func initialize(projectId: String, name: String, description: String, url: String, icons: [String], redirect: String) -> Bool {
        // --- CoreClient 初始化 ---
        let connectionType: Core.Model.ConnectionType = .automatic // 简化处理，假设为 .automatic
        let appMetaData = Core.Model.AppMetaData(
            name: name,
            description: description,
            url: URL(string: url)!,
            icons: icons.compactMap { URL(string: $0) },
            redirect: URL(string: redirect) // 假设 redirect 格式正确
        )

        do {
            try CoreClient.initialize(
                projectId: projectId,
                connectionType: connectionType,
                metaData: appMetaData
            )
        } catch {
            print("CoreClient.initialize failed: \(error)")
            onStateChange("Error" as NSString, nil, nil, error.localizedDescription as NSString)
            return false
        }

        // --- ReownAppKit 初始化 ---
        let initParams = Modal.Params.Init(
            coreClient: CoreClient,
            includeWalletIds: ["trustwallet"] // 仅连接 TrustWallet
        )

        var isSuccess = false
        let semaphore = DispatchSemaphore(value: 0)

        ReownAppKit.initialize(
            init: initParams,
            onSuccess: {
                print("ReownAppKit initialized successfully")
                isSuccess = true
                semaphore.signal()
            },
            onError: { error in
                print("ReownAppKit initialize failed: \(error.localizedDescription)")
                self.onStateChange("Error" as NSString, nil, nil, error.localizedDescription as NSString)
                isSuccess = false
                semaphore.signal()
            }
        )

        // 阻塞等待初始化结果 (仅用于 KMP 的同步调用，生产环境应使用 Swift Concurrency)
        // ⚠️ 在主线程调用时要小心死锁。在 KMP 中，`initialize` 是 `suspend` 函数，
        // 应该在后台线程被调用，以避免阻塞 UI。
        _ = semaphore.wait(timeout: .now() + 5.0)

        if isSuccess {
            ReownAppKit.setChains(chains: AppKitChainsPresets.ethChains.values.compactMap { $0 })
        }

        return isSuccess
    }

    // 返回 WalletConnect URI
    @objc public func connect() -> NSString? {
        do {
            // 1. 创建 Pairing
            guard let pairing = CoreClient.Pairing.create() else {
                return nil
            }

            // 2. 构造 Namespaces
            let ethNamespace = Modal.Model.Namespace.Proposal(
                chains: [Blockchain("eip155:1")!], // 主网
                methods: ["eth_sendTransaction", "personal_sign"],
                events: ["chainChanged", "accountsChanged"]
            )

            let connectParams = Modal.Params.Connect(
                namespaces: ["eip155": ethNamespace],
                optionalNamespaces: nil,
                properties: nil,
                pairing: pairing
            )

            // 3. Connect - 这将发起 WalletConnect 会话，但不会自动打开钱包 APP
            ReownAppKit.connect(
                connect: connectParams,
                onSuccess: { session in
                    print("ReownAppKit connected (channel set)")
                    // 成功连接后，回调会在 Delegate 中处理
                },
                onError: { error in
                    print("Connection failed: \(error.localizedDescription)")
                    self.onStateChange("Error" as NSString, nil, nil, error.localizedDescription as NSString)
                }
            )

            // 返回 URI 供 Kotlin 侧打开 Trust Wallet
            return pairing.uri as NSString

        } catch {
            print("Connect failed: \(error)")
            onStateChange("Error" as NSString, nil, nil, error.localizedDescription as NSString)
            return nil
        }
    }

    @objc public func disconnect() {
        // ReownAppKit.disconnect 的实现
        ReownAppKit.disconnect(
            onSuccess: {
                print("Disconnected successfully")
                self.onStateChange("Disconnected" as NSString, nil, nil, nil)
            },
            onError: { error in
                print("Disconnection failed: \(error.localizedDescription)")
                self.onStateChange("Error" as NSString, nil, nil, error.localizedDescription as NSString)
            }
        )
    }

    // 示例：查询余额，这里可能需要一个异步/回调机制，简化为同步返回空
    @objc public func fetchBalances(_ address: String) -> NSString {
        // 实际应用中，您会使用 ReownAppKit 的 rpcRequest 或一个外部服务来获取余额
        return "" as NSString
    }

    // 示例：发送交易
    @objc public func sendTransaction(_ transactionParam: String) {
        guard let session = CoreClient.session else {
             self.onStateChange("Error" as NSString, nil, nil, "Not connected" as NSString)
             return
        }

        // ⚠️ 实际交易逻辑需要解析 transactionParam 并调用 ReownAppKit.request()
        // 由于没有具体的 transactionParam 结构，这里仅作骨架展示
        /*
        let requestParams: Any = // 解析 transactionParam 为 Swift/JSON 对象
        ReownAppKit.request(
            session: session,
            chainId: Blockchain("eip155:1")!,
            method: "eth_sendTransaction",
            params: requestParams,
            onSuccess: { response in
                // onSessionRequestResponse 会在 delegate 中处理
            },
            onError: { error in
                // onSessionRequestResponse 会在 delegate 中处理
            }
        )
        */
         self.onStateChange("Error" as NSString, nil, nil, "Send transaction not implemented yet" as NSString)
    }
}


// MARK: - ReownAppKit 委托实现

@objcMembers
public class IOSAppkitDelegate: NSObject, ReownAppKit.ModalDelegate {

    private let callback: AppKitStateChangeCallback

    public init(callback: @escaping AppKitStateChangeCallback) {
        self.callback = callback
    }

    public func onSessionApproved(approvedSession: ReownAppKit.Modal.Model.ApprovedSession) {
        // 1. 获取账户列表
        guard let wcSession = approvedSession as? Modal.Model.WalletConnectSession,
              let account = wcSession.namespaces["eip155"]?.accounts.first else {
            callback("Error" as NSString, nil, nil, "Approved session missing eip155 account" as NSString)
            return
        }

        // 2. 解析出纯地址 (eip155:Chain ID:Address)
        let parts = account.absoluteString.split(separator: ":")
        let address = parts.last!

        // 3. 更新状态
        callback("Connected" as NSString, String(address) as NSString, wcSession.topic as NSString, nil)
    }

    public func onSessionRejected(rejectedSession: ReownAppKit.Modal.Model.RejectedSession) {
        callback("Error" as NSString, nil, nil, "User rejected connection" as NSString)
    }

    public func onSessionDelete(deletedSession: ReownAppKit.Modal.Model.DeletedSession) {
        callback("Disconnected" as NSString, nil, nil, "Wallet disconnected" as NSString)
    }

    public func onError(error: ReownAppKit.Modal.Model.Error) {
        callback("Error" as NSString, nil, nil, error.localizedDescription as NSString)
    }

    public func onSessionRequestResponse(response: ReownAppKit.Modal.Model.SessionRequestResponse) {
        // 处理交易或签名请求的响应
        switch response.result {
        case .jsonRpcError(let error):
            let errorString = "\(error.code):\(error.message)"
            // 使用 ResError 状态来表示请求执行失败
            callback("ResError" as NSString, nil, nil, errorString as NSString)
        case .jsonRpcResult(let result):
            // 交易成功，可以解析 result 中的 tx hash
            let message = "Transaction Hash: \(result.result)"
            // 可以选择更新到 Connected 状态，或者添加一个专门的 TransactionSuccess 状态
            print(message)
            // 保持连接状态不变
        default:
            break
        }
    }

    // 其他 Delegate 方法...
    public func onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {}
    public func onSessionExtend(session: Modal.Model.Session) {}
    public func onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {}
    public func onProposalExpired(proposal: Modal.Model.ExpiredProposal) {}
    public func onRequestExpired(request: Modal.Model.ExpiredRequest) {}
    public func onConnectionStateChange(state: Modal.Model.ConnectionState) {
        if !state.isConnected {
            callback("Disconnected" as NSString, nil, nil, "Connection state changed to Disconnected" as NSString)
        }
    }
}
*/
