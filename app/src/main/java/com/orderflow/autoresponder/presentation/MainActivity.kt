package com.orderflow.autoresponder.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.orderflow.autoresponder.core.security.SecureStorage
import com.orderflow.autoresponder.presentation.components.SuspendedScreen
import com.orderflow.autoresponder.presentation.navigation.NavGraph
import com.orderflow.autoresponder.presentation.theme.OrderFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var secureStorage: SecureStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrderFlowTheme {
                val isBlocked by secureStorage.applicationBlockFlow().collectAsState(initial = secureStorage.isEffectivelyBlocked())
                val context = androidx.compose.ui.platform.LocalContext.current

                LaunchedEffect(isBlocked) {
                    if (isBlocked) {
                        android.widget.Toast.makeText(context, "Access Restricted by Administrator", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
                
                android.util.Log.d("OrderFlow", "[MainActivity] UI Recomposing. isBlocked=$isBlocked, adminLocked=${secureStorage.isAdminLocked()}, subActive=${secureStorage.isSubscriptionActive()}")

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isBlocked) {
                        // Pass current state reason to screen
                        SuspendedScreen(
                            isAdminLocked = secureStorage.isAdminLocked(),
                            subscriptionStatus = secureStorage.getSubscriptionStatus()
                        )
                    } else {
                        NavGraph()
                    }
                }
            }
        }
    }
}
