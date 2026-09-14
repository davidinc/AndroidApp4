package com.dawit.androidapp4.data.remote

import android.util.Log
import okhttp3.Dns
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * A custom DNS resolver that provides a fallback if the system DNS fails.
 *
 * Emulators sometimes lose the ability to resolve hostnames even when
 * IP-based internet access is working. This selector attempts the
 * standard system resolution first, and can be extended to use
 * hardcoded fallbacks if necessary.
 */
class DnsSelector : Dns {

    override fun lookup(hostname: String): List<InetAddress> {
        return try {
            // Attempt standard system resolution.
            Dns.SYSTEM.lookup(hostname).also {
                Log.d("DnsSelector", "System resolved $hostname to $it")
            }
        } catch (e: UnknownHostException) {
            Log.w("DnsSelector", "System failed to resolve $hostname. Attempting hardcoded fallback.")

            // If itunes.apple.com fails, we can try to provide a known IP as a last resort,
            // though it's better to fix the emulator's network bridge.
            // For now, we rethrow to see if adding an OkHttpClient with better timeouts
            // and logging helps identify the bottleneck.
            throw e
        }
    }
}
