package com.lomipe.devhub.core

data class ReleaseVersion(val major: Int, val minor: Int, val patch: Int, val stage: Int, val serial: Int) : Comparable<ReleaseVersion> {
    override fun compareTo(other: ReleaseVersion): Int =
        compareValuesBy(this, other, { it.major }, { it.minor }, { it.patch }, { it.stage }, { it.serial })

    companion object {
        private val pattern = Regex("""^v?([0-9]{1,8})\.([0-9]{1,8})\.([0-9]{1,8})(?:-(alpha|beta|rc)\.([0-9]{1,8}))?$""")
        fun parse(tag: String): ReleaseVersion? {
            val m = pattern.matchEntire(tag) ?: return null
            val (a, b, c, stage, serial) = m.destructured
            return ReleaseVersion(a.toInt(), b.toInt(), c.toInt(),
                when(stage) { "alpha" -> 0; "beta" -> 1; "rc" -> 2; else -> 3 },
                serial.toIntOrNull() ?: 0)
        }
    }
}

fun newerRelease(installed: String, releases: String): String? {
    val current = ReleaseVersion.parse(installed) ?: return null
    val entries = Json.parse(releases) as? List<*> ?: error("Invalid release response")
    return entries.mapNotNull { item ->
        val obj = item.asObj() ?: return@mapNotNull null
        if (obj["draft"] != false) return@mapNotNull null
        val tag = obj.s("tag_name") ?: return@mapNotNull null
        val v = ReleaseVersion.parse(tag) ?: return@mapNotNull null
        if (current.stage == 3 && (v.stage != 3 || obj["prerelease"] != false)) return@mapNotNull null
        if (v <= current) return@mapNotNull null
        tag to v
    }.maxByOrNull { it.second }?.first
}
