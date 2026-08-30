package com.arrowescape.game.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object NetworkManager {

    private const val TAG = "NetworkManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isRegistered = false

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        // Immediate sync check
        val initialStatus = checkInstantConnectivity(appContext)
        _isOnline.value = initialStatus

        registerNetworkCallback()
        // Run deep active ping check in background
        checkActiveInternet()
    }

    private fun registerNetworkCallback() {
        val cm = connectivityManager ?: return
        if (isRegistered) return

        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "Network available. Verifying active internet...")
                    checkActiveInternet()
                }

                override fun onLost(network: Network) {
                    Log.d(TAG, "Network lost.")
                    _isOnline.value = false
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    if (hasInternet && isValidated) {
                        _isOnline.value = true
                    } else if (!hasInternet) {
                        _isOnline.value = false
                    }
                }
            }

            cm.registerNetworkCallback(request, callback)
            networkCallback = callback
            isRegistered = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback: ${e.message}", e)
        }
    }

    fun checkInstantConnectivity(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false

            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            hasInternet && isValidated
        } catch (e: Exception) {
            Log.e(TAG, "Error checking connectivity: ${e.message}", e)
            false
        }
    }

    fun checkActiveInternet(onComplete: ((Boolean) -> Unit)? = null) {
        scope.launch {
            val connected = withContext(Dispatchers.IO) {
                try {
                    // Try connecting to a reliable DNS/HTTP server with 2.5s timeout
                    val socket = Socket()
                    val socketAddress = InetSocketAddress("8.8.8.8", 53)
                    socket.connect(socketAddress, 2500)
                    socket.close()
                    true
                } catch (e: Exception) {
                    try {
                        val socket2 = Socket()
                        val socketAddress2 = InetSocketAddress("1.1.1.1", 53)
                        socket2.connect(socketAddress2, 2500)
                        socket2.close()
                        true
                    } catch (e2: Exception) {
                        false
                    }
                }
            }

            _isOnline.value = connected
            Log.d(TAG, "Active Internet Check Result: $connected")
            onComplete?.invoke(connected)
        }
    }

    suspend fun refreshConnectivityAsync(): Boolean {
        return withContext(Dispatchers.IO) {
            val connected = try {
                val socket = Socket()
                val socketAddress = InetSocketAddress("8.8.8.8", 53)
                socket.connect(socketAddress, 2500)
                socket.close()
                true
            } catch (e: Exception) {
                try {
                    val socket2 = Socket()
                    val socketAddress2 = InetSocketAddress("1.1.1.1", 53)
                    socket2.connect(socketAddress2, 2500)
                    socket2.close()
                    true
                } catch (e2: Exception) {
                    false
                }
            }

            withContext(Dispatchers.Main) {
                _isOnline.value = connected
            }
            connected
        }
    }
}
