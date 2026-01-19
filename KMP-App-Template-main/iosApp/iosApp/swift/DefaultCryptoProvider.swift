//
//  DefaultCryptoProvider.swift
//  iosApp
//
//  Created by XAIO on 4/12/25.
//  Copyright © 2025 orgName. All rights reserved.
//


import Foundation
import Web3
import CryptoSwift
//import reown_swift
import CryptoSwift
import WalletConnectSigner

struct DefaultCryptoProvider: CryptoProvider {

    public func recoverPubKey(signature: EthereumSignature, message: Data) throws -> Data {
        let publicKey = try EthereumPublicKey(
            //message: message.bytes,
            message: [UInt8](message), // FIXED
            v: EthereumQuantity(quantity: BigUInt(signature.v)),
            r: EthereumQuantity(signature.r),
            s: EthereumQuantity(signature.s)
        )
        return Data(publicKey.rawPublicKey)
    }

    public func keccak256(_ data: Data) -> Data {
        let digest = SHA3(variant: .keccak256)
        let hash = digest.calculate(for: [UInt8](data))
        return Data(hash)
    }

}
