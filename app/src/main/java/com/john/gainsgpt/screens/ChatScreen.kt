package com.john.gainsgpt.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class ChatMessage(val sender: Sender, val text: String)
enum class Sender { USER, TRAINER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userName: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var chatHistory by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    Sender.TRAINER,
                    "Hey $userName! How are you feeling today? Ready to crush your workout?"
                )
            )
        )
    }

    fun generateTrainerResponse(message: String): String {
        return when {
            message.contains("tired", ignoreCase = true) ->
                "No worries! Even champions have low days. Let's start light and see how you feel!"
            message.contains("ready", ignoreCase = true) || message.contains("let's go", ignoreCase = true) ->
                "That’s the spirit! Let’s smash today’s session. 💪"
            message.contains("sore", ignoreCase = true) ->
                "Soreness means progress, but remember to listen to your body. Want to adjust today's plan?"
            message.contains("great", ignoreCase = true) || message.contains("good", ignoreCase = true) ->
                "Love the positivity! Which muscle group are you excited for today?"
            message.contains("not sure", ignoreCase = true) ->
                "No worries! How about we focus on your weakest muscle group today for balanced gains?"
            else ->
                listOf(
                    "Keep that energy up! What’s your focus for today?",
                    "You’ve been crushing it lately—let’s keep up the momentum!",
                    "Remember, consistency is king. You’re one workout closer to your goal!",
                    "Awesome! Tell me your goal for today, and let’s get after it."
                ).random()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GainsGPT Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                reverseLayout = true
            ) {
                items(chatHistory.reversed()) { msg ->
                    Row(
                        horizontalArrangement = if (msg.sender == Sender.USER) Arrangement.End else Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = if (msg.sender == Sender.USER)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .widthIn(max = 320.dp)
                        ) {
                            Text(
                                msg.text,
                                modifier = Modifier.padding(12.dp),
                                color = if (msg.sender == Sender.USER)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(48.dp)
                )
                Button(
                    onClick = {
                        if (inputText.text.isNotBlank()) {
                            val userMsg = ChatMessage(Sender.USER, inputText.text)
                            chatHistory = chatHistory + userMsg
                            coroutineScope.launch {
                                val trainerMsg = ChatMessage(
                                    Sender.TRAINER,
                                    generateTrainerResponse(inputText.text)
                                )
                                chatHistory = chatHistory + trainerMsg
                            }
                            inputText = TextFieldValue("")
                        }
                    }
                ) {
                    Text("Send")
                }
            }
        }
    }
}
