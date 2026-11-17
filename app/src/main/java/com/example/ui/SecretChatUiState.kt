package com.example.ui

import androidx.annotation.StringRes

sealed interface StatusFeedback {
  data class Info(@param:StringRes val messageRes: Int, val formatArgs: List<Any> = emptyList()) :
    StatusFeedback

  data class Error(@param:StringRes val messageRes: Int, val formatArgs: List<Any> = emptyList()) :
    StatusFeedback
}

data class SecretChatUiState(
  val password: String = "",
  val isPasswordVisible: Boolean = true,
  val messageInput: String = "",
  val resultOutput: String = "",
  val statusFeedback: StatusFeedback? = null,
  val isCopiedFeedbackVisible: Boolean = false,
) {
  val inputCharacterCount: Int
    get() = messageInput.length

  val detectedTokenCount: Int
    get() {
      val trimmed = messageInput.trim()
      if (trimmed.isEmpty()) return 0
      return trimmed.split(Regex("[\\s,]+")).count { it.isNotEmpty() }
    }
}
