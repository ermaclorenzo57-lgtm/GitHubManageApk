package com.lomipe.devhub.core

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Date
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder

object ReleaseKey {
    data class Generated(val bytes: ByteArray, val fingerprint: String)
    private fun fingerprint(cert: X509Certificate) =
        MessageDigest.getInstance("SHA-256").digest(cert.encoded).joinToString("") { "%02x".format(it.toInt() and 255) }

    fun generate(alias: String, password: CharArray): Generated {
        require(alias.matches(Regex("[a-zA-Z0-9_-]{1,40}")))
        require(password.size >= 12)
        val rng = SecureRandom()
        val pair = KeyPairGenerator.getInstance("RSA").apply { initialize(3072, rng) }.generateKeyPair()
        val subject = X500Name("CN=MaxRepo Release")
        val now = System.currentTimeMillis()
        val holder = JcaX509v3CertificateBuilder(subject, BigInteger(160, rng).setBit(159),
            Date(now - 86400000L), Date(now + 36500L * 86400000L), subject, pair.public)
            .build(JcaContentSignerBuilder("SHA256withRSA").build(pair.private))
        val cert = CertificateFactory.getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(holder.encoded)) as X509Certificate
        cert.verify(pair.public)
        val store = KeyStore.getInstance("PKCS12")
        store.load(null, password)
        store.setKeyEntry(alias, pair.private, password, arrayOf(cert))
        val bytes = ByteArrayOutputStream().use { out -> store.store(out, password); out.toByteArray() }
        return Generated(bytes, fingerprint(cert))
    }

    fun inspect(bytes: ByteArray, alias: String, password: CharArray): String {
        val store = KeyStore.getInstance("PKCS12")
        ByteArrayInputStream(bytes).use { store.load(it, password) }
        require(store.isKeyEntry(alias))
        require(store.getKey(alias, password) != null)
        return fingerprint(store.getCertificate(alias) as X509Certificate)
    }
}
