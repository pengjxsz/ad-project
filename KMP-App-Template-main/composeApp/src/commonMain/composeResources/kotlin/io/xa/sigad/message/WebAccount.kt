package io.xa.sigad.message

import kotlinx.serialization.Serializable


/**
 * 1 device : deviceChip, devicePK must exist
 *   if not, bind and init
 * 2 user_id must exist
 *   if not, register and save.
 *   when registered, masterPK, chatPrivK, chatPK, chatPrivK are initialized again
 */
@Serializable
data class WebAccount(
    val user_id: String = "",
    val masterPK: String = "",
    val masterKey: String = "",
    val chatKey: String = "",
    val chatPK: String = "",

    val deviceChip: String = "",
    val devicePK: String = "",

    val deviceWith: Long = 0,
    val deviceHeight: Long = 0,
    val user_nick: String = "",
    val device_colors: Long = 0
){
    //Reserved for local post test/usage
    var host = "127.0.0.1"   //119.23.248.75
    var port = 400 //7000
}

/**
 * getnereate an account as pre-regisger accout
 * after regiestering this account, user_id will be return
 * if bind to device, deviceChip, devicePK will be return
 */
fun createWebAaccount(devicePK: String="",deviceChip: String=""): WebAccount{
    val tmpAccount = generateUnRegisteredAcount()
    val masterPubKeyHash = secp256k1GetPublicKeyFromPrivate(tmpAccount.masterKey, false)
    val chatPubKeyHash = secp256k1GetPublicKeyFromPrivate(tmpAccount.chatKey, false)
    //println("createWebAaccount: ${tmpAccount.userId} ${tmpAccount.chatKey} ${tmpAccount.masterKey}")

    return WebAccount(masterKey = tmpAccount.masterKey,
        masterPK="0x${masterPubKeyHash}",
        chatKey= tmpAccount.chatKey,
        chatPK = "0x${chatPubKeyHash}",
        deviceChip = deviceChip,
        devicePK = devicePK)
}