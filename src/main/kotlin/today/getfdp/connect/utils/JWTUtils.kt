package today.getfdp.connect.utils

import com.beust.klaxon.JsonObject
import com.beust.klaxon.KlaxonJson
import com.beust.klaxon.Parser
import com.beust.klaxon.json
import today.getfdp.connect.FConnect
import today.getfdp.connect.network.authenticate.JoseStuff
import java.security.KeyPair
import java.security.Signature
import java.util.*


object JWTUtils {
    private val parser = Parser.default()

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

    fun parseJsonObj(string: String): JsonObject {
        return parser.parse(StringBuilder(string)) as JsonObject
    }
}