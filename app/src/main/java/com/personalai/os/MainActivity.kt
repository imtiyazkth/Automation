package com.personalai.os

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personalai.os.ui.chat.ChatViewModel
import com.personalai.os.ui.chat.HeadAgentChatScreen
import com.personalai.os.ui.theme.AutomationOsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as AutomationOsApp

        setContent {
            AutomationOsTheme {
                val factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        ChatViewModel(app.headAgent) as T
                }
                val chatViewModel: ChatViewModel = viewModel(factory = factory)
                HeadAgentChatScreen(chatViewModel)
            }
        }
    }
}
