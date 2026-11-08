package com.example.cachecleaner.core.utils

object CacheCleaningScript {

    /**
     * Builds a safe shell script targeting exclusively application cache and code_cache directories.
     * Absolute Safety Mandate: NEVER touch databases, shared_prefs, files, or credential stores.
     * Uses find -mindepth 1 -delete to purge all cache files (including hidden/dot files)
     * while preserving directory existence, ownership, permissions, and SELinux contexts.
     */
    fun buildCacheCleanScript(packageName: String): String {
        return """
            total_kb=0
            for d in \
              /data/data/$packageName/cache \
              /data/data/$packageName/code_cache \
              /data/user/*/$packageName/cache \
              /data/user/*/$packageName/code_cache \
              /data/user_de/*/$packageName/cache \
              /data/user_de/*/$packageName/code_cache \
              /data/media/*/Android/data/$packageName/cache \
              /data/media/*/Android/data/$packageName/code_cache \
              /sdcard/Android/data/$packageName/cache \
              /sdcard/Android/data/$packageName/code_cache \
              /storage/emulated/*/Android/data/$packageName/cache \
              /storage/emulated/*/Android/data/$packageName/code_cache
            do
              if [ -d "${'$'}d" ]; then
                size=${'$'}(du -sk "${'$'}d" 2>/dev/null | cut -f1)
                if [ -n "${'$'}size" ]; then
                  total_kb=${'$'}((total_kb + size))
                fi
                find "${'$'}d" -mindepth 1 -delete 2>/dev/null || rm -rf "${'$'}d"/* "${'$'}d"/.[!.]* "${'$'}d"/..?* 2>/dev/null
              fi
            done
            sync
            echo "FREED_KB:${'$'}total_kb"
        """.trimIndent()
    }

    /**
     * Builds a safe batch shell script executing across multiple packages in a single shell process.
     * Dramatically reduces process spawn overhead from O(N) to O(1).
     */
    fun buildBatchCacheCleanScript(packageNames: List<String>): String {
        val validPackages = packageNames.filter { PackageValidator.isValidPackageName(it) }
        val pkgs = validPackages.joinToString(" ")
        return """
            total_kb=0
            for p in $pkgs; do
              for d in \
                /data/data/${'$'}p/cache \
                /data/data/${'$'}p/code_cache \
                /data/user/*/${'$'}p/cache \
                /data/user/*/${'$'}p/code_cache \
                /data/user_de/*/${'$'}p/cache \
                /data/user_de/*/${'$'}p/code_cache \
                /data/media/*/Android/data/${'$'}p/cache \
                /data/media/*/Android/data/${'$'}p/code_cache \
                /sdcard/Android/data/${'$'}p/cache \
                /sdcard/Android/data/${'$'}p/code_cache \
                /storage/emulated/*/Android/data/${'$'}p/cache \
                /storage/emulated/*/Android/data/${'$'}p/code_cache
              do
                if [ -d "${'$'}d" ]; then
                  size=${'$'}(du -sk "${'$'}d" 2>/dev/null | cut -f1)
                  if [ -n "${'$'}size" ]; then
                    total_kb=${'$'}((total_kb + size))
                  fi
                  find "${'$'}d" -mindepth 1 -delete 2>/dev/null || rm -rf "${'$'}d"/* "${'$'}d"/.[!.]* "${'$'}d"/..?* 2>/dev/null
                fi
              done
            done
            sync
            pm trim-caches 999999999999 2>/dev/null
            echo "FREED_KB:${'$'}total_kb"
        """.trimIndent()
    }

    /**
     * Parses the FREED_KB indicator output by the script and converts to bytes.
     */
    fun parseFreedBytes(stdout: String): Long {
        val freedKb = Regex("FREED_KB:(\\d+)").find(stdout)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        return freedKb * 1024L
    }
}
