package eda.postgenerate.llmflag

import java.security.MessageDigest

/**
 * JSON canonicalizer for stable cache-key derivation.
 *
 * Recursively sorts object keys alphabetically, serializes to a canonical JSON string,
 * and computes the SHA-256 hash via JDK MessageDigest. See spec.md FR-011/012 and
 * data-model.md Entity 6 (Cache Key).
 */
class JsonCanonicalizer {

    /**
     * Canonicalize a parsed JSON-like structure (Map, List, String, Number, Boolean, null)
     * into a deterministic JSON string. Object keys are sorted recursively; arrays preserve
     * their insertion order.
     */
    String canonicalize(Object value) {
        canonicalizeValue(value)
    }

    /**
     * Compute the cache key as SHA-256("description\0canonicalContract").
     * The null-byte separator prevents collisions where one input's bytes are a
     * prefix of another's.
     */
    String cacheKey(String description, String canonicalContract) {
        String payload = (description == null ? "" : description) + "\u0000" + (canonicalContract == null ? "" : canonicalContract)
        MessageDigest digest = MessageDigest.getInstance("SHA-256")
        byte[] hash = digest.digest(payload.getBytes("UTF-8"))
        return hash.collect { String.format("%02x", it) }.join()
    }

    private String canonicalizeValue(Object value) {
        if (value == null) {
            return "null"
        }
        if (value instanceof Map) {
            return canonicalizeMap((Map) value)
        }
        if (value instanceof List) {
            return canonicalizeList((List) value)
        }
        if (value instanceof String) {
            return canonicalizeString((String) value)
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString()
        }
        // Fallback: stringify
        return canonicalizeString(value.toString())
    }

    private String canonicalizeMap(Map<?, ?> map) {
        List<String> keys = new ArrayList<>(map.keySet())
        Collections.sort(keys)
        StringBuilder sb = new StringBuilder("{")
        boolean first = true
        for (String key : keys) {
            if (!first) sb.append(",")
            sb.append(canonicalizeString(key))
            sb.append(":")
            sb.append(canonicalizeValue(map.get(key)))
            first = false
        }
        sb.append("}")
        return sb.toString()
    }

    private String canonicalizeList(List<?> list) {
        StringBuilder sb = new StringBuilder("[")
        boolean first = true
        for (Object item : list) {
            if (!first) sb.append(",")
            sb.append(canonicalizeValue(item))
            first = false
        }
        sb.append("]")
        return sb.toString()
    }

    private String canonicalizeString(String s) {
        StringBuilder sb = new StringBuilder("\"")
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i)
            switch (c) {
                case '"':  sb.append("\\\""); break
                case '\\': sb.append("\\\\"); break
                case '\b': sb.append("\\b"); break
                case '\f': sb.append("\\f"); break
                case '\n': sb.append("\\n"); break
                case '\r': sb.append("\\r"); break
                case '\t': sb.append("\\t"); break
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c))
                    } else {
                        sb.append(c)
                    }
            }
        }
        sb.append("\"")
        return sb.toString()
    }
}
