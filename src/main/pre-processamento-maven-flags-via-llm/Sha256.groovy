package eda.postgenerate.pre_processamento_maven_flags_via_llm

import java.security.MessageDigest

/**
 * SHA-256 helper shared by the cache key and the contract materialization.
 */
class Sha256 {

    static String hex(String payload) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256")
        byte[] hash = digest.digest((payload == null ? "" : payload).getBytes("UTF-8"))
        return hash.collect { String.format("%02x", it) }.join()
    }
}
