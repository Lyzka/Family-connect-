package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HoloBlack
import com.example.ui.theme.HoloCyan
import com.example.ui.theme.HoloCyanBright
import com.example.ui.theme.HoloGold
import com.example.ui.theme.HoloGoldBright
import com.example.ui.theme.HoloRed
import com.example.ui.theme.HoloRedBright
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily

@Composable
fun HoloAiChatOverlay(
    chatState: HoloAiChatUiState,
    onSelectContact: (String) -> Unit,
    onSelectTier: (HoloAiModelTier) -> Unit,
    onSendMessage: (String) -> Unit,
    onDismissError: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val messages = chatState.activeMessages
    val isBusy = chatState.generationState is ChatGenerationState.Loading ||
        chatState.generationState is ChatGenerationState.Streaming

    // Auto-scroll to bottom when new messages or streaming chunks arrive
    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xD802060D))
            .safeDrawingPadding()
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("holo_ai_chat_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF2051529),
                            Color(0xF5020A16)
                        )
                    )
                )
                .border(1.5.dp, HoloCyan.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            // ================= 1. TOP HEADER: PERSONA ROLE & MODEL TIER SELECTOR =================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // LCARS Vertical Cyan Accent Bar
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(32.dp)
                            .background(HoloCyan)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "HOLO-AI SUBSPACE COMMS // ${chatState.activePersona.callSign}",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.4.sp,
                            color = HoloWhite
                        )
                        Text(
                            text = "ROLE: ${chatState.activePersona.roleTitle}",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = 1.2.sp,
                            color = HoloGoldBright
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HoloCyan.copy(alpha = 0.16f))
                        .border(1.dp, HoloCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .testTag("ai_chat_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close AI Chat",
                        tint = HoloWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contact Persona Switcher + Gemini Model Tier Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                // Contact Persona Tabs (USER_01, USER_02, USER_03)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HOLO_BOT_PERSONAS.values.forEach { persona ->
                        val isSelected = persona.contactId == chatState.activeContactId
                        Box(
                            modifier = Modifier
                                .testTag("chat_persona_${persona.contactId.lowercase()}")
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) HoloCyan.copy(alpha = 0.28f)
                                    else Color(0x66031020)
                                )
                                .border(
                                    width = if (isSelected) 1.3.dp else 0.8.dp,
                                    color = if (isSelected) HoloCyanBright else HoloCyan.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelectContact(persona.contactId) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = persona.callSign,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) HoloWhite else HoloCyanBright.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Model Tier Selector (FAST: 3.5-flash-lite | GENERAL: 3.8-flash | COMPLEX: 4-argon)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HoloAiModelTier.entries.forEach { tier ->
                        val isSelected = tier == chatState.selectedTier
                        Box(
                            modifier = Modifier
                                .testTag("model_tier_${tier.name.lowercase()}")
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) HoloGold.copy(alpha = 0.26f)
                                    else Color(0x66031020)
                                )
                                .border(
                                    width = if (isSelected) 1.3.dp else 0.8.dp,
                                    color = if (isSelected) HoloGoldBright else HoloGold.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelectTier(tier) }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = tier.badgeLabel,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 9.sp,
                                color = if (isSelected) HoloGoldBright else HoloGold.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ================= 2. SCROLLABLE MULTI-TURN MESSAGE THREAD =================
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HoloBlack.copy(alpha = 0.72f))
                    .border(1.dp, HoloCyan.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp)
                    .testTag("ai_chat_message_list")
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    HoloChatBubble(message = message)
                }
            }

            // ================= 3. ERROR BANNER (IF ANY) =================
            val errorState = chatState.generationState as? ChatGenerationState.Error
            if (errorState != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HoloRed.copy(alpha = 0.2f))
                        .border(1.dp, HoloRedBright, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("ai_chat_error_banner")
                ) {
                    Text(
                        text = "TRANSMISSION ERROR: ${errorState.message}",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = HoloRedBright,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "DISMISS",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = HoloWhite,
                        modifier = Modifier
                            .clickable(onClick = onDismissError)
                            .padding(start = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ================= 4. QUICK TACTICAL PROMPTS =================
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                chatState.activePersona.quickPrompts.forEach { promptChip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(HoloCyan.copy(alpha = 0.12f))
                            .border(0.8.dp, HoloCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable(enabled = !isBusy) {
                                onSendMessage(promptChip)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "> $promptChip",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = HoloCyanBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ================= 5. INPUT FIELD & SEND BUTTON =================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Transmit message to ${chatState.activePersona.callSign} (${chatState.selectedTier.modelId})...",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 13.sp,
                            color = HoloCyanBright.copy(alpha = 0.55f)
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = HoloWhite
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !isBusy) {
                                val toSend = inputText
                                inputText = ""
                                onSendMessage(toSend)
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HoloCyanBright,
                        unfocusedBorderColor = HoloCyan.copy(alpha = 0.5f),
                        focusedContainerColor = Color(0x99031020),
                        unfocusedContainerColor = Color(0x66031020),
                        cursorColor = HoloCyanBright
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input")
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isBusy) HoloCyan.copy(alpha = 0.15f)
                            else HoloCyan.copy(alpha = 0.32f)
                        )
                        .border(
                            width = 1.4.dp,
                            color = if (isBusy) HoloCyan.copy(alpha = 0.4f) else HoloCyanBright,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable(enabled = !isBusy && inputText.isNotBlank()) {
                            val toSend = inputText
                            inputText = ""
                            onSendMessage(toSend)
                        }
                        .testTag("ai_chat_send_button")
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            color = HoloCyanBright,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Holographic Transmission",
                            tint = HoloWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HoloChatBubble(message: HoloChatMessage) {
    val isUser = message.isUser
    val bubbleBorderColor = if (isUser) HoloGold.copy(alpha = 0.75f) else HoloCyan.copy(alpha = 0.75f)
    val bubbleBgColor = if (isUser) Color(0x38FFAA00) else Color(0x2600F3FF)

    Column(
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(HoloCyanBright)
                )
            }
            Text(
                text = message.senderLabel,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (isUser) HoloGoldBright else HoloCyanBright
            )
            Text(
                text = "[${message.modelBadge}]",
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = HoloWhite.copy(alpha = 0.65f)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isUser) 10.dp else 2.dp,
                        topEnd = if (isUser) 2.dp else 10.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .background(bubbleBgColor)
                .border(
                    width = 1.dp,
                    color = bubbleBorderColor,
                    shape = RoundedCornerShape(
                        topStart = if (isUser) 10.dp else 2.dp,
                        topEnd = if (isUser) 2.dp else 10.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (message.text.isEmpty() && message.isStreaming) {
                    "SYNTHESIZING HOLOGRAPHIC STREAM..."
                } else {
                    message.text
                },
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = HoloWhite
            )
        }
    }
}
