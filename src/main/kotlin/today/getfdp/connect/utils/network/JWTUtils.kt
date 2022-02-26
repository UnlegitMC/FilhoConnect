package today.getfdp.connect.utils.network

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import com.google.gson.JsonPrimitive
import today.getfdp.connect.FConnect
import today.getfdp.connect.utils.protocol.JoseStuff
import java.lang.reflect.Field
import java.math.BigInteger
import java.security.KeyPair
import java.security.MessageDigest
import java.security.Signature
import java.util.*


object JWTUtils {

    private val primitiveValueField: Field

    init {
        JsonPrimitive::class.java.getDeclaredField("value").also {
            it.isAccessible = true
            primitiveValueField = it
        }
    }

    fun toJWT(payload: String, keyPair: KeyPair): String {
        val headerJson = json { obj(
            "alg" to "ES384",
            "x5u" to Base64.getEncoder().encodeToString(keyPair.public.encoded)
        )}
        val header = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.toJsonString().toByteArray(Charsets.UTF_8))
        val payloadB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray(Charsets.UTF_8))
        val sign = signBytes("$header.$payloadB64".toByteArray(Charsets.UTF_8), keyPair)
        return "$header.$payloadB64.$sign"
    }

    fun signBytes(dataToSign: ByteArray, keyPair: KeyPair): String {
        val signature = Signature.getInstance("SHA384withECDSA")
        signature.initSign(keyPair.private)
        signature.update(dataToSign)
        val signatureBytes = JoseStuff.DERToJOSE(signature.sign(), JoseStuff.AlgorithmType.ECDSA384)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes)
    }

    fun parseJson(string: String): Any {
        return FConnect.parser.parse(StringBuilder(string))
    }

    fun parseJsonArr(string: String): JsonArray<*> {
        return parseJson(string) as JsonArray<*>
    }

    fun parseJsonObj(string: String): JsonObject {
        return parseJson(string) as JsonObject
    }

    fun getValuePrimitive(json: JsonPrimitive): Any {
        return primitiveValueField.get(json)
    }

    fun md5(string: String): String {
        val m = MessageDigest.getInstance("MD5")
        m.reset()
        m.update(string.toByteArray(Charsets.UTF_8))
        val digest = m.digest()
        val bigInt = BigInteger(1, digest)
        var hashtext = bigInt.toString(16)
        // Now we need to zero pad it if you actually want the full 32 chars.
        while (hashtext.length < 32) {
            hashtext = "0$hashtext"
        }
        return hashtext
    }
}