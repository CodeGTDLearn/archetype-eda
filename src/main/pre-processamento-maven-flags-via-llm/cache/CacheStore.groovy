package eda.postgenerate.pre_processamento_maven_flags_via_llm.cache

import groovy.json.JsonSlurper

/**
 * JSONL cache on disk, outside the repository.
 *
 * Responsibility: persistence of the produced command line and its provenance
 * report. It does not derive keys or decide whether a hit is valid; the
 * application owns that policy.
 */
class CacheStore {

    static java.io.File cacheFile(String cacheDir) {
        return new File(cacheDir, "cache.jsonl")
    }

    /**
     * Reads the cache file line by line.
     *
     * The on-disk format is JSON, so each line is parsed with JsonSlurper.
     * v1 entries (no "r" field) are still readable: they yield r == null and
     * the caller prints the legacy-entry note. A corrupted line is skipped.
     */
    Map<String, String> lookup(String cacheDir, String key) {
        if (key == null) return null
        java.io.File f = cacheFile(cacheDir)
        if (!f.exists()) return null
        Map<String, String> found = null
        f.eachLine("UTF-8") { String line ->
            if (line == null || line.trim().isEmpty()) return
            Map parsed = null
            try {
                Object obj = new JsonSlurper().parseText(line)
                if (obj instanceof Map) parsed = (Map) obj
            } catch (Exception e) {
                parsed = null
            }
            if (parsed == null) return
            String lineKey = parsed.get("k") == null ? null : parsed.get("k").toString()
            if (lineKey == null || lineKey != key) return
            Map<String, String> hit = new LinkedHashMap<>()
            hit.put("v", parsed.get("v") == null ? null : parsed.get("v").toString())
            Object report = parsed.get("r")
            hit.put("r", report == null ? null : report.toString())
            found = hit
        }
        return found
    }

    void write(String cacheDir, String key, String commandLine, String report) {
        if (key == null || commandLine == null || report == null) {
            throw new IllegalArgumentException("CacheStore.write: key, commandLine and report MUST be non-null")
        }
        java.io.File dir = new File(cacheDir)
        if (!dir.exists()) dir.mkdirs()
        java.io.File target = cacheFile(cacheDir)
        java.io.File tmp = new File(target.absolutePath + ".tmp." + System.nanoTime())
        long now = System.currentTimeMillis() / 1000L
        tmp.withWriter("UTF-8") { writer ->
            writer.write("{\"k\":\"" + key + "\",\"v\":" + escapeJsonString(commandLine) +
                    ",\"r\":" + escapeJsonString(report) + ",\"t\":" + now + "}")
            writer.write(System.lineSeparator())
        }
        if (!tmp.renameTo(target)) {
            tmp.withReader("UTF-8") { reader ->
                target.append(reader.text, "UTF-8")
            }
            tmp.delete()
        }
    }

    private String escapeJsonString(String s) {
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
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c))
                    else sb.append(c)
            }
        }
        sb.append("\"")
        return sb.toString()
    }
}
