package com.tudoo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HalamanAwal()
            }
        }
    }
}

@Composable
fun HalamanAwal() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7DCDC)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tudoo",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0A3323)
        )
        Text(
            text = "Halo! Semangat hari ini!",
            fontSize = 16.sp,
            color = Color(0xFF839958)
        )
    }
}
