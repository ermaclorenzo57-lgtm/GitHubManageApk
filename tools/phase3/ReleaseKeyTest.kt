package com.lomipe.devhub.core

import org.junit.Assert.*
import org.junit.Test

class ReleaseKeyTest {
    @Test fun createsPortableKey() {
        val password = "test-only-strong-password".toCharArray()
        val key = ReleaseKey.generate("maxrepo", password)
        try {
            assertEquals(64, key.fingerprint.length)
            assertEquals(key.fingerprint, ReleaseKey.inspect(key.bytes, "maxrepo", password))
            try {
                ReleaseKey.inspect(key.bytes, "maxrepo", "incorrect-password".toCharArray())
                fail("Wrong password accepted")
            } catch (_: java.io.IOException) { }
        } finally {
            password.fill('\u0000')
            key.bytes.fill(0)
        }
    }
}
