package com.wc2026stickers.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wc2026stickers.app.navigation.AppNavigation
import com.wc2026stickers.app.ui.theme.WC2026Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WC2026Theme {
                AppNavigation()
            }
        }
    }
}
