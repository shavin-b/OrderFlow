package com.orderflow.autoresponder.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
                val isSubSuspended by secureStorage.isAppSuspendedFlow().collectAsState(initial = secureStorage.isAppSuspended())
                val isAdminLocked by secureStorage.isAdminLockedFlow().collectAsState(initial = secureStorage.isAdminLocked())
                
                val isLocked = isSubSuspended || isAdminLocked

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLocked) {
                        SuspendedScreen()
                    } else {
                        NavGraph()
                    }
                }
            }
        }
    }
}
