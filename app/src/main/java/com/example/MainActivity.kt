package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainApp
import com.example.ui.theme.LishNilaiTheme
import com.example.ui.viewmodel.LishNilaiViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LishNilaiViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val effectiveDarkTheme = isDarkMode ?: systemDark

            LishNilaiTheme(darkTheme = effectiveDarkTheme) {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

