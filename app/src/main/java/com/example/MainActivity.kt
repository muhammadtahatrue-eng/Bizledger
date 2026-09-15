package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainScreen
import com.example.ui.theme.BizLedgerTheme
import com.example.ui.viewmodel.BizLedgerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BizLedgerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BizLedgerTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
