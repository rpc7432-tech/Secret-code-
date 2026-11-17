package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.crypto.NumericCipher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SecretChatViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(SecretChatUiState())
  val uiState: StateFlow<SecretChatUiState> = _uiState.asStateFlow()

  private var copyFeedbackJob: Job? = null

  fun onPasswordChange(newPassword: String) {
    // Allow optional leading '-' and ASCII digits only so input stays cleanly numeric
    val sanitized = buildString {
      newPassword.forEachIndexed { index, ch ->
        if (ch.isDigit() || (ch == '-' && index == 0)) {
          append(ch)
        }
      }
    }
    _uiState.update { it.copy(password = sanitized) }
  }

  fun onTogglePasswordVisibility() {
    _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
  }

  fun onClearPassword() {
    _uiState.update { it.copy(password = "", statusFeedback = null) }
  }

  fun onMessageInputChange(newMessage: String) {
    _uiState.update { it.copy(messageInput = newMessage) }
  }

  fun onClearMessageInput() {
    _uiState.update { it.copy(messageInput = "", statusFeedback = null) }
  }

  fun onEncryptClick() {
    val state = _uiState.value
    when (val result = NumericCipher.encrypt(state.password, state.messageInput)) {
      is NumericCipher.EncryptResult.InvalidPassword -> {
        _uiState.update {
          it.copy(statusFeedback = StatusFeedback.Error(R.string.error_invalid_password_encrypt))
        }
      }

      is NumericCipher.EncryptResult.EmptyMessage -> {
        _uiState.update {
          it.copy(statusFeedback = StatusFeedback.Error(R.string.error_empty_message_encrypt))
        }
      }

      is NumericCipher.EncryptResult.Success -> {
        val feedback =
          if (result.characterCount == 1) {
            StatusFeedback.Info(R.string.status_encrypted_singular)
          } else {
            StatusFeedback.Info(R.string.status_encrypted_plural, listOf(result.characterCount))
          }
        _uiState.update {
          it.copy(
            resultOutput = result.cipherText,
            statusFeedback = feedback,
            isCopiedFeedbackVisible = false,
          )
        }
      }
    }
  }

  fun onDecryptClick() {
    val state = _uiState.value
    when (val result = NumericCipher.decrypt(state.password, state.messageInput)) {
      is NumericCipher.DecryptResult.InvalidPassword -> {
        _uiState.update {
          it.copy(statusFeedback = StatusFeedback.Error(R.string.error_invalid_password_decrypt))
        }
      }

      is NumericCipher.DecryptResult.EmptyMessage -> {
        _uiState.update {
          it.copy(statusFeedback = StatusFeedback.Error(R.string.error_empty_message_decrypt))
        }
      }

      is NumericCipher.DecryptResult.InvalidToken -> {
        _uiState.update {
          it.copy(
            statusFeedback =
              StatusFeedback.Error(R.string.error_invalid_token_format, listOf(result.token))
          )
        }
      }

      is NumericCipher.DecryptResult.OutOfRangeCharCode -> {
        _uiState.update {
          it.copy(statusFeedback = StatusFeedback.Error(R.string.error_out_of_range_char))
        }
      }

      is NumericCipher.DecryptResult.Success -> {
        val feedback =
          if (result.tokenCount == 1) {
            StatusFeedback.Info(R.string.status_decrypted_singular)
          } else {
            StatusFeedback.Info(R.string.status_decrypted_plural, listOf(result.tokenCount))
          }
        _uiState.update {
          it.copy(
            resultOutput = result.plainText,
            statusFeedback = feedback,
            isCopiedFeedbackVisible = false,
          )
        }
      }
    }
  }

  fun onPasteFromClipboard(clipboardText: String?) {
    if (clipboardText.isNullOrEmpty()) {
      _uiState.update {
        it.copy(statusFeedback = StatusFeedback.Error(R.string.error_clipboard_empty))
      }
    } else {
      _uiState.update {
        it.copy(
          messageInput = clipboardText,
          statusFeedback = StatusFeedback.Info(R.string.status_pasted),
        )
      }
    }
  }

  fun onCopyResult(onCopyToSystemClipboard: (String) -> Unit) {
    val textToCopy = _uiState.value.resultOutput
    if (textToCopy.isEmpty()) {
      _uiState.update {
        it.copy(statusFeedback = StatusFeedback.Error(R.string.error_nothing_to_copy))
      }
      return
    }

    onCopyToSystemClipboard(textToCopy)
    _uiState.update {
      it.copy(
        isCopiedFeedbackVisible = true,
        statusFeedback = StatusFeedback.Info(R.string.status_copied),
      )
    }

    copyFeedbackJob?.cancel()
    copyFeedbackJob =
      viewModelScope.launch {
        delay(2000L)
        _uiState.update { it.copy(isCopiedFeedbackVisible = false) }
      }
  }

  fun onSwapResultToInput() {
    val currentResult = _uiState.value.resultOutput
    if (currentResult.isEmpty()) return
    _uiState.update {
      it.copy(
        messageInput = currentResult,
        resultOutput = "",
        isCopiedFeedbackVisible = false,
        statusFeedback = StatusFeedback.Info(R.string.status_swapped),
      )
    }
  }
}
