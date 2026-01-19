package io.xa.sigad.message


import fr.acinq.secp256k1.Hex
import fr.acinq.secp256k1.Secp256k1
import kotlin.io.encoding.*

import org.kotlincrypto.hash.sha3.Keccak256
import org.kotlincrypto.hash.sha2.SHA256


import io.ktor.utils.io.core.toByteArray
import kotlin.collections.get
import kotlin.text.get

private val HEX_CHARS = "0123456789abcdef".toCharArray()

/**
 * Converts a ByteArray to a hexadecimal string representation.
 * This implementation is pure Kotlin and suitable for Kotlin Multiplatform (KMP) commonMain.
 */
fun ByteArray.toHex(): String = buildString { // Use buildString for efficient string concatenation
    for (byte in this@toHex) { // Iterate over each byte in the ByteArray
        val hexChar1 = HEX_CHARS[(byte.toInt() shr 4) and 0x0F] // Get the high 4 bits
        val hexChar2 = HEX_CHARS[byte.toInt() and 0x0F] // Get the low 4 bits
        append(hexChar1)
        append(hexChar2)
    }
}

fun keccak256Hash(input: ByteArray): ByteArray {
    val digest = Keccak256()
    digest.update(input)
    return digest.digest()
}


fun sha256Hash(input: ByteArray): ByteArray {
    val digest = SHA256()
    digest.update(input)
    return digest.digest()
}

fun sha256Hash(input: String): ByteArray {
    val digest = SHA256()
    digest.update(input.encodeToByteArray())
    return digest.digest()
}

/**
 * Converts a hex string to a ByteArray.
 */
fun String.hexToByteArray(): ByteArray {
    return this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}

/**
 * Reverses a hex string (big-endian to little-endian).
 */
fun String.reverseHex(): String {
    return this.chunked(2).reversed().joinToString("")
}

/**
 * HASH => HEX ENCODE => Reverse
 */
fun POHash(content: String): String {
    val hash = sha256Hash(content).toHex().reverseHex()
    return hash
}
//fun String.reverseHex(): String {
//    val hexString = this
//    if (hexString.isEmpty()) {
//        return ""
//    }
//
//    val arr = mutableListOf<String>()
//    val rH = hexString.length
//
//    for (i in 2..rH step 2) {
//        arr.add(hexString.substring(rH - i, rH - i + 2))
//    }
//
//    return arr.joinToString("")
//}

fun secp256k1GetPublicKeyFromPrivate(privateKey: String, bCompress: Boolean =true): String {
    println("secp256k1GetPublicKeyFromPrivate privatekey " + privateKey);

    //Hex.decode("67E56582298859DDAE725F972992A07C6C4FB9F62A8FFF58CE3CA926A1063530".lowercase())
    //const ecPubCompressed = getPublicKey(Hex.decode(privateKey), true)
    val ecPubUnCompressed = Secp256k1.get().pubkeyCreate(Hex.decode(privateKey))
    println("ecPubUnCompressed uncompressed: "  + ecPubUnCompressed.toHex())
    if (!bCompress)
        return ecPubUnCompressed.toHex();

    val ecPubCompressed = Secp256k1.get().pubKeyCompress(ecPubUnCompressed)
    //println("ecPubCompressed " + (ecPubCompressed));
    println("ecPubCompressed hex " + (ecPubCompressed.toHex()));

    val swPublicKeyCompressed = (ecPubCompressed.toHex().reverseHex()).substring(0, 64)
    println("swPublicKeyCompressed " + swPublicKeyCompressed)

    val isEven = ecPubCompressed[0] == 0x02.toByte()
    val swPublicKeyCompressedWithHead = (if (isEven) "02" else "03") + swPublicKeyCompressed
    println("swPublicKeyCompressedWithHead $swPublicKeyCompressedWithHead")

    val cpkReversed =
        swPublicKeyCompressedWithHead.substring(0, 2) + swPublicKeyCompressedWithHead.substring(2)
            .reverseHex()
    println("cpkReversed $cpkReversed")

    //val cpkh = cpkReversed.hexToByteArray().sha256Hex().reverseHex()
    val cpkh = Hex.encode(sha256Hash(cpkReversed.hexToByteArray())).reverseHex();
    println("cpkh ${cpkh}")

    return cpkh
    /*
        const isEven = ProjectivePoint.fromPrivateKey(privateKey).y % 2n === 0n
        const swPublicKeyCompressedWithHead = (isEven ? '02' : '03') + swPublicKeyCompressed
        println("swPublicKeyCompressedWithHead ",swPublicKeyCompressedWithHead);

        const cpkReversed = swPublicKeyCompressedWithHead.slice(0, 2) + reverseHex(swPublicKeyCompressedWithHead.slice(2))
        println("cpkReversed ",cpkReversed);

        const cpkh = reverseHex(etc.bytesToHex(sha256(hexToBytes(cpkReversed))))
        println("cpkh ",cpkh);

        return cpkh
        */

}


//签名类型	            标准 v 值	                签名末尾             v 值	对应值范围
//标准 v 值 (Web3/geth)	v∈{0,1}	                    00 或 01	        v=raw v
//EIP-155 交易签名	    v=ChainID×2+35+raw v	    1b 或 1c 等	        v≥27
//v=27 / v=28 签名	    raw v∈{27,28}	            1b 或 1c	        v=27+raw v(mod2)
//original postoffic signarture: Web3/qeth, but put the v before the raw
//if bEncodeBase64, use above format
//else, use EIP-155
@OptIn(ExperimentalEncodingApi::class)
fun masterSignCompactB64(singedString: String, privateKey: String, bEncodeBase64: Boolean=true): String {
    val hash = sha256Hash(singedString)
    val hashString = hash.toHex()
    //const rawsig = secp.sign(hash, Hex. decode(this.masterKey))
    println("masterSignCompactB64, hashString: " + hashString)
    println("masterSignCompactB64, privateKey: " + privateKey)

    println("${hashString.toByteArray().size}  ${Hex.decode(privateKey).size}")

    val rawsig = Secp256k1.get().sign(hash, Hex.decode(privateKey))
    println("masterSignCompactB64, raw: " + rawsig.toHex())

    val pub = Secp256k1.pubkeyCreate(Hex.decode(privateKey))
    val pub0 = Secp256k1.ecdsaRecover(rawsig, hash, 0)
    val pub1 = Secp256k1.ecdsaRecover(rawsig, hash, 1)

    val recoveryIdByteArray = if (pub.contentEquals(pub0)) {
       if (bEncodeBase64) byteArrayOf(0) else byteArrayOf(27) //0x1b
    } else if (pub.contentEquals(pub1)) {
        if (bEncodeBase64) byteArrayOf(1) else byteArrayOf(28) //0x1c
    } else {
        throw Exception("Error Singature!")
    }
    println("Recovery ID: ${recoveryIdByteArray.toHex()}")

    /*
    // Extract r and s values
    val r = rawsig.copyOfRange(0, 32)
    val s = rawsig.copyOfRange(32, 64)

    // Convert s to BigInteger
    val sBigInt = BigInteger(1, s)

    // Define secp256k1 curve order (n)
    val curveOrder = BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16)

    // Compute recovery ID based on y-coordinate parity and s value
    val recoveryId = (r[31].toInt() and 1) xor (if (sBigInt > curveOrder.shiftRight(1)) 1 else 0)

   // println("Signature: ${Hex.encode(signature)}")
    println("Recovery ID: $recoveryId")

    */

   if (bEncodeBase64) {
       val bas464Msg = Base64.Default.encode(recoveryIdByteArray.plus(rawsig));
       println("base64Msg: " + bas464Msg);
       return bas464Msg;
   }else{
//       return recoveryIdByteArray.plus(rawsig).toHex();
       return rawsig.plus(recoveryIdByteArray).toHex();

   }
    //println(Base64.Default.encode("base64".encodeToByteArray())) // YmFzZTY0
    //return Base64.getEncoder().encodeToString(rawSigCompactHex.hexToByteArray())
    //console.log("masterSignCompactB64, rawCompactHex: ",rawsig.toCompactHex() )

//    val result = (rawsig.recovery + 0).toString(16).padStart(2, '0') + rawsig.toCompactHex()
//    console.log("masterSignCompactB64, result: ",result )
//    return Buffer.from(result, 'hex').toString('base64')
}


// Assume keccakHash is already computed and `address` is a String
// val keccakHash = digest.digest().toHex() // This should now work with the fixed toHex()

// Helper function to convert a single hex char to its integer value
// This can be a private top-level function or a function within your utility class/object
private fun Char.hexCharToInt(): Int {
    return when (this) {
        in '0'..'9' -> this - '0'
        in 'a'..'f' -> this - 'a' + 10
        in 'A'..'F' -> this - 'A' + 10
        else -> throw IllegalArgumentException("Invalid hex character: $this")
    }
}


fun checksumAddress(originAddress: String): String {

    val digest = Keccak256()
    digest.update(originAddress.encodeToByteArray())
    val keccakHash = digest.digest().toHex()


    // Assume `address` is the original address string (e.g., "0x...")
// And `keccakHash` is the hex string from your Keccak hash (e.g., "abcdef...")

// It's good practice to ensure the address is lowercase for consistent comparison
    val lowerCaseAddress = originAddress.lowercase()
    val checksumAddressBuilder = StringBuilder() // Use StringBuilder for efficient concatenation

// The logic here is for EIP-55 checksumming (Ethereum addresses)
// It takes the original address (lowercase) and selectively uppercases chars
// based on the corresponding bit in the Keccak hash of the address's hex representation.
// Note: EIP-55 typically applies to the address AFTER the "0x" prefix.
// Ensure your 'address' variable correctly represents the hex part without "0x"
// and that 'keccakHash' is the hash of that same hex part.

    for (i in lowerCaseAddress.indices) {
        val addressChar = lowerCaseAddress[i]
        // Skip '0x' prefix if present in the address string
        if (addressChar == '0' && i == 0 || addressChar == 'x' && i == 1) {
            checksumAddressBuilder.append(addressChar)
            continue
        }

        // Ensure we don't go out of bounds for keccakHash if address is longer than hash
        // (e.g., if 'address' includes "0x" and hash does not)
        if (i >= keccakHash.length) {
            checksumAddressBuilder.append(addressChar) // Append as is if no corresponding hash char
            continue
        }

        val hashChar = keccakHash[i]

        try {
            // Convert the hex character from the keccakHash to its integer value
            val hexValue = hashChar.hexCharToInt()

            // Check if the value is >= 8 (meaning the most significant bit is 1)
            if (hexValue >= 8) {
                checksumAddressBuilder.append(addressChar.uppercaseChar())
            } else {
                checksumAddressBuilder.append(addressChar)
            }
        } catch (e: IllegalArgumentException) {
            // If the hashChar is not a valid hex digit (shouldn't happen with .toHex() result)
            // or if original address has non-hex chars after 0x (also shouldn't happen for valid addresses)
            checksumAddressBuilder.append(addressChar)
        }
    }
    val checksumAddress = checksumAddressBuilder.toString()
    println("original address : ${originAddress}")
    println("Keccak Hash:: ${keccakHash}")
    println("checksumAddress address : ${checksumAddress}")

    return checksumAddress
}


/*
export const checksumAddress = (originAddress: string): string => {
    const address = (
        originAddress.startsWith("0x") ?
    originAddress.slice(2)
    : originAddress);
    const keccakHash = keccak256(address).toString("hex");

    let checksumAddress = "";
    for (let i = 0; i < address.length; i++) {
        checksumAddress +=
            Number(`0x${keccakHash[i]}`) >= 8 ?
        address[i].toUpperCase()
        : address[i];
    }
    return checksumAddress;
};
*/

/*
object POUtil {

    private val secp256k1 = Secp256k1.get()

    fun generateEthereumAddress(publicKey: ByteArray): String {
        val keccakHash = Keccak256.digest(publicKey.drop(1).toByteArray()) // Remove first byte for uncompressed public key
        return "0x" + Hex.encode(keccakHash.takeLast(20).toByteArray())
    }

    fun signMessage(messageHash: ByteArray, privateKey: ByteArray): String {
        val signature = secp256k1.sign(messageHash, privateKey)
        return Base64.getEncoder().encodeToString(signature)
    }

    fun verifySignature(messageHash: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean {
        return secp256k1.verify(messageHash, signature, publicKey)
    }

    // Convert from SW Signature to ECDSA
    fun convertFromSWSignature2ECDSA(swSignature: ByteArray): ByteArray {
        if (swSignature.size != 64) throw IllegalArgumentException("Invalid SW Signature length")
        val r = swSignature.sliceArray(0 until 32)
        val s = swSignature.sliceArray(32 until 64)
        return r + s
    }

    // Get Public Key Hash (PKH)
    fun getPKH(publicKey: ByteArray): ByteArray {
        return hash(publicKey).sliceArray(0 until 20) // Assuming `hash()` is implemented elsewhere
    }


    fun getCPKH1(privateKey: ByteArray): String {
        // Generate public key from private key using secp256k1
        val secp256k1 = Secp256k1()
        val publicKey = secp256k1.pubkeyCreate(privateKey, compressed = true)

        // Convert to hex and reverse
        return publicKey.joinToString("") { "%02x".format(it) }.reversed()
    }

    // Get Compressed Public Key Hash (CPKH)
    fun getCPKH(publicKey: ByteArray): ByteArray {
        val prefix = if ((publicKey[64] and 1) == 1.toByte()) 0x03.toByte() else 0x02.toByte()
        return byteArrayOf(prefix) + publicKey.sliceArray(0 until 32)
    }

    // Reverse Hex String
    fun reverseHex(hex: String): String {
        return hex.chunked(2).reversed().joinToString("")
    }

    // Sort JSON Object by Keys
    fun sortJSON(jsonString: String): String {
        val parsedJson = Json.parseToJsonElement(jsonString).jsonObject
        val sortedMap = parsedJson.toSortedMap(compareBy { it })
        return Json.encodeToString(JsonObject(sortedMap))
    }
}
*/