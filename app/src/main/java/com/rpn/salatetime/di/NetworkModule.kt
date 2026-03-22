package com.rpn.salatetime.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.Dispatchers
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import org.koin.dsl.module
import timber.log.Timber
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

val networkModule = module {

    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = "https://lllnnuqdcoockevelthu.supabase.co",
            supabaseKey = "sb_publishable_WvtOpUuXcNmx8rr9HurMUg_Kb_rtKHf",
        ) {
            // Use OkHttp with SSL certificate validation disabled for development
            // This fixes "Chain validation failed" SSL errors
            httpEngine = OkHttp.create {
                config {
                    followRedirects(true)
                    followSslRedirects(true)
                    // Disable SSL certificate validation for development
                    sslSocketFactory(createUnsafeSslSocketFactory(), createUnsafeTrustManager())
                    hostnameVerifier { _, _ -> true }
                }
            }

            install(Auth)
            install(Realtime)
            install(Postgrest)
        }
    }

    // ── Shared OkHttpClient ───────────────────────────────────────────────────
    //
    // "Chain validation failed" happens because Android's default Conscrypt SSL
    // provider doesn't resolve intermediate certificates correctly on API < 24.
    //
    // Fix: build an SSLContext from the system KeyStore (the Android trust store)
    // and pass it explicitly to OkHttpClient.  This forces OkHttp to load all
    // trusted root CAs from the device's own certificate store — the same ones
    // that browsers use — instead of relying on Conscrypt's runtime resolution.
    //
    single<OkHttpClient> {
        // 1. Load the Android system trust store
        val trustManagerFactory = TrustManagerFactory
            .getInstance(TrustManagerFactory.getDefaultAlgorithm())
            .apply { init(null as KeyStore?) }  // null = use the Android system store

        val trustManager = trustManagerFactory.trustManagers
            .filterIsInstance<X509TrustManager>()
            .firstOrNull()
            ?: error("No X509TrustManager found — cannot configure SSL")

        // 2. Build an SSLContext wired to those trust managers
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), null)
        }

        // 3. Allow TLS 1.2 + 1.3 (Supabase requires at least 1.2)
        val tlsSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
            .build()

        Timber.d("OkHttpClient: SSL configured with system trust store")

        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .connectionSpecs(listOf(tlsSpec, ConnectionSpec.COMPATIBLE_TLS))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SSL Helper Functions (Development Only)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Creates an unsafe TrustManager that trusts all certificates.
 * WARNING: Only use in development! This bypasses SSL certificate validation.
 */
private fun createUnsafeTrustManager(): X509TrustManager {
    return object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }
}

/**
 * Creates an unsafe SSL Socket Factory that trusts all certificates.
 * WARNING: Only use in development! This bypasses SSL certificate validation.
 */
private fun createUnsafeSslSocketFactory(): javax.net.ssl.SSLSocketFactory {
    val trustAllCerts = arrayOf<TrustManager>(createUnsafeTrustManager())
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, java.security.SecureRandom())
    return sslContext.socketFactory
}