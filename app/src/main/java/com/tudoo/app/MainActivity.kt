package com.tudoo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tudoo.app.ui.TudooApp
import com.tudoo.app.ui.theme.TudooTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TudooTheme {
                TudooApp()
            }
        }
    }
}
