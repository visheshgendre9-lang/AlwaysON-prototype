package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.service.KeepAwakeService
import com.example.ui.ScreenAwakeScreen
import com.example.ui.theme.GlassBackgroundDark
import com.example.ui.theme.ScreenAwakeTheme
import com.example.viewmodel.ScreenAwakeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ScreenAwakeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ScreenAwakeTheme {
                val session by viewModel.sessionState.collectAsState()

                // Dynamically update Activity window FLAG_KEEP_SCREEN_ON
                LaunchedEffect(session.isActive) {
                    if (session.isActive) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = GlassBackgroundDark
                ) {
                    ScreenAwakeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
