package com.orderflow.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

/**
 * NETWORK UTILS
 *
 * Purpose:
 * Provides methods to check internet connectivity before making Firebase calls.
 * Every Firestore operation that requires online access should first call
 * NetworkUtils.isNetworkAvailable(context) to show a meaningful error message
 * instead of letting the Firebase call time out silently.
 *
 * Why is this needed?
 * Firebase Firestore has offline persistence enabled by default, which means
 * write operations are queued locally even when offline. However, for read
 * operations that must return fresh data (like loading the dashboard stats),
 * we want to warn the user they are offline instead of showing stale data.
 *
 * Note on API compatibility:
 * The modern ConnectivityManager.getNetworkCapabilities() API is used (API 23+).
 * Since our minSdk is 26, we don't need the deprecated isConnected() fallback.
 */
public final class NetworkUtils {

    // Private constructor — utility class, not meant to be instantiated
    private NetworkUtils() {}

    /**
     * Checks if the device has an active internet connection.
     *
     * Uses ConnectivityManager.getNetworkCapabilities() which is the modern,
     * accurate API. It checks for:
     * - NET_CAPABILITY_INTERNET: the network can reach the internet
     * - NET_CAPABILITY_VALIDATED: the connection has been verified (not just connected to a
     *   WiFi hotspot without actual internet — common in Android's validation system)
     *
     * @param context Application or Activity context
     * @return true if the device is connected to the internet and the connection is validated
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;

        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
        if (caps == null) return false;

        // Check both: can reach internet AND the connection has been validated
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    /**
     * Returns the type of active network connection as a human-readable string.
     * Used for debug logging and diagnostics.
     *
     * @param context Application or Activity context
     * @return "WiFi", "Mobile Data", "Ethernet", "Unknown", or "None"
     */
    public static String getNetworkType(Context context) {
        if (context == null) return "None";

        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return "None";

        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork == null) return "None";

        NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
        if (caps == null) return "None";

        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))     return "WiFi";
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return "Mobile Data";
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return "Ethernet";

        return "Unknown";
    }
}
