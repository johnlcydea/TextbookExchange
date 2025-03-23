package com.example.textbookexchange

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.textbookexchange.data.local.AppDatabase
import com.example.textbookexchange.data.local.BookRepository
import com.example.textbookexchange.data.local.BookViewModel
import com.example.textbookexchange.ui.theme.TextbookExchangeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.textbookexchange.screens.EditBookScreen

class MainActivity : ComponentActivity() {
    private lateinit var bookRepository: BookRepository
    private lateinit var connectivityManager: ConnectivityManager
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val bookDao = database.bookDao()
        bookRepository = BookRepository(bookDao)
        val viewModel = BookViewModel(bookRepository)

        setupNetworkObserver()

        setContent {
            TextbookExchangeTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }

    private fun setupNetworkObserver() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available")
                bookRepository.setNetworkStatus(true)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        bookRepository.syncPendingChanges()
                        Log.d(TAG, "Synced pending changes on network available")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing on network available: ${e.message}")
                    }
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
                bookRepository.setNetworkStatus(false)
            }
        }

        // Check initial network state (compatible with API 21)
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected
        bookRepository.setNetworkStatus(isConnected)
        Log.d(TAG, "Initial network state: ${if (isConnected) "CONNECTED" else "DISCONNECTED"}")

        // Register network callback (compatible with API 21)
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            val networkCallback = object : ConnectivityManager.NetworkCallback() {}
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering network callback: ${e.message}")
        }
    }
}