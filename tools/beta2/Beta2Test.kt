package com.lomipe.devhub.core

import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking

class Beta2Test {
    @Test fun versionOrdering() {
        assertTrue(ReleaseVersion.parse("3.1.0-beta.10")!! > ReleaseVersion.parse("3.1.0-beta.2")!!)
        assertTrue(ReleaseVersion.parse("v3.1.0")!! > ReleaseVersion.parse("3.1.0-rc.2")!!)
        assertNull(ReleaseVersion.parse("v3.2.0/../../"))
        assertNull(ReleaseVersion.parse("not a version"))
    }

    @Test fun ignoresDraftsAndBetaForStable() {
        val releases = """[{"tag_name":"v3.3.0","draft":true,"prerelease":false},{"tag_name":"v3.2.0-beta.2","draft":false,"prerelease":true},{"tag_name":"v3.1.0","draft":false,"prerelease":false}]"""
        assertEquals("v3.1.0", newerRelease("3.0.0", releases))
        assertEquals("v3.2.0-beta.2", newerRelease("3.1.0-beta.2", releases))
        assertNull(newerRelease("3.1.0", releases))
    }

    @Test fun credentialOriginIncludesPort() {
        runBlocking {
            MockServer().use { first ->
                MockServer().use { second ->
                    val http = first.api("test-token").http
                    try {
                        http.send("GET", "http://127.0.0.1:" + second.port + "/user")
                        fail("A different origin must be refused")
                    } catch (_: IllegalArgumentException) { }
                    assertTrue(second.requests.isEmpty())
                }
            }
        }
    }
}
