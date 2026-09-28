package eda.postgenerate.llmflag

import java.security.MessageDigest

/**
 * JSONL cache on disk, outside the repository.
 * See spec.md FR-011, FR-012, FR-017; data-model.md Entity 6.
 *
 * Path: $XDG_CACHE_HOME/eda-archetype/llm-flag-inference/cache.jsonl
 *       with %LOCALAPPDATA% fallback on Windows when XDG_CACHE_HOME is unset.
 *
 * Storage: one JSON object per line, {"k": <sha256-hex>, "v": <command-line>, "t": <epoch-seconds>}.
 * Writes use tmp + File.renameTo for atomicity.
 * NEVER persists the contract payload — only the produced command line.
 */
class InvocationCache {

    static String defaultCacheDir() {
        String xdg = System.getenv("XDG_CACHE_HOME")
        if (xdg != null && !xdg.isEmpty()) {
            return new File(xdg, "eda-archetype/llm-flag-inference").absolutePath
        }
        String localAppData = System.getenv("LOCALAPPDATA")
        if (localAppData != null && !localAppData.isEmpty()) {
            return new File(localAppData, "eda-archetype/llm-flag-inference").absolutePath
        }
        // Last-resort fallback: ~/.cache (POSIX convention)
        String userHome = System.getProperty("user.home")
        return new File(userHome, ".cache/eda-archetype/llm-flag-inference").absolutePath
    }

    static java.io.File cacheFile(String cacheDir) {
        return new File(cacheDir, "cache.jsonl")
    }

    String lookup(String cacheDir, String key) {
        if (key == null) return null
        java.io.File f = cacheFile(cacheDir)
        if (!f.exists()) return null
        f.eachLine("UTF-8") { line ->
            if (line == null || line.isEmpty()) return
            def match = (line =~ /^\{"k":"([0-9a-f]{64})","v":"((?:[^"\\]|\\.)*)","t":(\d+)\}$/)
            if (match) {
                String k = match[0][1]
                if (k == key) {
                    return unescape(match[0][2])
                }
            }
        }
        return null
    }

    void write(String cacheDir, String key, String commandLine) {
        if (key == null || commandLine == null) {
            throw new IllegalArgumentException("InvocationCache.write: key and commandLine MUST be non-null")
        }
        java.io.File dir = new File(cacheDir)
        if (!dir.exists()) dir.mkdirs()
        java.io.File target = cacheFile(cacheDir)
        java.io.File tmp = new File(target.absolutePath + ".tmp." + System.nanoTime())
        long now = System.currentTimeMillis() / 1000L

        tmp.withWriter("UTF-8") { writer ->
            writer.write("{\"k\":\"" + key + "\",\"v\":\"" + escape(commandLine) + "\",\"t\":" + now + "}")
            writer.write(System.lineSeparator())
        }
        // Atomic rename
        if (!tmp.renameTo(target)) {
            // Fallback: append to target if rename fails (rare; means another process opened it)
            tmp.withReader("UTF-8") { reader ->
                target.append(reader.text, "UTF-8")
            }
            tmp.delete()
        }
    }

    private String escape(String s) {
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

    private String unescape(String s) {
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i)
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1)
                switch (next) {
                    case '"':  sb.append('"');  i++; break
                    case '\\': sb.append('\\'); i++; break
                    case 'b':  sb.append('\b'); i++; break
                    case 'f':  sb.append('\f'); i++; break
                    case 'n':  sb.append('\n'); i++; break
                    case 'r':  sb.append('\r'); i++; break
                    case 't':  sb.append('\t'); i++; break
                    case 'u':
                        if (i + 5 < s.length()) {
                            String hex = s.substring(i + 2, i + 6)
                            sb.append((char) Integer.parseInt(hex, 16))
                            i += 5
                        }
                        break
                    default:
                        sb.append(next)
                        i++
                }
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }
}
