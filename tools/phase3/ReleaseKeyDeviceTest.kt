package com.lomipe.devhub

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lomipe.devhub.core.ReleaseKey
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReleaseKeyDeviceTest {
    @Test fun androidCanRoundTripPortableKey() {
        val p = "device-test-password".toCharArray()
        val generated = ReleaseKey.generate("maxrepo", p)
        try {
            assertEquals(generated.fingerprint, ReleaseKey.inspect(generated.bytes, "maxrepo", p))
        } finally { p.fill('\u0000'); generated.bytes.fill(0) }
    }
}
