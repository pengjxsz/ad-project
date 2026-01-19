//
//  DefaultSocketFactory.swift
//  iosApp
//
//  Created by XAIO on 4/12/25.
//  Copyright © 2025 orgName. All rights reserved.
//


import Foundation
import WalletConnectRelay
import Starscream
//import reown_swift

extension WebSocket: WebSocketConnecting { }

struct DefaultSocketFactory: WebSocketFactory {
    func create(with url: URL) -> WebSocketConnecting {
        let socket = WebSocket(url: url)
        let queue = DispatchQueue(label: "com.walletconnect.sdk.sockets", attributes: .concurrent)
        socket.callbackQueue = queue
        return socket
    }
}
