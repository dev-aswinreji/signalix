package com.signalix.app.data

import java.util.UUID

object SignalManager {
    data class KeyBundle(val identity: String, val preKey: String, val signedPreKey: String)

    // TODO: replace with real libsignal-client key generation and session setup
    fun generateKeyBundle(): KeyBundle {
        return KeyBundle(
            identity = UUID.randomUUID().toString(),
            preKey = UUID.randomUUID().toString(),
            signedPreKey = UUID.randomUUID().toString()
        )
    }

    fun encrypt(plain: String, peer: String): String {
        // TODO: use Signal Protocol session cipher
        return java.util.Base64.getEncoder().encodeToString(plain.toByteArray())
    }

    fun decrypt(cipher: String, peer: String): String {
        // TODO: use Signal Protocol session cipher
        return String(java.util.Base64.getDecoder().decode(cipher))
    }
}
