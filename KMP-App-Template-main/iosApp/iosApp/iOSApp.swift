
import SwiftUI
import CoreBluetooth
import CoreNFC
import SystemConfiguration
import UIKit
import ComposeApp

// MARK: - 蓝牙状态枚举
enum BluetoothStatus {
    case unknown
    case poweredOnAndAuthorized      // 蓝牙开启 + 权限允许
    case poweredOff                  // 系统蓝牙关闭
    case unauthorized                // 用户拒绝权限
    case unsupported                 // 设备不支持
    case resetting                   // 蓝牙正在重置
}

// MARK: - NFC状态枚举
enum NFCStatus {
    case available                   // NFC可用
    case unauthorized                // 权限被拒绝
    case unsupported                 // 设备不支持
    case unknown
}

// MARK: - WiFi状态枚举
enum WiFiStatus {
    case available                   // 网络可用
    case unavailable                 // 网络不可用
    case unsupported                 // 设备不支持（模拟器或某些设备）
}

// MARK: - 蓝牙检查器（静默检查，按需请求权限）
class BluetoothChecker: NSObject {
    private var centralManager: CBCentralManager?
    private var completion: ((BluetoothStatus) -> Void)?
    private var hasReceivedCallback = false
    private var shouldRequestPermission = false
    private var permissionRequestStartTime: Date?
    
    func checkBluetoothStatus(requestPermissionIfNeeded: Bool = true, completion: @escaping (BluetoothStatus) -> Void) {
        // 清理之前的实例
        cleanup()
        
        self.completion = completion
        self.hasReceivedCallback = false
        self.shouldRequestPermission = requestPermissionIfNeeded
        self.permissionRequestStartTime = Date()
        
        // 创建新的CBCentralManager实例
        centralManager = CBCentralManager(delegate: self, queue: nil)
        
        // 设置超时保护（防止状态一直为unknown）
        DispatchQueue.main.asyncAfter(deadline: .now() + 8.0) { [weak self] in
            guard let self = self, !self.hasReceivedCallback else { return }
            
            let status = self.mapStateToStatus(self.centralManager?.state ?? .unknown)
            self.completion?(status)
            self.cleanup() // 超时后也必须释放资源
        }
    }
    
    private func mapStateToStatus(_ state: CBManagerState) -> BluetoothStatus {
        switch state {
        case .poweredOn:
            return .poweredOnAndAuthorized
        case .poweredOff:
            return .poweredOff
        case .unauthorized:
            return .unauthorized
        case .unsupported:
            return .unsupported
        case .resetting:
            return .resetting
        case .unknown:
            // 检查是否在权限请求期间
            if let startTime = permissionRequestStartTime,
               Date().timeIntervalSince(startTime) < 3.0 {
                // 如果是初始检查阶段，可能是权限还未确定
                return shouldRequestPermission ? .unauthorized : .unsupported
            } else {
                // 如果超过了权限请求时间，可能是权限被拒绝
                return .unauthorized
            }
        @unknown default:
            return .unknown
        }
    }
    
    private func cleanup() {
        centralManager = nil
        completion = nil
        hasReceivedCallback = false
        shouldRequestPermission = false
        permissionRequestStartTime = nil
    }
}

// MARK: - CBCentralManagerDelegate
extension BluetoothChecker: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        guard !hasReceivedCallback else { return }
        
        let status = mapStateToStatus(central.state)
        
        // 特殊处理重置状态：等待一段时间看是否恢复正常
        if status == .resetting {
            // 延迟检查，给蓝牙重置一些时间
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) { [weak self] in
                guard let self = self, !self.hasReceivedCallback else { return }
                
                // 检查重置后的新状态
                let finalStatus = self.mapStateToStatus(central.state)
                self.hasReceivedCallback = true
                self.completion?(finalStatus)
                self.cleanup()
            }
        } else if status == .unauthorized && shouldRequestPermission {
            // 如果是未授权状态并且允许请求权限，则延迟一下让系统有机会弹出权限请求
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) { [weak self] in
                guard let self = self, !self.hasReceivedCallback else { return }
                
                // 再次检查状态，看看权限对话框是否有结果
                let finalStatus = self.mapStateToStatus(central.state)
                self.hasReceivedCallback = true
                self.completion?(finalStatus)
                self.cleanup()
            }
        } else {
            hasReceivedCallback = true
            completion?(status)
            cleanup() // 状态更新后立即释放资源
        }
    }
}

// MARK: - NFC检查器（只检查硬件支持）
class NFCChecker {
    private var completion: ((NFCStatus) -> Void)?
    
    func checkNFCStatus(completion: @escaping (NFCStatus) -> Void) {
        self.completion = completion
        
        // 检查NFC是否可用（仅检查硬件支持）
        // 在iOS中，NFC硬件无法手动关闭，它由系统管理
        // readingAvailable只检查设备是否具备NFC硬件
        if NFCNDEFReaderSession.readingAvailable {
            // 设备支持NFC硬件
            DispatchQueue.main.async {
                completion(.available)
            }
        } else {
            // 设备不支持NFC
            DispatchQueue.main.async {
                completion(.unsupported)
            }
        }
        
        // 清理
        self.completion = nil
    }
}

// MARK: - WiFi检查器
class WiFiChecker {
    private var completion: ((WiFiStatus) -> Void)?
    
    func checkWiFiStatus(completion: @escaping (WiFiStatus) -> Void) {
        self.completion = completion
        
        // 检查网络连接状态
        let reachability = SCNetworkReachabilityCreateWithName(nil, "www.apple.com")
        var flags = SCNetworkReachabilityFlags()
        
        if let reachability = reachability, SCNetworkReachabilityGetFlags(reachability, &flags) {
            let isReachable = flags.contains(.reachable)
            _ = !flags.contains(.isWWAN) && isReachable  // isWWAN表示通过蜂窝网络连接
            
            if isReachable {
                DispatchQueue.main.async {
                    completion(.available)
                }
            } else {
                DispatchQueue.main.async {
                    completion(.unavailable)
                }
            }
        } else {
            // 如果无法获取网络状态，假定网络可用
            DispatchQueue.main.async {
                completion(.available)
            }
        }
        
        // 清理
        self.completion = nil
    }
}

// MARK: - 设备能力检查器（串行检查，全部完成后再决定是否退出）
class DeviceCapabilityChecker: ObservableObject {
    @Published var shouldExitApp = false
    @Published var exitReason = ""
    
    private let bluetoothChecker = BluetoothChecker()
    private let nfcChecker = NFCChecker()
    private let wifiChecker = WiFiChecker()
    
    func checkCapabilitiesAndExitIfNotSupported() {
        print("开始串行检查设备能力...")
        
        // 存储检查结果
        var bluetoothStatus: BluetoothStatus?
        var nfcStatus: NFCStatus?
        var wifiStatus: WiFiStatus?
        
        // 串行执行检查：蓝牙 -> NFC -> WiFi
        checkBluetooth { status in
            bluetoothStatus = status
            print("蓝牙检查完成: \(status)")
            
            self.checkNFC { status in
                nfcStatus = status
                print("NFC检查完成: \(status)")
                
                self.checkWiFi { status in
                    wifiStatus = status
                    print("网络检查完成: \(status)")
                    
                    // 所有检查完成后，汇总结果
                    self.evaluateAllResults(
                        bluetooth: bluetoothStatus!,
                        nfc: nfcStatus!,
                        wifi: wifiStatus!
                    )
                }
            }
        }
    }
    
    private func evaluateAllResults(bluetooth: BluetoothStatus, nfc: NFCStatus, wifi: WiFiStatus) {
        print("评估所有检查结果...")
        
        // 检查是否有任何一项失败
        var failureFound = false
        var failureMessages: [String] = []
        
        switch bluetooth {
        case .poweredOnAndAuthorized:
            print("✅ 蓝牙检查通过")
        case .poweredOff:
            failureFound = true
            failureMessages.append("蓝牙未开启 - 请在设置中打开蓝牙功能")
        case .unauthorized:
            failureFound = true
            failureMessages.append("蓝牙权限被拒绝 - 请在设置中允许蓝牙权限")
        case .unsupported:
            failureFound = true
            failureMessages.append("设备不支持蓝牙功能")
        case .unknown:
            failureFound = true
            failureMessages.append("蓝牙状态未知")
        case .resetting:
            failureFound = true
            failureMessages.append("蓝牙正在重置中")
        }
        
        switch nfc {
        case .available:
            print("✅ NFC检查通过")
        case .unauthorized:
            failureFound = true
            failureMessages.append("NFC权限被拒绝 - 请在设置中允许NFC权限")
        case .unsupported:
            failureFound = true
            failureMessages.append("设备不支持NFC功能")
        case .unknown:
            failureFound = true
            failureMessages.append("NFC状态未知")
        }
        
        switch wifi {
        case .available:
            print("✅ 网络检查通过")
        case .unavailable:
            failureFound = true
            failureMessages.append("网络不可用 - 请检查网络连接")
        case .unsupported:
            failureFound = true
            failureMessages.append("设备不支持网络功能")
        }
        
        // 如果有失败项，显示退出对话框
        if failureFound {
            let title = "功能受限"
            let message = failureMessages.joined(separator: "\n")
            setExitCondition(title: title, message: message)
        } else {
            print("✅ 所有检查通过")
        }
    }
    
    private func checkBluetooth(completion: @escaping (BluetoothStatus) -> Void) {
        bluetoothChecker.checkBluetoothStatus(requestPermissionIfNeeded: true) { status in
            DispatchQueue.main.async {
                completion(status)
            }
        }
    }
    
    private func checkNFC(completion: @escaping (NFCStatus) -> Void) {
        nfcChecker.checkNFCStatus { status in
            DispatchQueue.main.async {
                completion(status)
            }
        }
    }
    
    private func checkWiFi(completion: @escaping (WiFiStatus) -> Void) {
        wifiChecker.checkWiFiStatus { status in
            DispatchQueue.main.async {
                completion(status)
            }
        }
    }
    
    private func setExitCondition(title: String, message: String) {
        DispatchQueue.main.async {
            self.exitReason = "\(title)|\(message)"
            self.shouldExitApp = true
        }
    }
    
    func exitApp() {
        print("应用即将退出...")
        
        // 在主线程上延迟退出，确保UI更新完成
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            exit(0)
        }
    }
}

// MARK: - 退出确认视图
struct ExitConfirmationView: View {
    @ObservedObject var checker: DeviceCapabilityChecker
    
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "exclamationmark.triangle")
                .font(.largeTitle)
                .foregroundColor(.orange)
            
            Text("功能受限")
                .font(.title)
                .bold()
            
            Text(getMessage())
                .multilineTextAlignment(.center)
                .padding()
            
            HStack(spacing: 20) {
                Button("前往设置") {
                    if let settingsUrl = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(settingsUrl)
                    }
                }
                .buttonStyle(.bordered)
                
                Button("退出应用") {
                    checker.exitApp()
                }
                .buttonStyle(.bordered)
                .tint(.red)
            }
        }
        .padding()
        .onAppear {
            print("显示退出确认视图")
        }
    }
    
    private func getMessage() -> String {
        let parts = checker.exitReason.split(separator: "|")
        let message = String(parts.last ?? "应用需要蓝牙、NFC和网络功能才能正常运行。")
        
        // 将多行错误信息转换为列表格式
        var lines = message.components(separatedBy: "\n")
        lines.append("如果已经设置好，则请点“退出应用”后重新打开应用")
        return lines.map { "• \($0)" }.joined(separator: "\n")
    }
}

// MARK: - 修改后的现有应用结构
@main
struct iOSApp: App {
    @StateObject private var deviceChecker = DeviceCapabilityChecker()
    
    init() {
        //-----------------------------------
              // 步骤 A: Swift 侧平台桥接初始化（必须在 Koin 初始化前完成）
              // ----------------------------------------------------
          // 1. 实例化 Swift 实现类
          let bridgeImpl = IOSAppkitManager()
          // 2. 将实现注入到 KMP 模块中定义的全局属性
          // IosAppkitBridgeKt 是 KMP 编译器将 IosAppkitBridge.kt 文件导出的模块名
          IosAppkitBridgeKt.AppkitBridgeInstance = bridgeImpl

          // ----------------------------------------------------
                  // 步骤 B: Koin 初始化
                  // ----------------------------------------------------
          KoinKt.doInitKoin(appDeclaration: { _ in
                      // 如果未来 iOS 需要特定的 Koin 配置，代码会放在这里
                  })
     }
    
    var body: some Scene {
        WindowGroup {
            Group {
                if deviceChecker.shouldExitApp {
                    ExitConfirmationView(checker: deviceChecker)
                        .onAppear {
                            // 可以在这里添加任何必要的逻辑
                        }
                } else {
                    // 主应用内容
                    ContentView()
                        .onReceive(deviceChecker.$shouldExitApp) { shouldExit in
                            if shouldExit {
                                // 可以在这里添加任何必要的逻辑
                            }
                        }
                        //核心：使用 onOpenURL 捕获传入的 Deep Link
                        .onOpenURL { url in
                            // 确保您的桥接实例可以被访问
                            if let manager = IosAppkitBridgeKt.AppkitBridgeInstance as? IOSAppkitManager {
                                do {
                                    // 呼叫 manager 的处理函数
                                    print("conneted wallet url: \(url)")
                                    try manager.handleDeeplink(url: url)
                                } catch {
                                    // 处理 manager 抛出的错误
                                    print("Fatal Error during Deep Link processing: \(error)")
                                }
                            } else {
                                print("Error: IOSAppkitManager instance not found.")
                            }
                        }
                }
            }
            .onChange(of: deviceChecker.shouldExitApp) { newValue in
                if newValue {
                    // 可以在这里添加任何必要的逻辑
                }
            }
            .onAppear {
                // 在应用启动时检查设备能力
                deviceChecker.checkCapabilitiesAndExitIfNotSupported()
            }
        }
    }
}

// MARK: - UIDevice扩展，用于检测模拟器
extension UIDevice {
    var isSimulator: Bool {
        #if targetEnvironment(simulator)
        return true
        #else
        return false
        #endif
    }
}






